package com.crmbank.erp.hs00.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface Hs00Mapper {
    /**
     * HS00_000S_STR: 구매/영업 공통 코드 조회
     */
    List<Map<String, Object>> HS00_000S_STR(Map<String, Object> params);
    /**
     * HS00_150S_STR: 거래처 조회
     */
    List<Map<String, Object>> HS00_150S_STR(Map<String, Object> params);
}
