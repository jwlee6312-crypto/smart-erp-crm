package com.crmbank.erp.comm.controller;

import com.crmbank.erp.comm.service.SmsService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/common/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    @PostMapping("/send")
    public Map<String, Object> sendSms(@RequestBody SmsRequest request) {
        Map<String, Object> result = new HashMap<>();
        boolean success = smsService.sendSms(request.getTo(), request.getContent());
        
        result.put("success", success);
        result.put("message", success ? "문자가 성공적으로 발송되었습니다." : "문자 발송 실패");
        return result;
    }

    @Data
    public static class SmsRequest {
        private String to;
        private String content;
    }
}
