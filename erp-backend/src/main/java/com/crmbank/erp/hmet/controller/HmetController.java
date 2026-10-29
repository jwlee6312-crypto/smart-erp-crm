package com.crmbank.erp.hmet.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.crm.service.GeminiAiService;
import com.crmbank.erp.hmet.service.HmetService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hmet")
@RequiredArgsConstructor
public class HmetController {

    private final HmetService hmetService;
    private final GeminiAiService geminiAiService;

    private Map<String, Object> getParams(Map<String, Object> params, HttpSession session) {
        Map<String, Object> lowParams = new HashMap<>();
        if (params != null) {
            params.forEach((k, v) -> lowParams.put(k.toLowerCase(), v));
        }
        UserSession user = (UserSession) session.getAttribute("user_session");
        lowParams.put("cmpycd", user != null ? user.getCmpycd() : "COIT");
        lowParams.put("userid", user != null ? user.getUserid() : "SYSTEM");
        return lowParams;
    }

    @GetMapping("/master")
    public List<Map<String, Object>> getMaster(@RequestParam Map<String, Object> params, HttpSession session) {
        return hmetService.getMeetingMaster(getParams(params, session));
    }

    @GetMapping("/detail")
    public List<Map<String, Object>> getDetail(@RequestParam Map<String, Object> params, HttpSession session) {
        return hmetService.getMeetingAttendees(getParams(params, session));
    }

    @GetMapping("/files")
    public List<Map<String, Object>> getFiles(@RequestParam Map<String, Object> params, HttpSession session) {
        return hmetService.getMeetingFiles(getParams(params, session));
    }

    @PostMapping("/save")
    public String save(@RequestBody Map<String, Object> payload, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        @SuppressWarnings("unchecked")
        Map<String, Object> master = (Map<String, Object>) payload.get("master");
        if (master != null) {
            master.put("cmpycd", user != null ? user.getCmpycd() : "COIT");
            master.put("updemp", user != null ? user.getUserid() : "SYSTEM");
        }
        return hmetService.saveMeeting(payload);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody Map<String, Object> params, HttpSession session) {
        hmetService.deleteMeeting(getParams(params, session));
    }

    @PostMapping("/stt-simulate")
    public Map<String, Object> sttSimulate(@RequestBody Map<String, Object> payload) {
        // 녹취록 STT 자동 변환 및 AI 요약 테스트 시뮬레이션
        String audioName = String.valueOf(payload.getOrDefault("filename", "녹취파일.mp3"));
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("stt_text", "[자동 STT 변환 완료] \n- 일시: 2026년 10월 23일\n- 안건: 회의록 관리 시스템 구축 방안 논의\n- 주요발언: 1번 참석자 - 회의록 자동화 기능 필수, 2번 참석자 - STT 정확도 향상 필요.");
        result.put("summary", "[AI 회의 자동 요약]\n1. 목표: 회의록 작성 및 녹취 변환 자동화 시스템 구축\n2. 주요 결정사항: STT 변환 텍스트를 기반으로 핵심 요약 및 액션 아이템 추출 기능 도입\n3. 후속조치: 백엔드 API 연동 및 모바일 화면 개발 추진");
        return result;
    }

    @PostMapping("/stt-correct")
    public Map<String, Object> sttCorrect(@RequestBody Map<String, Object> payload) {
        String rawText = String.valueOf(payload.getOrDefault("stt_text", ""));
        Map<String, Object> result = new HashMap<>();
        try {
            String corrected = geminiAiService.correctSpelling(rawText);
            result.put("success", true);
            result.put("corrected_text", corrected);
        } catch (Exception e) {
            log.error("맞춤법 교정 실패: {}", e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
