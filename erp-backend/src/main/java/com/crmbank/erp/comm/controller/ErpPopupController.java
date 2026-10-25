package com.crmbank.erp.comm.controller;

import com.crmbank.erp.comm.dto.UserSession;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/erp")
@RequiredArgsConstructor
public class ErpPopupController {

    private final ErpPopupMapper popupMapper;

    @GetMapping("/customer")
    public List<Map<String, Object>> getCustomerList(@RequestParam Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        log.info("🔍 [Popup Customer] params: {}", params);
        return convertToLowerCaseKeys(popupMapper.selectCustomerList(params));
    }

    @GetMapping("/item")
    public List<Map<String, Object>> getItemList(@RequestParam Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        log.info("🔍 [Popup Item] params: {}", params);
        return convertToLowerCaseKeys(popupMapper.selectItemList(params));
    }

    @GetMapping("/code")
    public List<Map<String, Object>> getCodeList(@RequestParam Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        log.info("🔍 [Popup Code] params: {}", params);
        return convertToLowerCaseKeys(popupMapper.selectCodeList(params));
    }

    private List<Map<String, Object>> convertToLowerCaseKeys(List<Map<String, Object>> list) {
        List<Map<String, Object>> newList = new ArrayList<>();
        if (list != null) {
            for (Map<String, Object> map : list) {
                Map<String, Object> newMap = new LinkedHashMap<>();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    newMap.put(entry.getKey().toLowerCase(), entry.getValue());
                }
                newList.add(newMap);
            }
        }
        return newList;
    }

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            params.putIfAbsent("cmpycd", user.getCmpycd());
        }
        if (params.get("cmpycd") == null || String.valueOf(params.get("cmpycd")).isEmpty()) {
            params.put("cmpycd", "COIT");
        }
        Object keyword = params.get("keyword");
        if (keyword == null || String.valueOf(keyword).isEmpty()) {
            if (params.get("search") != null && !String.valueOf(params.get("search")).isEmpty()) {
                keyword = params.get("search");
            }
        }
        params.put("keyword", keyword != null ? String.valueOf(keyword).trim() : "");
    }
}
