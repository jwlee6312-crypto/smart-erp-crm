package com.crmbank.erp.haaa.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface HaaaMapper {
    List<Map<String, Object>> HAAA_010U_STR(Map<String, Object> params);
    List<Map<String, Object>> HAAA_800U_STR(Map<String, Object> params);
    List<Map<String, Object>> HAAA_810U_STR(Map<String, Object> params);
}
