package com.crmbank.erp.crm.service;

import com.crmbank.erp.crm.dto.CampRsltDtlDto;
import com.crmbank.erp.crm.dto.CampRsltMstDto;
import com.crmbank.erp.crm.dto.ConsultSaveRequest;
import com.crmbank.erp.crm.mapper.outbound.OutboundMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultSaveService {

    private final OutboundMapper outboundMapper;
    private final GeminiAiService geminiAiService;

    @Transactional
    public void saveOmniConsultation(ConsultSaveRequest request) {
        String interactionId = (request.getMedia_type() != null && "call".equalsIgnoreCase(request.getMedia_type()))
                ? "IN_" + UUID.randomUUID().toString().substring(0, 8)
                : "CHAT_" + UUID.randomUUID().toString().substring(0, 8);
        
        // 🤖 통합 AI 분석 및 요약 적용
        Map<String, String> aiResult = geminiAiService.analyze(request.getChat_history(), request.getRec_file());
        String chatLog = aiResult.get("stt");
        String aiSummary = aiResult.get("summary");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDtime = null;
        LocalDateTime endDtime = null;
        try {
            if (request.getStart_time() != null) {
                String st = request.getStart_time();
                if (st.length() == 10) st += " 00:00:00";
                startDtime = LocalDateTime.parse(st, formatter);
            }
            if (request.getEnd_time() != null) {
                String et = request.getEnd_time();
                if (et.length() == 10) et += " 23:59:59";
                endDtime = LocalDateTime.parse(et, formatter);
            }
        } catch (Exception e) {
            log.warn("Time parsing failed: {}", e.getMessage());
        }

        if (startDtime == null) startDtime = LocalDateTime.now().minusMinutes(5);
        if (endDtime == null) endDtime = LocalDateTime.now();

        // 💡 [교정] 하드코딩 제거 및 소문자 표준화
        String cmpycd = (request.getCmpycd() != null && !request.getCmpycd().isEmpty()) ? request.getCmpycd() : "";
        String mediaType = (request.getMedia_type() != null && !request.getMedia_type().isEmpty()) ? request.getMedia_type() : "chat";

        CampRsltMstDto mstDto = CampRsltMstDto.builder()
                .cmpycd(cmpycd)
                .call_seq(request.getCall_seq())
                .camp_no(request.getCamp_no())
                .rslt_cd(request.getRslt_cd())
                .remark(request.getMemo())
                .userid(request.getUserid())
                .line_num(request.getLine_num())   // 내선번호
                .call_telno(request.getCall_telno()) // 고객연락처
                .interaction_id(interactionId)
                .media_type(mediaType)
                .chat_log(chatLog)
                .ai_summary(aiSummary)
                .rec_file(request.getRec_file())
                .start_dtime(startDtime)
                .end_dtime(endDtime)
                .updemp(request.getUserid())
                .addtime(LocalDateTime.now())
                .updtime(LocalDateTime.now())
                .build();
        
        outboundMapper.insertCampaignRsltMst(mstDto);

        if (request.getSurveys() != null) {
            for (Map<String, Object> survey : request.getSurveys()) {
                String survNo = (String) survey.get("surv_no");
                String ansNo = survey.get("ans_no") != null ? (String) survey.get("ans_no") : "001";

                Object pointObj = survey.get("point");
                BigDecimal point = BigDecimal.ZERO;
                if (pointObj != null) {
                    try {
                        point = new BigDecimal(pointObj.toString());
                    } catch (Exception e) {
                        log.warn("Point parsing failed for value: {}", pointObj);
                    }
                }

                CampRsltDtlDto dtlDto = CampRsltDtlDto.builder()
                        .cmpycd(cmpycd)
                        .rslt_no(mstDto.getRslt_no())
                        .surv_no(survNo)
                        .ans_no(ansNo)
                        .point(point) 
                        .remark((String) survey.get("essay"))
                        .updemp(request.getUserid())
                        .addtime(LocalDateTime.now())
                        .updtime(LocalDateTime.now())
                        .build();
                outboundMapper.insertCampaignRsltDtl(dtlDto);
            }
        }

        // 💡 [교정] 파라미터 키 소문자 표준화
        Map<String, Object> statusParam = new HashMap<>();
        statusParam.put("cmpycd", cmpycd);
        statusParam.put("call_seq", request.getCall_seq());
        statusParam.put("status", "030");
        statusParam.put("rslt_cd", request.getRslt_cd());
        statusParam.put("resv_dtime", request.getResv_dtime());
        statusParam.put("resv_memo", request.getResv_memo());
        outboundMapper.updateCallListStatus(statusParam);
    }
}
