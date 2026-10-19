package com.crmbank.erp.comm.controller;

import com.crmbank.erp.comm.service.CommService;
import com.crmbank.erp.comm.dto.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/comm")
@RequiredArgsConstructor
public class CommController {

    private final CommService commService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> data, HttpServletRequest request) {
        log.info("📡 [로그인 시도] IP: {}, Data: {}", request.getRemoteAddr(), data);
        try {
            UserSession user = commService.login(data.get("cmpycd"), data.get("userid"), data.get("passwd"), request.getRemoteAddr());
            HttpSession session = request.getSession(true);
            session.setAttribute("user_session", user);
            
            // 🚀 [보안/호환성] 개별 세션 속성 누락 없이 전체 바인딩
            session.setAttribute("cmpycd", user.getCmpycd());
            session.setAttribute("cmpynm", user.getCmpynm());
            session.setAttribute("userid", user.getUserid());
            session.setAttribute("usernm", user.getUsernm());
            session.setAttribute("deptcd", user.getDeptcd());
            session.setAttribute("deptnm", user.getDeptnm());
            session.setAttribute("usergrp", user.getUsergrp());
            session.setAttribute("email", user.getEmail());
            session.setAttribute("hpno", user.getHpno());
            session.setAttribute("inner_no", user.getInner_no());
            session.setAttribute("salsyn", user.getSalsyn());
            session.setAttribute("status", user.getStatus());
            session.setAttribute("routing_mode", user.getRouting_mode());
            session.setAttribute("photo_path", user.getPhoto_path());
            
            return ResponseEntity.ok(user);
        } catch (Exception e) { 
            return ResponseEntity.badRequest().body(e.getMessage()); 
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) { 
        session.invalidate(); 
        return ResponseEntity.ok("로그아웃"); 
    }

    @GetMapping("/session")
    public ResponseEntity<?> getSession(HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        return user == null ? ResponseEntity.ok().build() : ResponseEntity.ok(user);
    }

    /** 🌐 웹 전용 대메뉴 조회 (원복) */
    @GetMapping("/top-menus")
    public ResponseEntity<List<Map<String, Object>>> getTopMenus() { 
        return ResponseEntity.ok(commService.getTopMenus()); 
    }

    /** 📱 모바일 전용 대메뉴 조회 */
    @GetMapping("/top-menus-mobile")
    public ResponseEntity<List<Map<String, Object>>> getTopMenusMobile() { 
        return ResponseEntity.ok(commService.getTopMenusMobile()); 
    }

    @PostMapping("/HA00_200S_STR")
    public ResponseEntity<List<Map<String, Object>>> execLeftMenu(@RequestBody Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();

        String cmpycd = user.getCmpycd();
        String usergrp = user.getUsergrp();
        String upmucd = String.valueOf(params.getOrDefault("upmucd", ""));
        
        // 🚀 앱에서 보낸 userid(MOBILE)가 있으면 세션 정보보다 우선, 없으면 로그인 ID 사용
        String targetUserId = String.valueOf(params.getOrDefault("userid", user.getUserid()));

        log.info("🔍 [메뉴 조회 실행] Params: {}, {}, {}, {}", cmpycd, targetUserId, upmucd, usergrp);
        return ResponseEntity.ok(commService.getLeftMenus(cmpycd, targetUserId, upmucd, usergrp));
    }

    @PostMapping("/getProgramList")
    public ResponseEntity<List<Map<String, Object>>> getProgramList(@RequestBody Map<String, Object> data) {
        return ResponseEntity.ok(commService.getProgramList(data));
    }
}
