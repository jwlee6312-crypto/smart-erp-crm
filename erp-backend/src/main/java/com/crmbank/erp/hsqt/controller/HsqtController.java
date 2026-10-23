package com.crmbank.erp.hsqt.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hsqt.service.HsqtService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hsqt")
@RequiredArgsConstructor
public class HsqtController {

    private final HsqtService hsqtService;

    private Map<String, Object> getParams(Map<String, Object> params, HttpSession session) {
        Map<String, Object> lowParams = new HashMap<>();
        if (params != null) {
            params.forEach((k, v) -> lowParams.put(k.toLowerCase(), v));
        }
        UserSession user = (UserSession) session.getAttribute("user_session");
        lowParams.put("cmpycd", user != null ? user.getCmpycd() : "COIT");
        lowParams.put("userid", user != null ? user.getUserid() : "SYSTEM");
        return lowParams;
    }

    @GetMapping("/master")
    public List<Map<String, Object>> getMaster(@RequestParam Map<String, Object> params, HttpSession session) {
        return hsqtService.getQuotationMaster(getParams(params, session));
    }

    @GetMapping("/detail")
    public List<Map<String, Object>> getDetail(@RequestParam Map<String, Object> params, HttpSession session) {
        return hsqtService.getQuotationDetail(getParams(params, session));
    }

    @GetMapping("/status")
    public List<Map<String, Object>> getStatus(@RequestParam Map<String, Object> params, HttpSession session) {
        return hsqtService.getQuotationStatus(getParams(params, session));
    }

    @PostMapping("/save")
    public void save(@RequestBody Map<String, Object> payload, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        @SuppressWarnings("unchecked")
        Map<String, Object> master = (Map<String, Object>) payload.get("master");
        if (master != null) {
            master.put("cmpycd", user != null ? user.getCmpycd() : "COIT");
            master.put("updemp", user != null ? user.getUserid() : "SYSTEM");
        }
        hsqtService.saveQuotation(payload);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody Map<String, Object> params, HttpSession session) {
        hsqtService.deleteQuotation(getParams(params, session));
    }
}
