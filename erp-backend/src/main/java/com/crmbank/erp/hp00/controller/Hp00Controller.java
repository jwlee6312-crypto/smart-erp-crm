package com.crmbank.erp.hp00.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.hp00.mapper.Hp00Mapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/hp00")
@RequiredArgsConstructor
public class Hp00Controller {

    private final Hp00Mapper hp00Mapper;

    @GetMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executeGet(
            @PathVariable String procedure, @RequestParam Map<String, Object> params, HttpSession session) {
        return execute(procedure, new HashMap<>(params), session);
    }

    @PostMapping("/{procedure}")
    public ResponseEntity<List<Map<String, Object>>> executePost(
            @PathVariable String procedure, @RequestBody Map<String, Object> params, HttpSession session) {
        return execute(procedure, params, session);
    }

    private ResponseEntity<List<Map<String, Object>>> execute(String procedure, Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        try {
            List<Map<String, Object>> result;
            if ("HP00_000S_STR".equalsIgnoreCase(procedure)) {
                params.putIfAbsent("gubun", "");
                params.putIfAbsent("gbncd", "");
                params.putIfAbsent("code", "");
                params.putIfAbsent("codenm", "");
                params.putIfAbsent("etcval", "");
                result = hp00Mapper.HP00_000S_STR(params);
            } else {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(convertToLowerCaseKeys(result));
        } catch (Exception e) {
            log.error("❌ [HP00] 에러: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
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
