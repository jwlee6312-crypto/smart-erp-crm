package com.crmbank.erp.ha00.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface Ha00Mapper {
    /**
     * HA00_00P_STR: 공통 팝업/도움창 데이터 조회
     */
    List<Map<String, Object>> HA00_00P_STR(Map<String, Object> params);
    
    /**
     * HA00_010S_STR: 공통 코드 조회
     */
    List<Map<String, Object>> HA00_010S_STR(Map<String, Object> params);

    /**
     * 💡 [이전] 공통 코드 테이블(haba900t_tbl) 조회
     */
    List<Map<String, Object>> GET_CODE_LIST(@Param("cdtype") String cdType);
}
