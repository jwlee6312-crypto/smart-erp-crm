package com.crmbank.erp.comm.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.comm.service.BriefingDashboardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/briefing-dashboard")
@RequiredArgsConstructor
public class BriefingDashboardController {

    private final BriefingDashboardService briefingService;

    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getBriefingData(HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();

        return ResponseEntity.ok(briefingService.getBriefingData(user.getCmpycd()));
    }
}
