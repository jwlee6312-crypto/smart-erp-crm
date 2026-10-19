package com.crmbank.erp.crm.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.crm.dto.ConsultSaveRequest;
import com.crmbank.erp.crm.service.ConsultSaveService;
import com.crmbank.erp.crm.service.OmniChatwootService;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/omni/chat")
@RequiredArgsConstructor
public class OmniChatController {

    private final OmniChatwootService omniChatwootService;
    private final ConsultSaveService consultSaveService; 

    @PostMapping("/inquiry")
    public Map<String, Object> sendInquiry(@RequestBody ChatRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            omniChatwootService.sendInquiry(request.getEmail(), request.getName(), request.getContent());
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
        }
        return result;
    }

    @PostMapping("/reply")
    public Map<String, Object> replyToCustomer(@RequestBody ChatRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            omniChatwootService.replyToCustomer(request.getEmail(), request.getContent());
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
        }
        return result;
    }

    @GetMapping("/messages")
    public List<Map> getMessages(@RequestParam String email) {
        return omniChatwootService.getMessages(email);
    }

    @PostMapping("/save-consolidated")
    public Map<String, Object> saveConsolidated(@RequestBody ConsultSaveRequest request, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 💡 세션에서 사용자 정보 추출하여 강제 설정 (보안 및 정확성)
            UserSession user = (UserSession) session.getAttribute("user_session");
            if (user != null) {
                request.setUserid(user.getUserid());
                request.setLine_num(user.getInner_no());
                request.setCmpycd(user.getCmpycd());
            }
            
            consultSaveService.saveOmniConsultation(request);
            result.put("success", true);
        } catch (Exception e) {
            log.error("Omni consultation save error: {}", e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @Data
    public static class ChatRequest {
        private String email;
        private String name;
        private String content;
    }
}
