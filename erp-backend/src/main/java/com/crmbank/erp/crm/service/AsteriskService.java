package com.crmbank.erp.crm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.crmbank.erp.crm.handler.*;
import com.crmbank.erp.crm.mapper.inbound.*;
import com.crmbank.erp.crm.dto.*;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.action.*;
import org.asteriskjava.manager.event.*;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsteriskService implements ManagerEventListener {

    private final ManagerConnection managerConnection;
    private final InboundMapper inboundMapper;
    private final CtiWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, String> exten_channel_map = new ConcurrentHashMap<>();
    private final Map<String, String> exten_caller_channel = new ConcurrentHashMap<>();
    private final Map<String, String> exten_linked_id = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> session_channels = new ConcurrentHashMap<>();
    private final Set<String> registered_extens = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public Map<String, String> getExtenChannelMap() { return exten_channel_map; }
    public Map<String, String> getExtenLinkedId() { return exten_linked_id; }
    public Map<String, Set<String>> getSessionChannels() { return session_channels; }
    public List<String> getRegisteredExtens() { return new ArrayList<>(registered_extens); }

    @Override
    public void onManagerEvent(ManagerEvent event) {
        log.info("🔔 [AMI EVENT] 수신된 이벤트: {} - {}", event.getClass().getSimpleName(), event.toString());
        if (event instanceof NewStateEvent) {
            handleNewState((NewStateEvent) event);
        } else if (event instanceof AgentCalledEvent) {
            handleAgentCalled((AgentCalledEvent) event);
        } else if (event instanceof DialBeginEvent) {
            handleDialBegin((DialBeginEvent) event);
        } else if (event instanceof BridgeEnterEvent) {
            handleBridgeEnter((BridgeEnterEvent) event);
        } else if (event instanceof HangupEvent) {
            handleHangup((HangupEvent) event);
        } else if (event instanceof SoftHangupRequestEvent) {
            handleHangupEventCommon(((SoftHangupRequestEvent) event).getChannel());
        } else if (event instanceof NewChannelEvent) {
            handleNewChannel((NewChannelEvent) event);
        }
    }

    private void handleDialBegin(DialBeginEvent e) {
        String destChannel = e.getDestChannel();
        String ext = extractNumberOnly(destChannel);
        log.info("🎯 [TRACE-1] DialBeginEvent 감지됨! DestChannel: {}, ExtractedExt: {}, CallerId: {}, Channel: {}, LinkedId: {}", 
                destChannel, ext, e.getCallerIdNum(), e.getChannel(), e.getLinkedId());
        if (ext != null) {
            String cleanExt = ext.trim();
            log.info("📢 [POPUP] DialBegin 신호 수신: 내선 [{}] <- 고객 채널 [{}]", cleanExt, e.getChannel());
            exten_caller_channel.put(cleanExt, e.getChannel());
            exten_linked_id.put(cleanExt, e.getLinkedId());
            sendInboundPopup(cleanExt, e.getCallerIdNum(), e.getChannel(), e.getLinkedId());
        } else {
            log.warn("⚠️ [TRACE-1] DialBeginEvent에서 내선번호 추출 실패 (destChannel: {})", destChannel);
        }
    }

    private void handleNewChannel(NewChannelEvent event) {
        String ext = extractNumberOnly(event.getChannel());
        if (ext != null) exten_channel_map.put(ext.trim(), event.getChannel());
    }

    private void handleNewState(NewStateEvent e) {
        String channel = e.getChannel();
        String ext = extractNumberOnly(channel);
        if (ext != null) {
            exten_channel_map.put(ext.trim(), channel);
        }
    }

    private void handleAgentCalled(AgentCalledEvent e) {
        String agentExt = extractNumberOnly(e.getInterface());
        if (agentExt != null) {
            String cleanExt = agentExt.trim();
            log.info("📢 [POPUP] 큐 대기열 신호 수신: {} <- {}", cleanExt, e.getCallerIdNum());
            exten_caller_channel.put(cleanExt, e.getChannel());
            exten_linked_id.put(cleanExt, e.getLinkedId());
            sendInboundPopup(cleanExt, e.getCallerIdNum(), e.getChannel(), e.getLinkedId());
        }
    }

    private void handleBridgeEnter(BridgeEnterEvent e) {
        String exten = extractNumberOnly(e.getChannel());
        if (exten != null) {
            sendCtiEvent(exten.trim(), "CALL_CONNECTED", e.getChannel(), null);
        }
    }

    private void handleHangup(HangupEvent e) {
        handleHangupEventCommon(e.getChannel());
    }

    private void handleHangupEventCommon(String channel) {
        if (channel == null) return;
        String exten = extractNumberOnly(channel);
        List<String> extensToRemove = new ArrayList<>();
        exten_caller_channel.forEach((ext, ch) -> {
            if (ch != null && ch.equals(channel)) {
                extensToRemove.add(ext);
            }
        });
        if (exten != null) extensToRemove.add(exten.trim());

        for (String cleanExt : extensToRemove) {
            String recFile = exten_linked_id.get(cleanExt);
            sendCtiEvent(cleanExt, "CALL_HANGUP", channel, recFile);
            exten_channel_map.remove(cleanExt);
            exten_caller_channel.remove(cleanExt);
            exten_linked_id.remove(cleanExt);
            log.info("🏠 [HANGUP CLEANUP] 내선 [{}] 채널 상태 정리 완료", cleanExt);
        }
    }

    public void answerCall(String exten) {
        if (exten == null) return;
        String cleanExt = exten.trim();
        String callerChannel = exten_caller_channel.get(cleanExt);
        if (callerChannel == null) {
            callerChannel = "SIP/" + cleanExt;
        }
        try {
            log.info("🚀 [AnswerCall] Redirecting channel [{}] to cti-answer-force for agent [{}]", callerChannel, cleanExt);
            managerConnection.sendAction(new SetVarAction(callerChannel, "AGENT_EXTEN", cleanExt));
            managerConnection.sendAction(new RedirectAction(callerChannel, "cti-answer-force", "s", 1));
        } catch (Exception e) {
            log.error("❌ [AnswerCall] Error: {}", e.getMessage(), e);
        }
    }

    public void hangupCall(String exten, String ch) {
        if (exten == null) return;
        String cleanExt = exten.trim();
        try {
            managerConnection.sendAction(new HangupAction("SIP/" + cleanExt));
            String myCh = exten_channel_map.get(cleanExt);
            if (myCh != null) managerConnection.sendAction(new HangupAction(myCh));
            String callerCh = exten_caller_channel.get(cleanExt);
            if (callerCh != null) managerConnection.sendAction(new HangupAction(callerCh));
        } catch (Exception ex) {
            log.error("❌ [HangupCall] Error: {}", ex.getMessage(), ex);
        }
    }

    public boolean checkAmiConnection() {
        return managerConnection != null && managerConnection.getState() == org.asteriskjava.manager.ManagerConnectionState.CONNECTED;
    }

    private void sendInboundPopup(String exten, String callerId, String channel, String linkedId) {
        log.info("🎯 [TRACE-2] sendInboundPopup 진입! exten: {}, callerId: {}, channel: {}, linkedId: {}", exten, callerId, channel, linkedId);
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("type", "INBOUND_CALL");
            data.put("callerid", callerId);
            data.put("exten", exten);
            data.put("linkedid", linkedId);
            String cmpycd = webSocketHandler.getCmpycd(exten);
            log.info("🎯 [TRACE-3] WebSocketHandler.getCmpycd 결과: {}", cmpycd);
            data.put("cmpycd", cmpycd != null && !cmpycd.isEmpty() ? cmpycd : "COIT");

            try {
                Map<String, Object> params = new HashMap<>();
                params.put("cmpycd", data.get("cmpycd"));
                params.put("phone", callerId);
                log.info("🎯 [TRACE-4] 고객 DB 조회 시작 (params: {})", params);
                Map<String, Object> customer = inboundMapper.findCustomerByPhoneMap(params);
                log.info("🎯 [TRACE-5] 고객 DB 조회 완료 (customer: {})", customer);
                if (customer != null) {
                    customer.forEach((k, v) -> data.put(k.toLowerCase(), v));
                } else {
                    data.put("custnm", "미등록 고객");
                }
            } catch (Exception dbEx) {
                log.warn("⚠️ [POPUP] 고객 정보 DB 조회 중 예외 발생 (무시하고 팝업 전송): {}", dbEx.getMessage(), dbEx);
                data.put("custnm", "미등록 고객");
            }

            String json = objectMapper.writeValueAsString(data);
            log.info("🚀 [POPUP] 웹소켓 전송 시도 -> 내선 [{}]: {}", exten, json);
            webSocketHandler.sendMessage(exten, json);
        } catch (Exception e) {
            log.error("❌ [POPUP] 전송 중 치명적 오류 발생: {}", e.getMessage(), e);
        }
    }

    private void sendCtiEvent(String exten, String type, String channel, String recFile) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("type", type); data.put("exten", exten);
            if (channel != null) data.put("channel", channel);
            if (recFile != null) data.put("recordingFile", recFile + ".wav");
            webSocketHandler.sendMessage(exten, objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            log.error("❌ [CtiEvent] 전송 오류 (내선: {}, 타입: {}): {}", exten, type, e.getMessage(), e);
        }
    }

    private String extractNumberOnly(String s) {
        if (s == null) return null;
        Matcher m = Pattern.compile("(\\d{3,4})").matcher(s);
        return m.find() ? m.group(1) : null;
    }

    @PostConstruct
    public void init() {
        try {
            managerConnection.addEventListener(this);
            managerConnection.login();
            log.info("🚀 [AsteriskService] AMI 매니저 연결 및 이벤트 리스너 등록 완료");
        } catch (Exception e) {
            log.error("❌ [AsteriskService] AMI 초기 연결 실패: {}", e.getMessage());
            new Thread(() -> {
                while (true) {
                    try {
                        if (managerConnection.getState() != org.asteriskjava.manager.ManagerConnectionState.CONNECTED) {
                            managerConnection.addEventListener(this);
                            managerConnection.login();
                            log.info("🚀 [AsteriskService] AMI 매니저 재연결 성공");
                            break;
                        }
                    } catch (Exception ex) {}
                    try { Thread.sleep(5000); } catch (InterruptedException ie) { break; }
                }
            }).start();
        }
    }
    @PreDestroy public void cleanup() { try { if (managerConnection != null) managerConnection.logoff(); } catch (Exception ex) {} }
}
