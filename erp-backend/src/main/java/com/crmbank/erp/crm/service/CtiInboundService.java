package com.crmbank.erp.crm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.crmbank.erp.crm.mapper.inbound.InboundMapper;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.action.RedirectAction;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CtiInboundService {

    private final ManagerConnection managerConnection;
    private final InboundMapper inboundMapper;

    /**
     * 💡 전화 돌려주기 (Transfer) 실행
     */
    public void transferCall(String fromExten, String targetExten, Map<String, Object> consultData, String linkedId, String myChannel, Set<String> sessionChannels, String cmpycd) {
        new Thread(() -> {
            try {
                if (linkedId != null) {
                    // 이관 한도 체크 (최대 2회)
                    Map<String, Object> existing = inboundMapper.selectTransferData(linkedId);
                    int currentCnt = (existing != null && existing.get("transfer_cnt") != null) ? Integer.parseInt(existing.get("transfer_cnt").toString()) : 0;
                    if (currentCnt >= 2) {
                        log.warn("🚫 [이관 차단] 내선 {} 이관 한도 초과", fromExten);
                        return;
                    }

                    consultData.put("linkedid", linkedId);
                    consultData.put("from_exten", fromExten);
                    consultData.put("to_exten", targetExten);
                    consultData.put("cmpycd", cmpycd);
                    inboundMapper.upsertTransferData(consultData);
                    
                    if (sessionChannels != null) {
                        String customerChannel = sessionChannels.stream().filter(c -> !c.equals(myChannel)).findFirst().orElse(null);
                        if (customerChannel != null) {
                            managerConnection.sendAction(new RedirectAction(customerChannel, "from-internal", targetExten, 1));
                            log.info("🚀 [INBOUND] 이관 실행: {} -> {}", fromExten, targetExten);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("❌ [INBOUND] 이관 실패: {}", e.getMessage());
            }
        }).start();
    }
}
