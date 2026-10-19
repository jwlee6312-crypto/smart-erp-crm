package com.crmbank.erp.comm.handler;

import com.crmbank.erp.comm.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 🚀 전역 예외 처리기 (ERP 통합 표준)
 * 모든 비즈니스 예외 및 시스템 오류를 ApiResponse 구조로 통일하여 반환합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("🔥 [전역 예외 발생] : {}", e.getMessage(), e);
        // 모든 에러 메시지를 ApiResponse 표준 구조로 반환하여 프론트엔드 일관성 유지
        return ResponseEntity.internalServerError()
                .body(ApiResponse.serverError(e.getMessage()));
    }
}
