package com.crmbank.erp.comm.handler;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * 📡 [전역 요청 추적기]
 * 모든 HTTP 요청의 경로와 발신 IP를 로그로 남겨 통신 흐름을 진단합니다.
 */
@Slf4j
@Component
@org.springframework.core.annotation.Order(0)
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getRequestURI();
        String query = req.getQueryString();
        String fullPath = (query != null) ? path + "?" + query : path;
        
        // 💡 [최종 진단] 모든 요청을 낱낱이 기록하여 Nginx 전달 주소를 확인합니다.
        log.info("📡 [Incoming Request] Method: {}, Path: {}, RemoteIP: {}", 
                 req.getMethod(), fullPath, req.getRemoteAddr());

        chain.doFilter(request, response);
    }
}
