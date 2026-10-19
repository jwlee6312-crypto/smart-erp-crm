package com.crmbank.erp.haba.controller;

import com.crmbank.erp.comm.dto.UserSession;
import com.crmbank.erp.haba.mapper.HabaMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.SqlSession;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * [HABA] 회계/영업기준 통합 컨트롤러 (사용자 정의 최종 표준형)
 */
@SuppressWarnings("unused")
@Slf4j
@RestController
@RequestMapping("/haba")
@RequiredArgsConstructor
public class HabaController {

    private final HabaMapper habaMapper;
    private final SqlSession sqlSession;
    private final JdbcTemplate jdbcTemplate;

    // ==========================================
    // 1. 특수 / 사용자 상태 관리 엔드포인트
    // ==========================================

    @Transactional
    @PostMapping("/update-my-status")
    public ResponseEntity<?> updateMyStatus(@RequestBody Map<String, Object> data, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user == null) return ResponseEntity.status(401).build();

        String cmpycd = user.getCmpycd();
        String userid = user.getUserid();
        String status = String.valueOf(data.get("status"));
        String routingMode = String.valueOf(data.get("routing_mode"));

        jdbcTemplate.update("UPDATE HABA920T_TBL SET status = ?, routing_mode = ?, updtime = GETDATE(), updemp = ? WHERE cmpycd = ? AND userid = ?",
                status, routingMode, userid, cmpycd, userid);

        user.setStatus(status);
        user.setRouting_mode(routingMode);
        session.setAttribute("user_session", user);

        return ResponseEntity.ok(Map.of("success", true));
    }

    // ==========================================
    // 2. U_STR 프로시저 (마스터/디테일 표준화)
    // ==========================================

    /*
    @PostMapping("/HABA_010U_STR")
    public ResponseEntity<?> callHABA_010U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_010U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_010U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S1")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_010U_STR(params);

        if ("S1".equals(actkind) || "S2".equals(actkind) || "SR".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    /*
    @PostMapping("/HABA_020U_STR")
    public ResponseEntity<?> callHABA_020U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_020U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_020U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S1")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_020U_STR(params);

        if ("S1".equals(actkind) || "S2".equals(actkind) || "S4".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    /*
    @PostMapping("/HABA_021U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHABA_021U_STR(@RequestBody Object inputDetails, HttpSession session) {
        Map<String, Object> params = null;
        if (inputDetails instanceof Map) {
            params = (Map<String, Object>) inputDetails;
        } else if (inputDetails instanceof List && !((List<?>) inputDetails).isEmpty()) {
            params = (Map<String, Object>) ((List<?>) inputDetails).get(0);
        }

        if (params != null) {
            String actkind = String.valueOf(params.getOrDefault("actkind", "SR")).toUpperCase();
            if (actkind.startsWith("S") || actkind.startsWith("L")) {
                injectSession(params, session);
                fillMissingParameters("HABA_021U_STR", params);
                List<Map<String, Object>> res = convertToLowerCaseKeys(habaMapper.HABA_021U_STR(params));
                log.info("📊 [HABA_021U_STR SELECT 결과 (총 {}건)]: {}", res.size(), res);
                return ResponseEntity.ok(res);
            }
        }

        List<Map<String, Object>> details = new ArrayList<>();
        if (inputDetails instanceof List) {
            details = (List<Map<String, Object>>) inputDetails;
        } else if (inputDetails instanceof Map) {
            details.add((Map<String, Object>) inputDetails);
        }

        List<Map<String, Object>> totalResults = new ArrayList<>();
        for (int i = 0; i < details.size(); i++) {
            Map<String, Object> detail = details.get(i);
            injectSession(detail, session);
            fillMissingParameters("HABA_021U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HABA_021U_STR", detail));

            List<Map<String, Object>> raw = habaMapper.HABA_021U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if (!"OK".equals(String.valueOf(resRow.getOrDefault("result", "msg")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("msg", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }
    */

    /*
    @PostMapping("/HABA_022U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHABA_022U_STR(@RequestBody Object inputDetails, HttpSession session) {
        Map<String, Object> params = null;
        if (inputDetails instanceof Map) {
            params = (Map<String, Object>) inputDetails;
        } else if (inputDetails instanceof List && !((List<?>) inputDetails).isEmpty()) {
            params = (Map<String, Object>) ((List<?>) inputDetails).get(0);
        }

        if (params != null) {
            String actkind = String.valueOf(params.getOrDefault("actkind", "SR")).toUpperCase();
            if (actkind.startsWith("S") || actkind.startsWith("L")) {
                injectSession(params, session);
                fillMissingParameters("HABA_022U_STR", params);
                List<Map<String, Object>> res = convertToLowerCaseKeys(habaMapper.HABA_022U_STR(params));
                log.info("📊 [HABA_022U_STR SELECT 결과 (총 {}건)]: {}", res.size(), res);
                return ResponseEntity.ok(res);
            }
        }

        List<Map<String, Object>> details = new ArrayList<>();
        if (inputDetails instanceof List) {
            details = (List<Map<String, Object>>) inputDetails;
        } else if (inputDetails instanceof Map) {
            details.add((Map<String, Object>) inputDetails);
        }

        List<Map<String, Object>> totalResults = new ArrayList<>();
        for (int i = 0; i < details.size(); i++) {
            Map<String, Object> detail = details.get(i);
            injectSession(detail, session);
            fillMissingParameters("HABA_022U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HABA_022U_STR", detail));

            List<Map<String, Object>> raw = habaMapper.HABA_022U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if (!"OK".equals(String.valueOf(resRow.getOrDefault("result", "msg")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("msg", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }
    */

    /*
    @PostMapping("/HABA_030U_STR")
    public ResponseEntity<?> callHABA_030U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_030U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_030U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S1")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_030U_STR(params);

        if ("S1".equals(actkind) || "SR".equals(actkind) || "TX".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    /*
    @PostMapping("/HABA_040U_STR")
    public ResponseEntity<?> callHABA_040U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_040U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_040U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_040U_STR(params);

        if ("S0".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    /*
    @PostMapping("/HABA_050U_STR")
    public ResponseEntity<?> callHABA_050U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_050U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_050U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_050U_STR(params);

        if ( "S0".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    @PostMapping("/HABA_060U_STR")
    public ResponseEntity<?> callHABA_060U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_060U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_060U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_060U_STR(params);

        if ( "S0".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    /*
    @PostMapping("/HABA_070U_STR")
    public ResponseEntity<?> callHABA_070U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_070U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_070U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_070U_STR(params);

        if ( "S0".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    /*
    @PostMapping("/HABA_071U_STR")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> callHABA_071U_STR(@RequestBody Object details, HttpSession session) {
        if (details instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) details;
            String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
            if ("S0".equals(actkind)) {
                injectSession(params, session);
                fillMissingParameters("HABA_071U_STR", params);
                return ResponseEntity.ok(convertToLowerCaseKeys(habaMapper.HABA_071U_STR(params)));
            }
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) details;
        List<Map<String, Object>> totalResults = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> detail = list.get(i);
            injectSession(detail, session);
            fillMissingParameters("HABA_071U_STR", detail);
            log.info("📑 [Detail #{} SQL]: {}", i + 1, buildPositionalSql("HABA_071U_STR", detail));

            List<Map<String, Object>> raw = habaMapper.HABA_071U_STR(detail);
            if (raw != null && !raw.isEmpty()) {
                Map<String, Object> resRow = convertToLowerCaseKeys(raw).getFirst();
                if (!"OK".equals(String.valueOf(resRow.getOrDefault("result", "")))) {
                    throw new RuntimeException("상세 행 #" + (i+1) + " 오류: " + resRow.getOrDefault("msg", "저장 실패"));
                }
                totalResults.add(resRow);
            }
        }
        return ResponseEntity.ok(totalResults);
    }
    */

    /*
    @PostMapping("/HABA_080U_STR")
    public ResponseEntity<?> callHABA_080U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_080U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_080U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_080U_STR(params);

        if ( "S0".equals(actkind) || "S1".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    /*
    @PostMapping("/HABA_090U_STR")
    public ResponseEntity<?> callHABA_090U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_090U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_090U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_090U_STR(params);

        if ( "S0".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    @PostMapping("/HABA_100U_STR")
    public ResponseEntity<?> callHABA_100U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_100U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_100U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_100U_STR(params);

        if ( "S0".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    /*
    @PostMapping("/HABA_110U_STR")
    public ResponseEntity<?> callHABA_110U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_110U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_110U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S1")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_110U_STR(params);

        if ( "SR".equals(actkind) || "S1".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    /*
    @PostMapping("/HABA_120U_STR")
    public ResponseEntity<?> callHABA_120U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_120U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_120U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S1")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_120U_STR(params);

        if ( "SR".equals(actkind) || "S1".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    /*
    @PostMapping("/HABA_130U_STR")
    public ResponseEntity<?> callHABA_130U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_130U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_130U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S1")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_130U_STR(params);

        if ( "SR".equals(actkind) || "S1".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    /*
    @PostMapping("/HABA_140U_STR")
    public ResponseEntity<?> callHABA_140U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_140U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_140U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S1")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_140U_STR(params);

        if ( "SR".equals(actkind) || "S1".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    /*
    @PostMapping("/HABA_150U_STR")
    public ResponseEntity<?> callHABA_150U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_150U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_150U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S1")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_150U_STR(params);

        if ( "SR".equals(actkind) || "S1".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    /*
    @PostMapping("/HABA_160U_STR")
    public ResponseEntity<?> callHABA_160U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_160U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_160U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S1")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_160U_STR(params);

        if ( "SR".equals(actkind) || "S1".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    /*
    @PostMapping("/HABA_170U_STR")
    public ResponseEntity<?> callHABA_170U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_170U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_170U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S1")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_170U_STR(params);

        if ( "SR".equals(actkind) || "S1".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    /*
    @PostMapping("/HABA_180U_STR")
    public ResponseEntity<?> callHABA_180U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_180U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_180U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_180U_STR(params);

        if ("S0".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"N".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    /*
    @PostMapping("/HABA_210U_STR")
    public ResponseEntity<?> callHABA_210U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_210U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_210U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "SR")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_210U_STR(params);

        if ("SR".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    /*
    @PostMapping("/HABA_220U_STR")
    public ResponseEntity<?> callHABA_220U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_220U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_220U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "SR")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_220U_STR(params);

        if ("SR".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    /*
    @PostMapping("/HABA_230U_STR")
    public ResponseEntity<?> callHABA_230U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_230U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_230U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "SR")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_230U_STR(params);

        if ("SR".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    /*
    @PostMapping("/HABA_240U_STR")
    public ResponseEntity<?> callHABA_240U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_240U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_240U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "SR")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_240U_STR(params);

        if ("SR".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    /*
    @PostMapping("/HABA_250U_STR")
    public ResponseEntity<?> callHABA_250U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_250U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_250U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "SR")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_250U_STR(params);

        if ("SR".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    /*
    @PostMapping("/HABA_260U_STR")
    public ResponseEntity<?> callHABA_260U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_260U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_260U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "SR")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_260U_STR(params);

        if ("SR".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    /*
    @PostMapping("/HABA_510U_STR")
    public ResponseEntity<?> callHABA_510U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_510U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_510U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_510U_STR(params);

        if ( "S0".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }
    */

    @PostMapping("/HABA_900U_STR")
    public ResponseEntity<?> callHABA_900U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_900U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_900U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_900U_STR(params);

        if ( "S0".equals(actkind) || "S1".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HABA_910U_STR")
    public ResponseEntity<?> callHABA_910U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_910U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_910U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_910U_STR(params);

        if ("S".equals(actkind) || "S0".equals(actkind) || "S1".equals(actkind) || "L".equals(actkind)) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HABA_920U_STR")
    public ResponseEntity<?> callHABA_920U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_920U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_920U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S1")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_920U_STR(params);

        if ("S1".equals(actkind) || "S2".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    @PostMapping("/HABA_935U_STR")
    public ResponseEntity<?> callHABA_935U_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_935U_STR", params);
        log.info("🏢 [Master SQL]: {}", buildPositionalSql("HABA_935U_STR", params));

        String actkind = String.valueOf(params.getOrDefault("actkind", "S0")).toUpperCase();
        List<Map<String, Object>> raw = habaMapper.HABA_935U_STR(params);

        if ( "S0".equals(actkind) || "S1".equals(actkind) ) return ResponseEntity.ok(convertToLowerCaseKeys(raw));

        if (raw == null || raw.isEmpty()) throw new RuntimeException("마스터 처리 결과가 없습니다.");

        Map<String, Object> resultRow = mapToAlias(raw.getFirst(), "result", "msg");
        String code = String.valueOf(resultRow.get("result")).trim();
        if (!"OK".equals(code)) {
            throw new RuntimeException(String.valueOf(resultRow.get("msg")));
        }
        return ResponseEntity.ok(List.of(resultRow));
    }

    // ==========================================
    // 3. S_STR 현황 및 조회 프로시저 (1:1 직결 매핑)
    // ==========================================

    /*
    @PostMapping("/HABA_YYYY_S")
    public ResponseEntity<?> callHABA_YYYY_S(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_YYYY_S", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HABA_YYYY_S", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(habaMapper.HABA_YYYY_S(params)));
    }
    */

    /*
    @PostMapping("/HABA_190S_STR")
    public ResponseEntity<?> callHABA_190S_STR(@RequestBody Map<String, Object> params, HttpSession session) {
        injectSession(params, session);
        fillMissingParameters("HABA_190S_STR", params);
        log.info("🏢 [Exec SQL]: {}", buildPositionalSql("HABA_190S_STR", params));
        return ResponseEntity.ok(convertToLowerCaseKeys(habaMapper.HABA_190S_STR(params)));
    }
    */

    // ==========================================
    // 4. 공통 유틸리티 헬퍼 메서드
    // ==========================================

    private Map<String, Object> mapToAlias(Map<String, Object> rawRow, String col1Alias, String col2Alias) {
        Map<String, Object> newMap = new LinkedHashMap<>();
        int i = 1;
        for (Map.Entry<String, Object> entry : rawRow.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (key.startsWith("col") || key.isEmpty()) {
                if (i == 1) key = col1Alias;
                else if (i == 2) key = col2Alias;
            }
            newMap.put(key, entry.getValue() == null ? "" : entry.getValue());
            i++;
        }
        return newMap;
    }

    private void injectSession(Map<String, Object> params, HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("user_session");
        if (user != null) {
            if (params.get("cmpycd") == null || params.get("cmpycd").toString().trim().isEmpty()) {
                params.put("cmpycd", user.getCmpycd());
            }
            if (params.get("userid") == null || params.get("userid").toString().trim().isEmpty()) {
                params.put("userid", user.getUserid());
            }
            params.put("updemp", user.getUserid());
        }
    }

    private void fillMissingParameters(String proc, Map<String, Object> params) {
        try {
            String statementId = HabaMapper.class.getName() + "." + proc;
            if (!sqlSession.getConfiguration().hasStatement(statementId)) return;
            MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(statementId);
            BoundSql boundSql = ms.getBoundSql(params);

            for (ParameterMapping pm : boundSql.getParameterMappings()) {
                String prop = pm.getProperty();
                if (prop != null && !prop.startsWith("_") && !prop.contains(".")) {
                    String cleanProp = prop.trim();
                    if (!params.containsKey(cleanProp) || params.get(cleanProp) == null || params.get(cleanProp).toString().trim().isEmpty()) {
                        params.put(cleanProp, "");
                    }
                    if (!cleanProp.equals(prop)) params.put(prop, params.get(cleanProp));
                }
            }
        } catch (Exception e) { log.warn("🛠 missing parameter alarm ({}): {}", proc, e.getMessage()); }
    }

    private String buildPositionalSql(String proc, Map<String, Object> params) {
        try {
            String statementId = HabaMapper.class.getName() + "." + proc;
            if (!sqlSession.getConfiguration().hasStatement(statementId)) return "EXEC " + proc;
            BoundSql boundSql = sqlSession.getConfiguration().getMappedStatement(statementId).getBoundSql(params);
            List<String> values = new ArrayList<>();

            for (ParameterMapping pm : boundSql.getParameterMappings()) {
                Object val = params.get(pm.getProperty().trim());
                String valStr = (val == null || "null".equals(String.valueOf(val))) ? "''" : "N'" + val.toString().replace("'", "''").trim() + "'";
                values.add(valStr);
            }
            return String.format("EXEC %s %s", proc, String.join(", ", values));
        } catch (Exception e) { return "EXEC " + proc; }
    }

    private List<Map<String, Object>> convertToLowerCaseKeys(List<Map<String, Object>> list) {
        if (list == null) return new ArrayList<>();
        List<Map<String, Object>> newList = new ArrayList<>();
        for (Map<String, Object> map : list) {
            Map<String, Object> newMap = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                newMap.put(entry.getKey().toLowerCase(), entry.getValue());
            }
            newList.add(newMap);
        }
        return newList;
    }
}
