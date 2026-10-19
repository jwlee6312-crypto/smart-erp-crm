package com.crmbank.erp.comm.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.crmbank.erp.comm.dto.EmailDto;
import com.crmbank.erp.comm.dto.EmailResultDto;
import com.crmbank.erp.comm.dto.EmailSendHistoryDto;
import com.crmbank.erp.comm.mapper.EmailMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 이메일 비동기 발송 서비스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncEmailSender {

	private final JavaMailSender mailSender;
	private final SpringTemplateEngine templateEngine;
	private final PlaywrightPdfService playwrightPdfService;
	private final EmailMapper emailMapper;

	@Async("emailTaskExecutor")
	public CompletableFuture<EmailResultDto> sendMailAsync(EmailDto dto, Map<String, String> cookies, String cmpycd,
																												 String fromEmail, String userid) {

		EmailSendHistoryDto historyDto = new EmailSendHistoryDto();
		String errorMessage = null;

		try {
			log.info("[{}] 메일 전송 시작: {} -> {}", Thread.currentThread().getName(), dto.getNo(), dto.getEmail());

			// PDF 생성
			byte[] pdfData = playwrightPdfService.generatePdfFromUrl(dto.getUrl(), cookies);

			// 메일 전송
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			Context context = new Context();
			context.setVariable("custnm", dto.getCustnm());
			context.setVariable("name", dto.getName());
			context.setVariable("to", dto.getEmail());

			String html = templateEngine.process("email/email-form", context);
			helper.setFrom(fromEmail);
			helper.setTo(dto.getEmail());
			helper.setSubject(dto.getSubject());
			helper.setText(html, true);
			helper.addAttachment("invoice.pdf", new ByteArrayResource(pdfData));

			mailSender.send(message);
			historyDto.setSendyn("Y");

		} catch (Exception e) {
			errorMessage = e.getMessage();
			log.error("[{}] 메일 전송 실패: {} -> {}", Thread.currentThread().getName(), dto.getNo(), dto.getEmail());
			historyDto.setSendyn("N");
		} finally {
			saveHistory(historyDto, dto, cmpycd, fromEmail, userid);
		}

		return CompletableFuture.completedFuture(
			new EmailResultDto(errorMessage == null, dto, errorMessage)
		);
	}

	private void saveHistory(EmailSendHistoryDto historyDto, EmailDto dto, String cmpycd, String fromEmail, String userid) {
		LocalDateTime now = LocalDateTime.now();
		historyDto.setCmpycd(cmpycd);
		historyDto.setDocgb(dto.getDocgb());
		historyDto.setSendymd(now.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
		historyDto.setSendno(dto.getNo());
		historyDto.setUserid(userid);
		historyDto.setSenddate(now);
		historyDto.setCustcd(dto.getCustcd());
		historyDto.setEmail(dto.getEmail());
		historyDto.setSendemail(fromEmail);
		historyDto.setSubject(dto.getSubject());
		historyDto.setAttachurl(dto.getUrl());
		historyDto.setScheduletype(dto.getScheduletype());
		historyDto.setAddtime(now);
		historyDto.setUpdtime(now);
		historyDto.setUpdemp(userid);
		emailMapper.saveMailHist(historyDto);
	}
}
