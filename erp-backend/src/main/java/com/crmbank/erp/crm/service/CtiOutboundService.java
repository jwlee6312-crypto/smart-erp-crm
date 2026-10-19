package com.crmbank.erp.crm.service;

import com.crmbank.erp.crm.dto.TotalCallLogDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.action.OriginateAction;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CtiOutboundService {

    private final ManagerConnection managerConnection;
    private final InboundService inboundService;

    /**
     * 💡 아웃바운드 발신 실행 (세션 기반 회사코드 반영)
     */
    public String makeCall(String exten, String dest, String context, String cmpycd) {
        final String targetContext = (context == null || context.isEmpty()) ? "outbound-call" : context;
        final String finalCmpycd = (cmpycd == null || cmpycd.isEmpty()) ? "COIT" : cmpycd;
        
        // 1. 백엔드에서 표준 파일명 생성
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String recFileName = String.format("%s-%s-%s", timestamp, exten, dest);

        new Thread(() -> {
            try {
                OriginateAction action = new OriginateAction();
                action.setChannel("SIP/" + exten); 
                action.setContext(targetContext);    
                action.setExten(dest);               
                action.setPriority(1);
                action.setCallerId("CRM OUT <" + exten + ">");
                action.setTimeout(30000L);
                action.setAsync(true);

                Map<String, String> variables = new HashMap<>();
                variables.put("REC_FILE", recFileName);
                action.setVariables(variables);

                if (managerConnection.getState().toString().equals("CONNECTED")) {
                    managerConnection.sendAction(action);
                    log.info("🚀 [OUTBOUND] 발신 요청: {} -> {} [Company: {}]", exten, dest, finalCmpycd);
                    
                    // 🚀 [기록] 세션에서 받은 진짜 회사코드로 기록
                    inboundService.insertTotalInteractionLog(TotalCallLogDto.builder()
                            .uniqueid("OUT_" + UUID.randomUUID().toString().substring(0, 8))
                            .cmpycd(finalCmpycd)
                            .media_type("call")
                            .direction("out")
                            .src_no(exten)
                            .dst_no(dest)
                            .keyword(dest)
                            .start_time(LocalDateTime.now())
                            .result_cd("100") 
                            .rec_file(recFileName + ".wav")
                            .build());
                }
            } catch (Exception ex) {
                log.error("❌ [OUTBOUND] 오류: {}", ex.getMessage());
            }
        }).start();

        return recFileName + ".wav"; 
    }

    public String makeCall(String exten, String dest) {
        return makeCall(exten, dest, "outbound-call", "COIT");
    }
}
