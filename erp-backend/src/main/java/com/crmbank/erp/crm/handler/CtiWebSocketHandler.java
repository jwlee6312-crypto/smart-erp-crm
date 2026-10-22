package com.crmbank.erp.crm.handler;

import com.crmbank.erp.comm.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class CtiWebSocketHandler extends TextWebSocketHandler {

    // 💡 내선번호당 여러 개의 세션(메인 창, 팝업 창 등)을 허용하도록 수정
    private static final Map<String, List<WebSocketSession>> SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            String exten = getExtension(session);

            if (exten != null && !exten.trim().isEmpty()) {
                String cleanExt = exten.trim(); // 🚀 [추가] 저장 시 공백 제거
                SESSIONS.computeIfAbsent(cleanExt, k -> new CopyOnWriteArrayList<>()).add(session);
                log.info("🎯 [CTI WS] 상담원 연결 완료: 내선번호 [{}], ID: [{}], 총 세션: {}",
                        cleanExt, session.getId(), SESSIONS.get(cleanExt).size());
            } else {
                log.warn("⚠️ [CTI WS] 내선번호 없이 연결됨: ID: [{}]. 팝업 수신이 불가능합니다.", session.getId());
            }
        } catch (Exception e) {
            log.error("❌ [CTI WS] 연결 처리 중 오류 발생: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String exten = getExtension(session);
        if (exten != null) {
            List<WebSocketSession> sessions = SESSIONS.get(exten);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    SESSIONS.remove(exten);
                }
                log.info("🏠 [CTI WS] 상담원 세션 종료: [{}], 남은 세션 수: {}", exten, sessions.size());
            }
        }
    }

    private String getExtension(WebSocketSession session) {
        Map<String, Object> attributes = session.getAttributes();
        String exten = (String) attributes.get("inner_no");
        if (exten == null) {
            String query = session.getUri().getQuery();
            if (query != null && query.contains("exten=")) {
                exten = query.split("exten=")[1].split("&")[0];
            }
        }
        return exten;
    }

    public boolean isSessionActive(String exten) {
        List<WebSocketSession> sessions = SESSIONS.get(exten);
        return sessions != null && !sessions.isEmpty() && sessions.stream().anyMatch(WebSocketSession::isOpen);
    }

    public List<String> getActiveExtens() {
        return new ArrayList<>(SESSIONS.keySet());
    }

    // 💡 [추가] 특정 내선의 세션에서 회사코드 추출
    public String getCmpycd(String exten) {
        List<WebSocketSession> sessions = SESSIONS.get(exten);
        if (sessions != null && !sessions.isEmpty()) {
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    Map<String, Object> attrs = session.getAttributes();
                    Object userObj = attrs.get("user_session");
                    if (userObj instanceof UserSession) {
                        String cmpycd = ((UserSession) userObj).getCmpycd();
                        return cmpycd != null ? cmpycd.trim() : "";
                    }
                }
            }
        }
        return "";
    }

    public void sendMessage(String exten, String message) {
        if (exten == null) return;
        String cleanExt = exten.trim(); // 💡 공백 제거로 정확한 매칭 유도
        log.info("🔍 [CTI WS] 메시지 전송 요청 내선 [{}]. 현재 서버에 등록된 모든 세션 내선 목록: {}", cleanExt, SESSIONS.keySet());
        List<WebSocketSession> sessions = SESSIONS.get(cleanExt);

        if (sessions == null || sessions.isEmpty()) {
            log.warn("⚠️ [CTI WS] 내선 [{}] 세션 없음. 접속 중인 모든 활성 세션으로 브로드캐스트 전환합니다. (등록된 내선: {})", cleanExt, SESSIONS.keySet());
            sessions = new ArrayList<>();
            for (List<WebSocketSession> sList : SESSIONS.values()) {
                if (sList != null) {
                    sessions.addAll(sList);
                }
            }
        }

        if (!sessions.isEmpty()) {
            log.info("🚀 [CTI WS] 메시지 전송 시작 -> 대상 세션수: {}", sessions.size());
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(message));
                        log.info("✅ [CTI WS] 메시지 전송 성공 -> 세션 ID: {}", session.getId());
                    } catch (IOException e) {
                        log.error("❌ [CTI WS] 전송 실패: {}", e.getMessage());
                    }
                } else {
                    log.warn("⚠️ [CTI WS] 세션이 닫혀있음");
                }
            }
        } else {
            log.warn("⚠️ [CTI WS] 전송 실패: 현재 서버에 등록된 활성 세션이 전혀 없습니다.");
        }
    }
}