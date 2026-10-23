package com.crmbank.erp.ha00.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.ha00.mapper.Ha00Mapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/ha00")
@RequiredArgsConstructor
public class Ha00Controller {

    private final Ha00Mapper ha00Mapper;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executePost(
            @PathVariable String procedure, @RequestBody Map<String, Object> params, HttpSession session) {
        
        if (session.getAttribute("user_session") == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> lowParams = new HashMap<>();
        if (params != null) {
            params.forEach((k, v) -> lowParams.put(k.toLowerCase(), v));
        }
        injectSession(lowParams, session);
        
        try {
            String proc = procedure.toUpperCase();
            List<Map<String, Object>> result;
            
            if ("HA00_00P_STR".equals(proc)) {
                lowParams.putIfAbsent("gbncd", "");
                lowParams.putIfAbsent("code", "");
                lowParams.putIfAbsent("remark", "");
                String gubun = (String) lowParams.get("gubun");
                if (lowParams.get("gbncd") == null || ((String)lowParams.get("gbncd")).isEmpty()) {
                    if (gubun != null) {
                        lowParams.put("gbncd", gubun);
                    }
                }
                if ("sc".equals(gubun)) {
                    result = ha00Mapper.HA00_MENU_GROUP_STR(lowParams);
                } else {
                    result = ha00Mapper.HA00_00P_STR(lowParams);
                }
            } else if ("HA00_010S_STR".equals(proc)) {
                lowParams.putIfAbsent("cdkind", "");
                lowParams.putIfAbsent("cdgbn", "");
                lowParams.putIfAbsent("code", "");
                lowParams.putIfAbsent("code_1", "");
                result = ha00Mapper.HA00_010S_STR(lowParams);
            } else {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(convertToLowerCaseKeys(result));
            
        } catch (Exception e) {
            log.error("❌ [HA00] 에러: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executeGet(
            @PathVariable String procedure, @RequestParam Map<String, Object> params, HttpSession session) {
        return executePost(procedure, new HashMap<>(params), session);
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
            params.putIfAbsent("userid", user.getUserid());
        }
    }
}
