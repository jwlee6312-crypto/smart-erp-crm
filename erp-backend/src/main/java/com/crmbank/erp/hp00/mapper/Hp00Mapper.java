package com.crmbank.erp.hp00.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface Hp00Mapper {
    /**
     * HP00_000S_STR: 생산/공정 공통 코드 조회
     */
    List<Map<String, Object>> HP00_000S_STR(Map<String, Object> params);
}
