package com.crmbank.erp.global.handler;

import com.crmbank.erp.comm.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 🔐 MyBatis AES 양방향 암호화 Type Handler
 * DB에 저장할 때는 암호화, 조회할 때는 복호화하여 자바 객체에 매핑합니다.
 */
@Slf4j
@Component
public class AesTypeHandler extends BaseTypeHandler<String> {

    private static String AES_KEY;

    @Value("${AESKEY}") // 💡 기본값을 제거하여 설정 누락 시 서버 기동 실패하도록 강화
    public void setAesKey(String key) {
        AES_KEY = key;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, SecurityUtil.encryptAes(parameter, AES_KEY));
        } catch (Exception e) {
            log.error("❌ [AES 암호화 에러] : {}", e.getMessage());
            ps.setString(i, parameter);
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return decrypt(rs.getString(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return decrypt(rs.getString(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return decrypt(cs.getString(columnIndex));
    }

    private String decrypt(String value) {
        if (value == null || value.isEmpty()) return value;
        try {
            return SecurityUtil.decryptAes(value, AES_KEY);
        } catch (Exception e) {
            // 암호화되지 않은 기존 데이터의 경우 복호화 실패 시 원본 반환
            log.warn("⚠️ [AES 복호화 실패] (기존 데이터 가능성) : {}", e.getMessage());
            return value;
        }
    }
}
