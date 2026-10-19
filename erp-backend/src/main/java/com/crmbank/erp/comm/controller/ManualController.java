package com.crmbank.erp.comm.controller;

import com.crmbank.erp.comm.service.ManualService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ManualController {

    private final ManualService manualService;

    /**
     * DB 기반 매뉴얼 조회
     */
    @GetMapping("/manual/db/{progid}")
    public ResponseEntity<?> getManualDb(@PathVariable String progid) {
        return ResponseEntity.ok(manualService.getManual(progid));
    }

    /**
     * DB 기반 매뉴얼 저장
     */
    @PostMapping("/manual/db/save")
    public ResponseEntity<?> saveManualDb(@RequestBody Map<String, Object> params) {
        try {
            log.info("매뉴얼 저장 시도: {}", params.get("progid"));
            manualService.saveManual(params);
            return ResponseEntity.ok("저장되었습니다.");
        } catch (Exception e) {
            log.error("매뉴얼 저장 실패: ", e);
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
