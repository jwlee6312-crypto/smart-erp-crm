package com.crmbank.erp.sysm.controller;

import com.crmbank.erp.comm.dto.UserSession;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/sysm/init")
@RequiredArgsConstructor
public class SysmController {

    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/template/{type}")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable String type) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Template");
            Row headerRow = sheet.createRow(0);

            String[] headers;
            switch (type) {
                case "org" -> headers = new String[]{"부서코드", "부서명", "상위부서코드", "사용여부"};
                case "user" -> headers = new String[]{"사원번호", "사용자ID", "성명", "부서코드", "이메일", "연락처", "내선번호"};
                case "item" -> headers = new String[]{"품목코드", "품목명", "규격", "단가", "자산구분(astkind)", "사용여부"};
                case "cust" -> headers = new String[]{"거래처코드", "사업번호", "거래처명", "대표자명", "업태", "종목", "우편번호", "주소-1", "주소-2", "메일", "핸드폰", "ERP코드"};
                case "phone" -> headers = new String[]{"전화번호(phoneno)", "유형(phonetype)", "거래처코드(custcd)", "대표여부(repyn)", "이름(name)", "사용여부(useyn)"};
                default -> throw new IllegalArgumentException("지원하지 않거나 허용되지 않는 템플릿 유형입니다: " + type);
            }

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);

            byte[] bytes = baos.toByteArray();
            HttpHeaders headersMap = new HttpHeaders();
            headersMap.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = type + "_" + timestamp + ".xlsx";
            headersMap.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

            return ResponseEntity.ok().headers(headersMap).body(bytes);
        } catch (Exception e) {
            log.error("❌ 템플릿 다운로드 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage().getBytes());
        }
    }

    @PostMapping("/json-upload/{type}")
    public ResponseEntity<?> uploadJson(@PathVariable String type, @RequestBody List<Map<String, Object>> rows, HttpSession session) {
        if ("code".equals(type)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "공통코드 일괄 업로드는 보안 및 무결성 정책에 따라 엄격히 금지되어 있습니다."));
        }

        UserSession user = (UserSession) session.getAttribute("user_session");
        String cmpycd = user != null ? user.getCmpycd() : "COIT";
        String userid = user != null ? user.getUserid() : "system";

        try {
            int successCount = 0;
            for (Map<String, Object> row : rows) {
                if ("org".equals(type)) {
                    String deptcd = String.valueOf(row.getOrDefault("deptcd", "")).trim();
                    String deptnm = String.valueOf(row.getOrDefault("deptnm", "")).trim();
                    String updept = String.valueOf(row.getOrDefault("updept", "")).trim();
                    String useyn = String.valueOf(row.getOrDefault("useyn", "Y")).trim();

                    if (!deptcd.isEmpty() && !deptnm.isEmpty()) {
                        int updated = jdbcTemplate.update(
                            "INSERT IGNORE INTO haba120t_tbl (cmpycd, deptcd, deptnm, updept, useyn, addtime, updtime, updemp) VALUES (?, ?, ?, ?, ?, NOW(), NOW(), ?)",
                            cmpycd, deptcd, deptnm, updept, useyn.isEmpty() ? "Y" : useyn, userid
                        );
                        if (updated > 0) successCount++;
                    }
                } else if ("user".equals(type)) {
                    String empno = String.valueOf(row.getOrDefault("empno", "")).trim();
                    String targetUserid = String.valueOf(row.getOrDefault("userid", "")).trim();
                    String usernm = String.valueOf(row.getOrDefault("usernm", "")).trim();
                    String deptcd = String.valueOf(row.getOrDefault("deptcd", "")).trim();
                    String email = String.valueOf(row.getOrDefault("email", "")).trim();
                    String telno = String.valueOf(row.getOrDefault("telno", "")).trim();
                    String inner_no = String.valueOf(row.getOrDefault("inner_no", "")).trim();

                    if (!targetUserid.isEmpty() && !usernm.isEmpty()) {
                        String encodedPw = passwordEncoder.encode("smart");
                        int updated = jdbcTemplate.update(
                            "INSERT IGNORE INTO haba920t_tbl (cmpycd, empno, userid, usernm, deptcd, email, telno, inner_no, pw, usergrp, useyn, addtime, updtime, updemp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, '600', 'Y', NOW(), NOW(), ?)",
                            cmpycd, empno, targetUserid, usernm, deptcd, email, telno, inner_no, encodedPw, userid
                        );
                        if (updated > 0) successCount++;
                    }
                } else if ("item".equals(type)) {
                    String itemcd = String.valueOf(row.getOrDefault("itemcd", "")).trim();
                    String itemnm = String.valueOf(row.getOrDefault("itemnm", "")).trim();
                    String spec = String.valueOf(row.getOrDefault("spec", "")).trim();
                    double price = 0.0;
                    try { price = Double.parseDouble(String.valueOf(row.getOrDefault("price", 0))); } catch (Exception ignored) {}
                    String astkind = String.valueOf(row.getOrDefault("astkind", "")).trim();
                    String useyn = String.valueOf(row.getOrDefault("useyn", "Y")).trim();

                    if (!itemcd.isEmpty() && !itemnm.isEmpty() && !astkind.isEmpty()) {
                        int updated = jdbcTemplate.update(
                            "INSERT IGNORE INTO hsba010t_tbl (cmpycd, itemcd, itemnm, spec, price, astkind, useyn, addtime, updtime, updemp) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?)",
                            cmpycd, itemcd, itemnm, spec, price, astkind, useyn.isEmpty() ? "Y" : useyn, userid
                        );
                        if (updated > 0) successCount++;
                    }
                } else if ("cust".equals(type)) {
                    String custcd = String.valueOf(row.getOrDefault("custcd", "")).trim();
                    String bizno = String.valueOf(row.getOrDefault("bizno", "")).trim();
                    String custnm = String.valueOf(row.getOrDefault("custnm", "")).trim();
                    String ceonm = String.valueOf(row.getOrDefault("ceonm", "")).trim();
                    String bizcond = String.valueOf(row.getOrDefault("bizcond", "")).trim();
                    String item = String.valueOf(row.getOrDefault("item", "")).trim();
                    String postno = String.valueOf(row.getOrDefault("postno", "")).trim();
                    String address = String.valueOf(row.getOrDefault("address", "")).trim();
                    String address2 = String.valueOf(row.getOrDefault("address2", "")).trim();
                    String email = String.valueOf(row.getOrDefault("email", "")).trim();
                    String telno = String.valueOf(row.getOrDefault("telno", "")).trim();
                    String outcustcd = String.valueOf(row.getOrDefault("outcustcd", "")).trim();

                    if (!custcd.isEmpty() && !custnm.isEmpty()) {
                        int updated = jdbcTemplate.update(
                            "INSERT IGNORE INTO haba020t_tbl (cmpycd, custcd, custno, custnm, bizno, bossnm, bizcond, item, postno, address, d_address, email, telno, outcustcd, useyn, addtime, updtime, updemp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Y', NOW(), NOW(), ?)",
                            cmpycd, custcd, bizno.isEmpty() ? custcd : bizno, custnm, bizno.isEmpty() ? custcd : bizno, ceonm, bizcond, item, postno, address, address2, email, telno, outcustcd, userid
                        );
                        if (updated > 0) successCount++;
                    }
                } else if ("phone".equals(type)) {
                    String phoneno = String.valueOf(row.getOrDefault("phoneno", "")).trim();
                    String phonetype = String.valueOf(row.getOrDefault("phonetype", "")).trim();
                    String custcd = String.valueOf(row.getOrDefault("custcd", "")).trim();
                    String repyn = String.valueOf(row.getOrDefault("repyn", "N")).trim();
                    String name = String.valueOf(row.getOrDefault("name", "")).trim();
                    String useyn = String.valueOf(row.getOrDefault("useyn", "Y")).trim();

                    if (!phoneno.isEmpty() && !phonetype.isEmpty() && !custcd.isEmpty()) {
                        int updated = jdbcTemplate.update(
                            "INSERT IGNORE INTO phone_number_map (phoneno, phonetype, custcd, repyn, name, useyn) VALUES (?, ?, ?, ?, ?, ?)",
                            phoneno, phonetype, custcd, repyn, name, useyn.isEmpty() ? "Y" : useyn
                        );
                        if (updated > 0) successCount++;
                    }
                }
            }

            log.info("📁 [시스템 초기화 JSON - 추가전용] 회사 [{}] - 유형 [{}] 일괄 등록 성공 (총 {}건 신규 등록)", cmpycd, type, successCount);
            return ResponseEntity.ok(Map.of("success", true, "count", successCount, "message", "총 " + successCount + "건의 신규 데이터가 성공적으로 추가되었습니다. (중복 및 마스터키 누락 건 제외)"));
        } catch (Exception e) {
            log.error("❌ JSON 업로드 처리 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
