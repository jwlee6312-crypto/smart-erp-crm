package com.crmbank.erp.comm.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 🚀 [전역 데이터 세척기]
 * 시스템으로 들어오는 모든 RequestBody(JSON) 내의 문자열에서 
 * SQL 실행을 방해하는 Null 문자(\u0000) 및 특수 공백(\u00a0)을 근본적으로 제거합니다.
 */
@Slf4j
@RestControllerAdvice
public class StringSanitizerAdvice extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true; // 모든 컨트롤러의 RequestBody에 적용
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return sanitize(body);
    }

    private Object sanitize(Object obj) {
        if (obj == null) return null;

        if (obj instanceof String) {
            return cleanString((String) obj);
        }

        if (obj instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) obj;
            map.entrySet().forEach(entry -> {
                entry.setValue(sanitize(entry.getValue()));
            });
            return map;
        }

        if (obj instanceof List) {
            List<Object> list = (List<Object>) obj;
            return list.stream().map(this::sanitize).collect(Collectors.toList());
        }

        // 일반 DTO 객체의 필드 세척은 Jackson 모듈 설정을 통해 처리하는 것이 좋으나, 
        // 현재 프로젝트의 주요 통신 수단인 Map/List/String에 대해서만 우선 완벽히 처리합니다.
        return obj;
    }

    private String cleanString(String str) {
        if (str == null) return null;
        // 1. Null 문자(\u0000) 제거
        // 2. 특수 공백(\u00a0, NBSP)을 일반 공백으로 변환
        // 3. 앞뒤 공백 제거
        return str.replace("\u0000", "")
                  .replace("\u00a0", " ")
                  .trim();
    }
}
