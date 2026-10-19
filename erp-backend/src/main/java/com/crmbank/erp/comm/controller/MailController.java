package com.crmbank.erp.comm.controller;

import com.crmbank.erp.comm.config.AesConfig;
import com.crmbank.erp.comm.util.SecurityUtil;
import com.crmbank.erp.comm.dto.ApiResponse;
import com.crmbank.erp.comm.dto.EmailDto;
import com.crmbank.erp.comm.dto.EmailSendHistoryDto;
import com.crmbank.erp.comm.service.EmailService;
import com.crmbank.erp.crm.service.ChatwootService;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 메일 발송 통합 컨트롤러 (공통)
 * 발주서, 거래명세서, 상담 초대 기능 통합
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mail")
public class MailController {

    private final EmailService emailService;
    private final ChatwootService chatwootService; // 💡 Added for session clearing
    private final AesConfig aesConfig;

    private String getDecryptedEmail(Object emailObj) {
        if (emailObj == null) return null;
        String email = emailObj.toString();
        if (email.contains("@")) return email;
        try {
            return SecurityUtil.decryptAes(email, aesConfig.getAesKey());
        } catch (Exception e) {
            return email;
        }
    }

    @PostMapping("/send-invite")
    public ResponseEntity<ApiResponse<String>> sendInvite(@RequestBody InviteRequest request, HttpSession session) throws Exception {
        String fromEmail = getDecryptedEmail(session.getAttribute("email"));
        String cmpycd = (String) session.getAttribute("cmpycd");
        String userid = (String) session.getAttribute("userid");

        log.info("📧 [MailController] send-invite request: {} -> {}", fromEmail, request.getToEmail());

        // 💡 [Requirement] Clear previous chat history before sending a new invitation
        // This ensures the customer starts with a fresh conversation ID and empty chat window.
        try {
            chatwootService.clearHistory(request.getToEmail());
        } catch (Exception e) {
            log.warn("⚠️ [MailController] Failed to clear chat history: {}", e.getMessage());
        }

        emailService.sendInviteEmail(
            request.getToEmail(),
            request.getCustNm(),
            fromEmail,
            cmpycd,
            userid,
            request.getCustcd()
        );

        return ResponseEntity.ok(ApiResponse.success("초대 메일이 발송되었습니다."));
    }

    @PostMapping("/send-bal")
    public ResponseEntity<ApiResponse<String>> sendBalEmail(@RequestBody EmailDto payload, HttpSession session) throws Exception {
        String fromEmail = getDecryptedEmail(session.getAttribute("email"));
        int totalCount = emailService.sendBal(
            payload, 
            (String)session.getAttribute("cmpycd"), 
            session.getId(), 
            fromEmail, 
            (String)session.getAttribute("nacd"), 
            (String)session.getAttribute("userid")
        );
        return ResponseEntity.ok(ApiResponse.success(String.format("총 [%d]건 발주서 메일 전송 완료", totalCount)));
    }

    @PostMapping("/send-statement")
    public ResponseEntity<ApiResponse<String>> sendStatement(@RequestBody List<EmailDto> payload, HttpSession session) throws Exception {
        String fromEmail = getDecryptedEmail(session.getAttribute("email"));
        int totalCount = emailService.sendStatement(
            payload, 
            (String)session.getAttribute("cmpycd"), 
            session.getId(), 
            fromEmail, 
            (String)session.getAttribute("nacd"), 
            (String)session.getAttribute("userid")
        );
        return ResponseEntity.ok(ApiResponse.success(String.format("총 [%d]건 거래명세서 메일 전송 완료", totalCount)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<EmailSendHistoryDto>>> getEmailHistory(
            @RequestParam String fromdt, 
            @RequestParam String todt,
            @RequestParam String docgb, 
            @RequestParam String custnm,
            @RequestParam String sendyn, 
            HttpSession session) {
        
        List<EmailSendHistoryDto> historyList = emailService.getEmailHistory(
            (String)session.getAttribute("cmpycd"), 
            (String)session.getAttribute("nacd"), 
            docgb, 
            custnm, 
            sendyn, 
            fromdt, 
            todt
        );
        return ResponseEntity.ok(ApiResponse.success(historyList, "이메일 전송 기록 조회 완료"));
    }

    @Data
    public static class InviteRequest {
        private String toEmail;
        private String custNm;
        private String custcd;
    }
}
