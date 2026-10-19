package com.crmbank.erp.comm.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/")
    public String healthCheck(jakarta.servlet.http.HttpServletRequest request) {
        return "<h1>🚀 ERP Backend System is Running!</h1>" +
               "<p>Your IP: <b>" + request.getRemoteAddr() + "</b></p>" +
               "<p>API Gateway 및 네트워크 연결이 완벽하게 성공했습니다.</p>";
    }
}
