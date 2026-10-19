package com.crmbank.erp.comm.handler;

import com.crmbank.erp.comm.dto.UserSession;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 🔒 [심플 세션 체크]
 * 로그인 후 사용 중 세션이 증발하면 401을 던져 로그아웃 유도
 */
@Slf4j
@Component
@Order(1)
public class SessionCheckFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getRequestURI();

        // 로그인, 세션확인, 정적파일, CTI 웹훅 및 웹소켓은 무조건 통과
        if (path.contains("/comm/login") || path.contains("/comm/session") || path.contains("/ws/cti") || path.contains("/inbound/trigger-popup") || path.contains("/inbound/log-callback") || !path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        // 로그인 세션 및 필수 정보(회사코드, ID) 체크
        HttpSession session = req.getSession(false);
        if (session != null) {
            UserSession user = (UserSession) session.getAttribute("user_session");
            if (user != null && user.getCmpycd() != null && user.getUserid() != null) {
                chain.doFilter(request, response);
                return;
            }
        }

        // 정보 유실 시 401 반환
        res.setStatus(401);
    }
}
