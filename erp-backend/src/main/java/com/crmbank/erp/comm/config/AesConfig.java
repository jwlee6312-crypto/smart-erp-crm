package com.crmbank.erp.comm.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 암호화 Key 설정 클래스 (config 패키지로 이동)
 */
@Configuration
@Getter
public class AesConfig {

    @Value("${AESKEY}")
    private String aesKey;
}
