package com.crmbank.erp.comm.controller;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

/**
 * 세션 상태 확인 및 정보 조회 공통 컨트롤러
 */
@RestController
public class SessionController {

    @GetMapping("/session-check")
    public ResponseEntity<Map<String, Boolean>> checkSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        boolean active = false;

        if (session != null) {
            String cmpycd = (String) session.getAttribute("cmpycd"); 
            active = (cmpycd != null);
        }

        return ResponseEntity.ok(Map.of("sessionActive", active));
    }

    @GetMapping("/session")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSessionInfo(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        response.put("cmpycd", session.getAttribute("cmpycd"));
        response.put("cmpynm", session.getAttribute("cmpynm")); // 🚀 회사명 추가
        response.put("nacd", session.getAttribute("nacd"));
        response.put("userid", session.getAttribute("userid"));
        response.put("usernm", session.getAttribute("usernm"));
        response.put("deptcd", session.getAttribute("deptcd"));
        response.put("deptnm", session.getAttribute("deptnm"));
        response.put("usergrp", session.getAttribute("usergrp"));
        response.put("telno", session.getAttribute("telno"));
        response.put("hpno", session.getAttribute("hpno"));
        response.put("empno", session.getAttribute("empno"));
        response.put("salsyn", session.getAttribute("salsyn"));
        response.put("useyn", session.getAttribute("useyn"));
        response.put("email", session.getAttribute("email"));
        response.put("extension", session.getAttribute("extension"));
        response.put("inner_no", session.getAttribute("inner_no"));
        return ResponseEntity.ok(response);
    }
}
