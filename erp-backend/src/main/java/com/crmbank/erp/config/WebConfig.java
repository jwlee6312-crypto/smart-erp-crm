package com.crmbank.erp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 🚀 중요: 비어있는 핸들러를 등록하거나 기본 정적 리소스 매핑을 무력화합니다.
        // 스프링 부트가 내장 리소스 핸들러로 /storage/** 나 /** 를 선점하는 것을 방지합니다.
        registry.addResourceHandler("/null-resource-path/**")
                .addResourceLocations("file:/dev/null/");
    }
}