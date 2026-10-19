package com.crmbank.erp.crm.controller;

import com.crmbank.erp.comm.dto.ApiResponse;
import com.crmbank.erp.crm.dto.ConsultSaveRequest;
import com.crmbank.erp.crm.service.ChatwootService;
import com.crmbank.erp.crm.service.ConsultSaveService;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/common/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatwootService chatwootService;
    private final ConsultSaveService consultSaveService; 

    @PostMapping("/inquiry")
    public ResponseEntity<ApiResponse<Void>> sendInquiry(@RequestBody ChatRequest request) {
        try {
            chatwootService.sendInquiry(request.getEmail(), request.getName(), request.getContent());
            return ResponseEntity.ok(ApiResponse.success(null, "Inquiry Sent"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.serverError("Inquiry Failed"));
        }
    }

    @PostMapping("/reply")
    public ResponseEntity<ApiResponse<Void>> replyToCustomer(@RequestBody ChatRequest request) {
        try {
            chatwootService.replyToCustomer(request.getEmail(), request.getContent());
            return ResponseEntity.ok(ApiResponse.success(null, "Reply Sent"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.serverError("Reply Failed"));
        }
    }

    @GetMapping("/messages")
    public ResponseEntity<ApiResponse<List<Map>>> getMessages(@RequestParam(name = "email") String email) {
        try {
            // 💡 TRACE 레벨로 변경하여 폴링 로그 노이즈 제거
            log.trace("[Polling] Email: {}", email);
            return ResponseEntity.ok(ApiResponse.success(chatwootService.getMessages(email), "Fetched"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.serverError("Fetch Failed"));
        }
    }

    @PostMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearHistory(@RequestBody ChatRequest request) {
        try {
            chatwootService.clearHistory(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success(null, "Cleared"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.serverError("Clear Failed"));
        }
    }

    @PostMapping("/save-consolidated")
    public ResponseEntity<ApiResponse<Void>> saveConsolidated(@RequestBody ConsultSaveRequest request, HttpSession session) {
        try {
            // 💡 세션의 cmpycd를 DTO에 수동 매핑 (하드코딩 방지)
            String sessionCmpycd = (String) session.getAttribute("cmpycd");
            if (sessionCmpycd != null) request.setCmpycd(sessionCmpycd);
            
            consultSaveService.saveOmniConsultation(request);
            return ResponseEntity.ok(ApiResponse.success(null, "Saved"));
        } catch (Exception e) {
            log.error("Save Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.serverError("Save Failed"));
        }
    }

    @Data
    public static class ChatRequest {
        private String email;
        private String name;
        private String content;
        public void setEMAIL(String email) { this.email = email; }
        public void setCONTENT(String content) { this.content = content; }
        public void setNAME(String name) { this.name = name; }
    }
}
