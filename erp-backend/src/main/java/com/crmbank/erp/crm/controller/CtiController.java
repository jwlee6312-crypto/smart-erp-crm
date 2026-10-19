package com.crmbank.erp.crm.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.crm.service.AsteriskService;
import com.crmbank.erp.crm.service.CtiInboundService;
import com.crmbank.erp.crm.service.CtiOutboundService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/crm/cti")
@RequiredArgsConstructor
public class CtiController {

    private final AsteriskService asteriskService;
    private final CtiOutboundService ctiOutboundService;
    private final CtiInboundService ctiInboundService;

    @GetMapping("/answer")
    public Map<String, Object> answerCall(@RequestParam String exten) {
        log.info("📥 [API] 전화받기 요청 -> 내선: {}", exten);
        asteriskService.answerCall(exten);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    @GetMapping("/hangup")
    public Map<String, Object> hangupCall(@RequestParam String exten, @RequestParam(required = false) String channel) {
        log.info("📥 [API] 전화끊기 요청 -> 내선: {}", exten);
        asteriskService.hangupCall(exten, channel);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    @GetMapping("/transfer")
    public Map<String, Object> transferCall(
            @RequestParam String exten,
            @RequestParam String target,
            @RequestParam Map<String, Object> params,
            HttpSession session) {

        // 💡 [소문자 표준화] session key: "user_session"
        UserSession user = (UserSession) session.getAttribute("user_session");
        String cmpycd = user != null ? user.getCmpycd() : "haionnet";

        log.info("📥 [API] 돌려주기 요청 -> {} to {} (Company: {})", exten, target, cmpycd);

        String linkedId = asteriskService.getExtenLinkedId().get(exten);
        String myChannel = asteriskService.getExtenChannelMap().get(exten);

        ctiInboundService.transferCall(
                exten,
                target,
                params,
                linkedId,
                myChannel,
                linkedId != null ? asteriskService.getSessionChannels().get(linkedId) : null,
                cmpycd
        );

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    @GetMapping("/status")
    public Map<String, Object> checkStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("online", asteriskService.checkAmiConnection());
        return result;
    }

    @GetMapping("/active-agents")
    public java.util.List<String> getActiveAgents() {
        return asteriskService.getRegisteredExtens();
    }

    /**
     * 💡 실제 아웃바운드 발신 실행 (세션 기반 회사코드 연동)
     */
    @GetMapping("/make-call")
    public Map<String, Object> makeCall(
            @RequestParam String exten, 
            @RequestParam String dest, 
            @RequestParam(required = false) String context,
            HttpSession session) {
        
        // 🚀 [해결] 세션에서 유저 정보와 회사코드를 꺼내옴
        UserSession user = (UserSession) session.getAttribute("user_session");
        String cmpycd = (user != null) ? user.getCmpycd() : "COIT";

        log.info("🚀 [API] 아웃바운드 발신 요청: {} -> {} (Company: {})", exten, dest, cmpycd);
        
        String recFile = ctiOutboundService.makeCall(exten, dest, context, cmpycd);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("recFile", recFile);
        return result;
    }

    /**
     * 💡 녹취 파일 직접 재생 API
     */
    @GetMapping("/play-recording")
    public ResponseEntity<Resource> playRecording(@RequestParam String file) {
        String path = "/var/spool/asterisk/monitor/" + file;
        File audioFile = new File(path);
        
        if (!audioFile.exists()) {
            log.warn("⚠️ 녹취 파일 없음: {}", path);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/wav"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file + "\"")
                .body(new FileSystemResource(audioFile));
    }
}
