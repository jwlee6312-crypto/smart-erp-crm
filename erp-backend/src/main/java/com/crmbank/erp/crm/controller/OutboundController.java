package com.crmbank.erp.crm.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.crm.dto.*;
import com.crmbank.erp.crm.service.OutboundService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/crm/outbound")
@RequiredArgsConstructor
public class OutboundController {

    private final OutboundService outboundService;

    /**
     * 💡 [핵심] 모든 요청 파라미터를 소문자로 통합 관리 (CRM 소문자 원칙 적용)
     */
    private Map<String, Object> getParams(Map<String, Object> params, HttpSession session) {
        Map<String, Object> lowParams = new HashMap<>();
        if (params != null) {
            params.forEach((k, v) -> lowParams.put(k.toLowerCase(), v));
        }
        UserSession user = (UserSession) session.getAttribute("user_session");
        lowParams.put("cmpycd", user != null ? user.getCmpycd() : "");
        lowParams.put("userid", user != null ? user.getUserid() : "SYSTEM");
        return lowParams;
    }

    // 1. 공통 코드 및 캠페인 목록 조회
    @GetMapping("/code")
    public List<Map<String, Object>> getCrmCode(@RequestParam Map<String, Object> params, HttpSession session) {
        return outboundService.getCrmCodeList(getParams(params, session));
    }

    @GetMapping("/camp-list")
    public List<Map<String, Object>> getCampList(@RequestParam Map<String, Object> params, HttpSession session) {
        return outboundService.getCampList(getParams(params, session));
    }

    // 2. 캠페인 마스터 관리
    @PostMapping("/camp-save")
    public void saveCamp(@RequestBody CampMstDto dto, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        dto.setCmpycd(user != null ? user.getCmpycd() : "");
        dto.setUpdemp(user != null ? user.getUserid() : "SYSTEM");
        outboundService.saveCampMst(dto);
    }

    @PostMapping("/camp-delete")
    public void deleteCamp(@RequestBody Map<String, Object> params, HttpSession session) {
        outboundService.deleteCamp(getParams(params, session));
    }

    // 3. 질문 및 답변 관리 (HGOA030U)
    @GetMapping("/surv/mst/search")
    public List<Map<String, Object>> getSurvMst(@RequestParam Map<String, Object> params, HttpSession session) {
        return outboundService.getSurvMstList(getParams(params, session));
    }

    @GetMapping("/surv/dtl/search")
    public List<Map<String, Object>> getSurvDtl(@RequestParam Map<String, Object> params, HttpSession session) {
        return outboundService.getSurvDtlList(getParams(params, session));
    }

    @PostMapping("/surv/save")
    public void saveSurv(@RequestBody Map<String, Object> payload, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        outboundService.saveSurveyTransaction(payload,
                user != null ? user.getCmpycd() : "",
                user != null ? user.getUserid() : "SYSTEM");
    }

    @PostMapping("/surv/delete")
    public void deleteSurv(@RequestBody Map<String, Object> params, HttpSession session) {
        outboundService.removeSurvey(getParams(params, session));
    }

    // 4. 설문 유형 매핑 (HGOA040U)
    @GetMapping("/mapping/list")
    public List<Map<String, Object>> getMappingList(@RequestParam Map<String, Object> params, HttpSession session) {
        return outboundService.getMappedQuestions(getParams(params, session));
    }

    @PostMapping("/mapping/save")
    public void saveMapping(@RequestBody Map<String, Object> payload, HttpSession session) {
        outboundService.saveMapping(getParams(payload, session));
    }

    // 5. 캠페인 대상자 명단 (HGOA050U / HGOA100U)
    @GetMapping("/call-list")
    public List<Map<String, Object>> getCallList(@RequestParam Map<String, Object> params, HttpSession session) {
        return outboundService.getCampCallList(getParams(params, session));
    }

    @PostMapping("/call-list/save")
    public void saveCallList(@RequestBody List<Map<String, Object>> list, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        String cmpycd = user != null ? user.getCmpycd() : "";
        String userid = user != null ? user.getUserid() : "SYSTEM";
        
        // 서비스 레이어에 회사 코드와 사용자 ID를 함께 전달
        outboundService.saveCallListBatch(list, cmpycd, userid);
    }

    @PostMapping("/call-list/delete")
    public void deleteCallList(@RequestBody Map<String, Object> params, HttpSession session) {
        outboundService.removeCampCallListBatch(getParams(params, session));
    }

    // 6. 속성 매핑 (HGOA060U)
    @GetMapping("/attr-mapper/list")
    public List<CampAttrMapperDto> getAttrList(@RequestParam Map<String, Object> params, HttpSession session) {
        return outboundService.getAttrList(getParams(params, session));
    }

    @PostMapping("/attr-mapper/save")
    public void saveAttrMapper(@RequestBody Map<String, Object> payload, HttpSession session) {
        Map<String, Object> params = getParams(payload, session);
        List<Map<String, Object>> list = (List<Map<String, Object>>) payload.get("list");
        outboundService.saveAttrMapperBatch(params, list);
    }

    // 7. 통계 및 기타 정보
    @GetMapping("/surv-form")
    public List<Map<String, Object>> getSurvForm(@RequestParam Map<String, Object> params, HttpSession session) {
        return outboundService.getSurvFormList(getParams(params, session));
    }

    @GetMapping("/camp-surv-mst-list")
    public List<Map<String, Object>> getCampSurvMstList(@RequestParam Map<String, Object> params, HttpSession session) {
        return outboundService.getCampSurvMstList(getParams(params, session));
    }

    @GetMapping("/camp-stats")
    public Map<String, Object> getCampStats(@RequestParam Map<String, Object> params, HttpSession session) {
        return outboundService.getCampStats(getParams(params, session));
    }

    @GetMapping("/customer-history")
    public List<Map<String, Object>> getHistory(@RequestParam Map<String, Object> params, HttpSession session) {
        return outboundService.getCampaignHistory(getParams(params, session));
    }

    @GetMapping("/rslt-dtl")
    public List<Map<String, Object>> getRsltDtl(@RequestParam Map<String, Object> params, HttpSession session) {
        return outboundService.getCampaignRsltDtl(getParams(params, session));
    }

    /**
     * 💡 [최종] 캠페인 상담 통합 저장 (소문자 표준화 적용)
     * 프론트엔드 HGOA100U.vue에서 호출함
     */
    @PostMapping("/save-consolidated")
    public Map<String, Object> saveConsolidated(@RequestBody Map<String, Object> payload, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 모든 키를 소문자로 변환하여 서비스 레이어의 통합 저장 로직 호출
            outboundService.saveConsolidated(getParams(payload, session));
            result.put("success", true);
        } catch (Exception e) {
            log.error("통합 저장 처리 에러: {}", e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
