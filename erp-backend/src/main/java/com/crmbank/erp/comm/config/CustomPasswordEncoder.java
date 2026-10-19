package com.crmbank.erp.comm.config;

import com.crmbank.erp.comm.util.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 커스텀 패스워드 인코더 (config 패키지로 이동)
 */
public class CustomPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        return SecurityUtil.encryptSha256(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        String encrypted = SecurityUtil.encryptSha256(rawPassword.toString());
        return encrypted.equals(encodedPassword);
    }
}
