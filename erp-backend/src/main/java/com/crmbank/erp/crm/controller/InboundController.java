package com.crmbank.erp.crm.controller;

import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.crmbank.erp.crm.dto.*;
import com.crmbank.erp.comm.dto.*;
import com.crmbank.erp.crm.service.*;
import com.crmbank.erp.crm.mapper.inbound.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/crm/inbound")
@RequiredArgsConstructor
public class InboundController {

    private final InboundService inboundService;
    private final GeminiAiService geminiAiService;
    private final InboundMapper inboundMapper;

    @Value("${asterisk.sounds.path}")
    private String soundsPath;

    @Value("${asterisk.recording.path}")
    private String recordingPath;

    @GetMapping("/play-recording")
    public ResponseEntity<ResourceRegion> playRecording(@RequestHeader HttpHeaders headers, @RequestParam(value = "file", required = false) String file) throws IOException {
        if (file == null || file.trim().isEmpty()) return ResponseEntity.notFound().build();
        
        // 🚀 [정밀 복구] 경로 탈취를 막고 오직 파일 이름만 떼어내어 지정된 soundsPath에서만 찾습니다.
        String fileNameOnly = new File(file.contains("?") ? file.substring(0, file.indexOf("?")) : file).getName();
        File targetFile = new File(soundsPath, fileNameOnly);
        
        // 🚀 [원본 로직] soundsPath에 없으면 recordingPath에서도 한 번 더 찾습니다. (상담원님 한 달 전 원본)
        if (!targetFile.exists()) targetFile = new File(recordingPath, fileNameOnly);

        if (!targetFile.exists()) {
            log.error("❌ [음원 파일 없음] 최종 탐색 경로: {}", targetFile.getAbsolutePath());
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(targetFile);
        long contentLength = resource.contentLength();
        List<HttpRange> ranges = headers.getRange();
        if (!ranges.isEmpty()) {
            HttpRange range = ranges.get(0);
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            return ResponseEntity.status(206).contentType(MediaType.parseMediaType("audio/wav")).body(new ResourceRegion(resource, start, Math.min(1024*1024L, end - start + 1)));
        }
        return ResponseEntity.status(206).contentType(MediaType.parseMediaType("audio/wav")).body(new ResourceRegion(resource, 0, Math.min(1024*1024L, contentLength)));
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> save(@RequestBody SaveRequest request, HttpSession session) {
        try {
            UserSession user = (UserSession) session.getAttribute("user_session");
            String cmpycd = user != null ? user.getCmpycd() : "";
            String userid = user != null ? user.getUserid() : "system";
            CallMstDto dto = request.getDto();
            dto.setCmpycd(cmpycd); dto.setConsultid(userid); dto.setUpdemp(userid);
            
            LocalDateTime now = LocalDateTime.now();
            dto.setEnd_time(now);
            String svcymd = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            String svcno = inboundService.saveCallMst(dto, request.getRecordings(), svcymd, user != null ? user.getDeptcd() : "");
            return ResponseEntity.ok(Map.of("success", true, "svcno", svcno));
        } catch (Exception e) { 
            return ResponseEntity.status(500).build(); 
        }
    }

    @PostMapping("/recording-list-pop")
    public ResponseEntity<List<Map<String, Object>>> getRecordingListPop(@RequestBody Map<String, Object> params) {
        File dir = new File(recordingPath);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".wav"));
        List<Map<String, Object>> result = new ArrayList<>();
        if (files != null) {
            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
            for (int i = 0; i < Math.min(files.length, 20); i++) {
                Map<String, Object> map = new HashMap<>();
                map.put("filename", files[i].getName());
                map.put("mtime", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(files[i].lastModified()), java.time.ZoneId.of("Asia/Seoul"))));
                result.add(map);
            }
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/analyze-audio")
    public ResponseEntity<Map<String, String>> analyzeAudio(@RequestParam(required = false) String file, HttpSession session) {
        if (file == null || file.isEmpty()) return ResponseEntity.notFound().build();
        UserSession user = (UserSession) session.getAttribute("user_session");
        String customerPhone = (user != null) ? user.getHpno() : "";
        return ResponseEntity.ok(geminiAiService.analyzeAudio(new File(recordingPath, file).getAbsolutePath(), customerPhone));
    }

    @GetMapping("/status-list")
    public List<Map<String, Object>> getStatusList(@RequestParam String fromdt, @RequestParam String todt, @RequestParam(required = false) String custnm, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        return inboundService.getStatusList(user != null ? user.getCmpycd() : "", fromdt, todt, custnm);
    }

    @GetMapping("/customer-detail")
    public Map<String, Object> getCustomerDetail(@RequestParam String custcd, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        return inboundService.getCustomerByCustCd(user != null ? user.getCmpycd() : "", custcd);
    }

    @Data public static class SaveRequest { private CallMstDto dto; private List<String> recordings; private String ai_mode; }

    // 1. 콜백 목록 조회 API
    @GetMapping("/callback-list")
    public ResponseEntity<?> getCallbackList(
            @RequestParam String fromdt,
            @RequestParam String todt,
            @RequestParam(required = false) String src_no,
            HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        Map<String, Object> params = new HashMap<>();
        params.put("cmpycd", user != null ? user.getCmpycd() : "");
        params.put("fromdt", fromdt.replace("-", ""));
        params.put("todt", todt.replace("-", ""));
        params.put("src_no", src_no != null ? src_no : "");
        return ResponseEntity.ok(inboundMapper.selectCallbackList(params));
    }

    // 2. 콜백 응대 결과 저장 API
    @PostMapping("/interaction/save-response")
    public ResponseEntity<?> saveCallbackResponse(@RequestBody Map<String, Object> payload, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        payload.put("cmpycd", user != null ? user.getCmpycd() : "");
        payload.put("userid", user != null ? user.getUserid() : "system");
        inboundMapper.updateCallbackResult(payload);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
