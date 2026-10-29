package com.crmbank.erp.crm.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.crm.service.CustomQueryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/crm/outbound/custom-query")
@RequiredArgsConstructor
public class CustomQueryController {

    private final CustomQueryService customQueryService;

    private Map<String, Object> getParams(Map<String, Object> params, HttpSession session) {
        Map<String, Object> lowParams = new HashMap<>();
        if (params != null) {
            params.forEach((k, v) -> lowParams.put(k.toLowerCase(), v));
        }
        UserSession user = (UserSession) session.getAttribute("user_session");
        String cmpycd = user != null ? user.getCmpycd() : null;
        if (cmpycd == null || cmpycd.trim().isEmpty()) {
            cmpycd = "COIT";
        }
        lowParams.put("cmpycd", cmpycd.trim());
        lowParams.put("userid", user != null ? user.getUserid() : "SYSTEM");
        return lowParams;
    }

    @GetMapping("/list")
    public List<Map<String, Object>> getQueryList(@RequestParam Map<String, Object> params, HttpSession session) {
        return customQueryService.getQueryList(getParams(params, session));
    }

    @PostMapping("/save")
    public void saveQuery(@RequestBody Map<String, Object> payload, HttpSession session) {
        customQueryService.saveQuery(getParams(payload, session));
    }

    @PostMapping("/delete")
    public void deleteQuery(@RequestBody Map<String, Object> params, HttpSession session) {
        customQueryService.deleteQuery(getParams(params, session));
    }

    @PostMapping("/execute")
    public List<Map<String, Object>> executeQuery(@RequestBody Map<String, Object> params, HttpSession session) {
        return customQueryService.executeCustomQuery(getParams(params, session));
    }
}
