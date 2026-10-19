/*
 * ===========================================
 * 프로그램명			: 거래명세서, 명세서 메일전송, Invoice 발행, 메일전송기록조회
 * 프로그램 ID		: HESB050S, HECG050U, HECG040U, HECG060U
 * 작성일자				: 25.06.19
 * 작성자				: 이현준
 * 수정일자				: 25.11.25
 * 수정자				: 이현준
 * 설명					: Email 전송 Service
 * 수정 내용			: URL 파라미터 인코딩 처리 및 경로 확정
 * ===========================================
 */

package com.crmbank.erp.comm.service;

import com.crmbank.erp.comm.config.AesConfig;
import com.crmbank.erp.comm.dto.EmailDto;
import com.crmbank.erp.comm.dto.EmailResultDto;
import com.crmbank.erp.comm.dto.EmailSendHistoryDto;
import com.crmbank.erp.comm.mapper.EmailMapper;
import com.crmbank.erp.comm.util.SecurityUtil;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final PlaywrightPdfService playwrightPdfService;
    private final EmailMapper emailMapper;
    private final AsyncEmailSender asyncEmailSender;
    private final AesConfig aesConfig;

    @Value("${spring.session.store-type:none}")
    private String sessionStoreType;

    private final String FIXED_FROM_EMAIL = "jwlee6312@gmail.com";
    private final String BRAND_NAME = "SmartCore";

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * 💡 실시간 상담실 초대 메일 발송
     */
    public void sendInviteEmail(String toEmail, String custNm, String fromEmail, String cmpycd, String userid, String custcd) {
        EmailSendHistoryDto historyDto = new EmailSendHistoryDto();
        
        String inviteUrl = "";
        try {
            // 💡 [중요] URL 파라미터 인코딩 (한글 이름 및 특수문자 깨짐 방지)
            String encodedEmail = URLEncoder.encode(toEmail, StandardCharsets.UTF_8);
            String encodedName = URLEncoder.encode(custNm, StandardCharsets.UTF_8);
            inviteUrl = String.format("%s/HGOA/HGOA100C?email=%s&name=%s", frontendUrl, encodedEmail, encodedName);
        } catch (Exception e) {
            inviteUrl = String.format("%s/HGOA/HGOA100C?email=%s&name=%s", frontendUrl, toEmail, custNm);
        }

        String subject = String.format("[%s] 실시간 고객 상담실 초대장입니다.", BRAND_NAME);

        log.info("📧 [EmailService] 초대장 발송 시도: {} -> {} (URL: {})", fromEmail, toEmail, inviteUrl);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("custNm", custNm);
            context.setVariable("inviteUrl", inviteUrl);

            String html = templateEngine.process("email/email-invite", context);
            
            helper.setFrom(FIXED_FROM_EMAIL, BRAND_NAME);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
            historyDto.setSendyn("Y");
            log.info("✅ [EmailService] 메일 발송 완료");
        } catch (Exception e) {
            log.error("❌ [EmailService] 메일 발송 실패: {}", e.getMessage(), e);
            historyDto.setSendyn("N");
        } finally {
            saveHistory(historyDto, cmpycd, "700", "INV-" + System.currentTimeMillis(), userid, custcd, toEmail, subject, inviteUrl);
        }
    }

    public int sendBal(EmailDto payload, String cmpycd, String sessionId, String fromEmail, String nacd, String userid) throws Exception {
        EmailSendHistoryDto historyDto = new EmailSendHistoryDto();
        String senderName = (payload.getFromnm() != null && !payload.getFromnm().isEmpty()) ? payload.getFromnm() : BRAND_NAME;
        String subject = String.format("[%s] 발주서 전달드립니다", senderName);
        try {
            byte[] pdfData;
            if (payload.getHtmlcontent() != null && !payload.getHtmlcontent().isEmpty()) {
                pdfData = playwrightPdfService.generatePdfFromHtml(payload.getHtmlcontent());
            } else {
                Map<String, String> cookies = prepareCookies(sessionId);
                pdfData = playwrightPdfService.generatePdfFromUrl(payload.getUrl(), cookies);
            }
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            Context context = new Context();
            context.setVariable("custnm", payload.getCustnm());
            String html = templateEngine.process("order/purchase-mail", context);
            helper.setText(html, true);
            helper.setFrom(FIXED_FROM_EMAIL, senderName); // 🚀 [개선] 회사명으로 발신자명 설정
            helper.setTo(payload.getEmail());
            helper.setSubject(subject);
            helper.addAttachment("발주서.pdf", new ByteArrayResource(pdfData));
            mailSender.send(message);
            historyDto.setSendyn("Y");
            return 1;
        } catch (Exception e) { historyDto.setSendyn("N"); throw e; }
        finally { saveHistory(historyDto, cmpycd, payload.getDocgb(), payload.getNo(), userid, payload.getCustcd(), payload.getEmail(), subject, payload.getUrl()); }
    }

    public int sendStatement(List<EmailDto> payload, String cmpycd, String sessionId, String fromEmail, String nacd, String userid) throws Exception {
        Map<String, String> cookies = prepareCookies(sessionId);
        int count = 0;
        for (EmailDto dto : payload) {
            EmailSendHistoryDto historyDto = new EmailSendHistoryDto();
            String subject = String.format("[%s] 거래명세서 입니다.", BRAND_NAME);
            try {
                byte[] pdfData = playwrightPdfService.generatePdfFromUrl(dto.getUrl(), cookies);
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                Context context = new Context();
                context.setVariable("custnm", dto.getCustnm());
                String html = templateEngine.process("email/email-statement", context);
                helper.setText(html, true);
                helper.setFrom(FIXED_FROM_EMAIL, BRAND_NAME);
                helper.setTo(dto.getEmail());
                helper.setSubject(subject);
                helper.addAttachment("statement.pdf", new ByteArrayResource(pdfData));
                mailSender.send(message);
                historyDto.setSendyn("Y");
                count++;
            } catch (Exception e) { historyDto.setSendyn("N"); }
            finally { saveHistory(historyDto, cmpycd, dto.getDocgb(), dto.getNo(), userid, dto.getCustcd(), dto.getEmail(), subject, dto.getUrl()); }
        }
        return count;
    }

    public EmailSendSummary sendSummary(List<EmailDto> payload, String cmpycd, String sessionId, String fromEmail, String userid) {
        Map<String, String> cookies = prepareCookies(sessionId);
        List<CompletableFuture<EmailResultDto>> futures = payload.stream()
            .map(dto -> asyncEmailSender.sendMailAsync(dto, cookies, cmpycd, FIXED_FROM_EMAIL, userid))
            .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        List<EmailResultDto> results = futures.stream().map(CompletableFuture::join).toList();
        return analyzeResults(results);
    }

    private void saveHistory(EmailSendHistoryDto historyDto, String cmpycd, String docgb, String sendno, String userid, String custcd, String email, String subject, String url) {
        LocalDateTime now = LocalDateTime.now();
        historyDto.setCmpycd(cmpycd);
        historyDto.setDocgb(docgb);
        historyDto.setSendymd(now.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        historyDto.setSendno(sendno);
        historyDto.setUserid(userid);
        historyDto.setSenddate(now);
        historyDto.setCustcd(custcd != null && !custcd.isEmpty() ? custcd : "0000000");
        historyDto.setEmail(email);
        historyDto.setSendemail(FIXED_FROM_EMAIL);
        historyDto.setSubject(subject);
        historyDto.setAttachurl(url);
        historyDto.setScheduletype("N");
        historyDto.setAddtime(now);
        historyDto.setUpdtime(now);
        historyDto.setUpdemp(userid);
        emailMapper.saveMailHist(historyDto);
    }

    private Map<String, String> prepareCookies(String sessionId) {
        Map<String, String> cookies = new HashMap<>();
        if ("redis".equalsIgnoreCase(sessionStoreType)) {
            String encodedSessionId = Base64.getEncoder().encodeToString(sessionId.getBytes(StandardCharsets.UTF_8));
            cookies.put("JSESSIONID", encodedSessionId);
        } else {
            cookies.put("JSESSIONID", sessionId);
        }
        return cookies;
    }

    private EmailSendSummary analyzeResults(List<EmailResultDto> results) {
        int success = (int) results.stream().filter(EmailResultDto::isSuccess).count();
        return new EmailSendSummary(results.size(), success, results.size() - success, new ArrayList<>());
    }

    @Getter @AllArgsConstructor
    public static class EmailSendSummary {
        private int total; private int success; private int failed; private List<FailedEmail> failedEmails;
        public String getSummaryMessage() { return failed == 0 ? String.format("총 %d건 전송 완료", total) : String.format("%d건 중 %d건 성공, %d건 실패", total, success, failed); }
    }

    @Getter @AllArgsConstructor public static class FailedEmail { private String email; private String no; private String reason; }

    public List<EmailSendHistoryDto> getEmailHistory(String cmpycd, String nacd, String docgb, String custnm, String sendyn, String fromdt, String todt) {
        Map<String, Object> params = new HashMap<>();
        params.put("cmpycd", cmpycd); params.put("nacd", nacd); params.put("docgb", docgb);
        params.put("custnm", custnm); params.put("sendyn", sendyn); params.put("fromdt", fromdt); params.put("todt", todt);
        return emailMapper.getMailHistory(params);
    }
}
