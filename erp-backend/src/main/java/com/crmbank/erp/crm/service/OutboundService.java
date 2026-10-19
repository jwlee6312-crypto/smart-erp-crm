package com.crmbank.erp.crm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.crmbank.erp.crm.dto.*;
import com.crmbank.erp.crm.mapper.outbound.OutboundMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundService {

    private final OutboundMapper outboundMapper;
    private final ObjectMapper objectMapper;
    private final GeminiAiService geminiAiService;

    // 1. 공통 코드 및 캠페인 조회
    public List<Map<String, Object>> getCrmCodeList(Map<String, Object> params) {
        return outboundMapper.selectCrmCodeList(params);
    }

    public List<Map<String, Object>> getCampList(Map<String, Object> params) {
        return outboundMapper.selectCampList(params);
    }

    // 2. 캠페인 마스터 관리
    @Transactional
    public void saveCampMst(CampMstDto dto) {
        if (dto.getCamp_no() == null || dto.getCamp_no().trim().isEmpty()) {
            Map<String, Object> p = new HashMap<>();
            p.put("cmpycd", dto.getCmpycd());
            dto.setCamp_no(outboundMapper.generateCampNo(p));
            outboundMapper.insertCampMst(dto);
        } else {
            outboundMapper.updateCampMst(dto);
        }
    }

    @Transactional
    public void deleteCamp(Map<String, Object> params) {
        outboundMapper.deleteCampMst(params);
    }

    // 3. 질문 및 답변 관리
    public List<Map<String, Object>> getSurvMstList(Map<String, Object> params) {
        return outboundMapper.selectSurvMstList(params);
    }

    public List<Map<String, Object>> getCampSurvMstList(Map<String, Object> params) {
        return outboundMapper.selectCampSurvMstList(params);
    }

    public List<Map<String, Object>> getSurvDtlList(Map<String, Object> params) {
        return outboundMapper.selectSurvDtlList(params);
    }

    @Transactional
    public void saveSurveyTransaction(Map<String, Object> payload, String cmpycd, String userid) {
        Map<String, Object> mstMap = (Map<String, Object>) payload.get("mst");
        List<Map<String, Object>> dtlList = (List<Map<String, Object>>) payload.get("dtl");

        if (mstMap == null) return;

        String survNo = (String) mstMap.get("surv_no");
        SurvMstDto mstDto = objectMapper.convertValue(mstMap, SurvMstDto.class);
        mstDto.setCmpycd(cmpycd);
        mstDto.setUpdemp(userid);

        if (survNo == null || survNo.isEmpty()) {
            survNo = outboundMapper.generateSurvNo(Map.of("cmpycd", cmpycd));
            mstDto.setSurv_no(survNo);
            outboundMapper.insertSurvMst(mstDto);
        } else {
            outboundMapper.updateSurvMst(mstDto);
        }

        outboundMapper.deleteSurvDtlAll(Map.of("cmpycd", cmpycd, "surv_no", survNo));
        if (dtlList != null) {
            for (Map<String, Object> dtl : dtlList) {
                SurvDtlDto dtlDto = objectMapper.convertValue(dtl, SurvDtlDto.class);
                dtlDto.setCmpycd(cmpycd);
                dtlDto.setSurv_no(survNo);
                dtlDto.setUpdemp(userid);
                outboundMapper.insertSurvDtl(dtlDto);
            }
        }
    }

    @Transactional
    public void removeSurvey(Map<String, Object> params) {
        outboundMapper.deleteSurvDtlAll(params);
        outboundMapper.deleteSurvMst(params);
    }

    // 4. 설문 유형 매핑
    public List<Map<String, Object>> getMappedQuestions(Map<String, Object> params) {
        return outboundMapper.selectMappedQuestions(params);
    }

    @Transactional
    public void saveMapping(Map<String, Object> payload) {
        outboundMapper.deleteCampSurveyAll(payload);
        List<Map<String, Object>> questions = (List<Map<String, Object>>) payload.get("questions");
        if (questions != null) {
            for (Map<String, Object> q : questions) {
                Map<String, Object> p = new HashMap<>(payload);
                p.put("surv_no", q.get("surv_no"));
                p.put("sortcd", q.get("sortcd"));
                p.put("useyn", "y");
                outboundMapper.insertCampSurvey(p);
            }
        }
    }

    // 5. 캠페인 대상자(콜 리스트) 관리
    public List<Map<String, Object>> getCampCallList(Map<String, Object> params) {
        return outboundMapper.selectCampCallList(params);
    }

    @Transactional
    public void saveCallListBatch(List<Map<String, Object>> list, String cmpycd, String userid) {
        if (list == null || list.isEmpty()) return;
        
        if (cmpycd == null || cmpycd.trim().isEmpty()) {
            throw new IllegalArgumentException("회사 코드(cmpycd)는 필수 입력 항목입니다.");
        }

        // 1. 자동 매핑 정보 동기화 (첫 번째 행 기준)
        Map<String, Object> first = list.get(0);
        String survGb = (String) first.get("surv_gb");
        Map<String, Object> extData = (Map<String, Object>) first.get("ext_data");
        if (extData != null && !extData.isEmpty()) {
            syncCampAttrMapper(cmpycd, survGb, extData.keySet(), userid);
        }

        // 2. 단건씩 루프를 돌며 저장 (안정성 확보)
        for (Map<String, Object> item : list) {
            item.put("cmpycd", cmpycd);
            item.put("updemp", userid);
            if (item.get("userid") == null) item.put("userid", userid);
            
            if (item.get("camp_no") == null || String.valueOf(item.get("camp_no")).trim().isEmpty()) {
                throw new IllegalArgumentException("캠페인 번호(camp_no)가 누락된 항목이 있습니다.");
            }
            
            // 확실한 단건 인서트 호출
            outboundMapper.insertCampCallList(item);
        }
        log.info("캠페인 대상자 {}건 저장 완료 (cmpycd: {})", list.size(), cmpycd);
    }

    /**
     * 💡 엑셀의 새로운 헤더를 시스템 매핑 정보로 자동 등록
     */
    private void syncCampAttrMapper(String cmpycd, String survGb, Set<String> keys, String userid) {
        Map<String, Object> p = new HashMap<>();
        p.put("cmpycd", cmpycd);
        p.put("surv_gb", survGb);
        
        List<CampAttrMapperDto> existing = outboundMapper.selectAttrMapperList(p);
        Set<String> existingKeys = new HashSet<>();
        if (existing != null) {
            existing.forEach(e -> existingKeys.add(e.getAttr_key()));
        }

        for (String key : keys) {
            if (!existingKeys.contains(key)) {
                CampAttrMapperDto dto = new CampAttrMapperDto();
                dto.setCmpycd(cmpycd);
                dto.setSurv_gb(survGb);
                dto.setAttr_nm(key);   // 화면 표시명: 엑셀 헤더 그대로
                dto.setAttr_key(key);  // 데이터 키: 엑셀 헤더 그대로
                dto.setData_type("STRING");
                dto.setStats_yn("N");  // 💡 [변경] 자동 등록 시 통계 활용은 기본 'N'으로 (관리자가 사후 검토)
                dto.setUseyn("Y");
                dto.setUpdemp(userid != null ? userid : "SYSTEM");
                outboundMapper.insertAttrMapper(dto);
                log.info("새로운 캠페인 속성 자동 등록: {} ({})", key, survGb);
            }
        }
    }

    @Transactional
    public void removeCampCallListBatch(Map<String, Object> params) {
        outboundMapper.deleteCampCallListBatch(params);
    }

    // 6. 속성 매핑 관리
    public List<CampAttrMapperDto> getAttrList(Map<String, Object> params) {
        return outboundMapper.selectAttrMapperList(params);
    }

    @Transactional
    public void saveAttrMapperBatch(Map<String, Object> params, List<Map<String, Object>> list) {
        outboundMapper.deleteAttrMapperAll(params);
        if (list != null) {
            for (Map<String, Object> item : list) {
                CampAttrMapperDto dto = objectMapper.convertValue(item, CampAttrMapperDto.class);
                dto.setCmpycd((String) params.get("cmpycd"));
                dto.setSurv_gb((String) params.get("surv_gb"));
                outboundMapper.insertAttrMapper(dto);
            }
        }
    }

    // 7. 상담 현황 및 이력
    public Map<String, Object> getCampStats(Map<String, Object> params) {
        Map<String, Object> stats = outboundMapper.selectCampCallStats(params);
        if (stats == null) stats = new HashMap<>();
        stats.put("details", outboundMapper.selectCampDetailStats(params));
        return stats;
    }

    public List<Map<String, Object>> getSurvFormList(Map<String, Object> params) {
        return outboundMapper.selectSurvFormList(params);
    }

    public List<Map<String, Object>> getCampaignHistory(Map<String, Object> params) {
        return outboundMapper.selectCampaignHistory(params);
    }

    public List<Map<String, Object>> getCampaignRsltDtl(Map<String, Object> params) {
        return outboundMapper.selectCampaignRsltDtl(params);
    }

    /**
     * 💡 [최종] 캠페인 상담 통합 저장 (소문자 표준화 적용)
     */
    @Transactional
    public void saveConsolidated(Map<String, Object> payload) {
        String cmpycd = (String) payload.get("cmpycd");
        String campNo = (String) payload.get("camp_no");
        Object callSeqObj = payload.get("call_seq");
        Integer callSeq = callSeqObj instanceof Integer ? (Integer) callSeqObj : Integer.parseInt(String.valueOf(callSeqObj));
        String rsltCd = (String) payload.get("rslt_cd");
        String remark = (String) (payload.get("memo") != null ? payload.get("memo") : payload.get("remark"));
        String userid = (String) payload.get("userid");
        String recFile = (String) payload.get("rec_file");
        String chatHistory = (String) payload.get("chat_history");
        String mediaType = (String) payload.get("media_type");
        String lineNum = (String) payload.get("line_num");

        // 시간 파싱 (String -> LocalDateTime)
        String startTimeStr = (String) payload.get("start_time");
        String endTimeStr = (String) payload.get("end_time");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        LocalDateTime startDtime = (startTimeStr != null && !startTimeStr.isEmpty()) ? LocalDateTime.parse(startTimeStr, formatter) : LocalDateTime.now();
        LocalDateTime endDtime = (endTimeStr != null && !endTimeStr.isEmpty()) ? LocalDateTime.parse(endTimeStr, formatter) : LocalDateTime.now();

        // 1. 고객 정보 조회 (키워드 추출용)
        Map<String, Object> q = new HashMap<>();
        q.put("cmpycd", cmpycd); q.put("camp_no", campNo); q.put("call_seq", callSeq);
        List<Map<String, Object>> customers = outboundMapper.selectCampCallList(q);
        Map<String, Object> customer = (customers != null && !customers.isEmpty()) ? customers.get(0) : new HashMap<>();
        String custTel = (String) customer.get("tel_no");
        String custEmail = (String) customer.get("email");

        String interactionId = "out_" + UUID.randomUUID().toString().substring(0, 8);

        // 2. 통합 AI 분석 및 요약 적용
        String fullPath = (recFile != null && !recFile.isEmpty()) ? "/var/spool/asterisk/monitor/" + recFile : null;
        Map<String, String> aiResult = geminiAiService.analyze(chatHistory, fullPath);
        
        String chatLog = aiResult.get("stt");
        String aiSummary = aiResult.get("summary");

        // 3. 상담 결과 마스터 저장 (소문자 DTO 필드 매핑)
        CampRsltMstDto mstDto = CampRsltMstDto.builder()
                .cmpycd(cmpycd).camp_no(campNo).call_seq(callSeq).rslt_cd(rsltCd)
                .interaction_id(interactionId).remark(remark).userid(userid)
                .chat_log(chatLog).ai_summary(aiSummary).rec_file(recFile)
                .start_dtime(startDtime).end_dtime(endDtime).updemp(userid)
                .line_num(lineNum)
                .build();
        outboundMapper.insertCampaignRsltMst(mstDto);

        // 4. 설문 답변 저장
        List<Map<String, Object>> surveys = (List<Map<String, Object>>) payload.get("surveys");
        if (surveys != null) {
            for (Map<String, Object> s : surveys) {
                CampRsltDtlDto dtlDto = CampRsltDtlDto.builder()
                        .cmpycd(cmpycd).rslt_no(mstDto.getRslt_no()).surv_no((String) s.get("surv_no"))
                        .ans_no((String) s.get("ans_no")).remark((String) s.get("remark"))
                        .point(s.get("point") != null ? new java.math.BigDecimal(s.get("point").toString()) : java.math.BigDecimal.ZERO)
                        .updemp(userid).build();
                outboundMapper.insertCampaignRsltDtl(dtlDto);
            }
        }

        // 5. 통합 히스토리 로그 저장
        Map<String, Object> interLog = new HashMap<>();
        interLog.put("interaction_id", interactionId);
        interLog.put("cmpycd", cmpycd);
        interLog.put("direction", "out");
        interLog.put("userid", userid);
        interLog.put("result_cd", rsltCd);
        interLog.put("rec_file", recFile);
        interLog.put("start_time", startDtime);
        interLog.put("end_time", endDtime);

        if ("chat".equalsIgnoreCase(mediaType)) {
            interLog.put("media_type", "chat");
            interLog.put("keyword", custEmail);
            interLog.put("src_no", "system");
            interLog.put("dst_no", custEmail);
        } else {
            interLog.put("media_type", "call");
            interLog.put("keyword", custTel);
            interLog.put("src_no", userid);
            interLog.put("dst_no", custTel);
        }
        outboundMapper.insertTotalInteractionLog(interLog);

        // 6. 발신 리스트 상태 업데이트
        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("cmpycd", cmpycd);
        updateParams.put("call_seq", callSeq);
        updateParams.put("rslt_cd", rsltCd);
        updateParams.put("status", "030");
        outboundMapper.updateCallListStatus(updateParams);
    }
}
