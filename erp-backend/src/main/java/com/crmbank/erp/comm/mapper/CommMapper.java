package com.crmbank.erp.comm.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface CommMapper {
    Map<String, Object> GET_COMPANY_INFO(Map<String, Object> param);
    Map<String, Object> GET_USER_INFO(Map<String, Object> param);
    Map<String, Object> GET_MENU_CONFIG(Map<String, Object> param);
    List<Map<String, Object>> GET_TOP_MENU_LIST();
    List<Map<String, Object>> GET_TOP_MENU_MOBILE_LIST();
    List<Map<String, Object>> HA00_200S_STR(Map<String, Object> param);
    List<Map<String, Object>> GET_PROGRAM_LIST(Map<String, Object> param);

    List<Map<String, Object>> HABA_100U_STR(Map<String, Object> param);
    List<Map<String, Object>> HABA_030U_STR(Map<String, Object> param);
    List<Map<String, Object>> HABA_900U_STR(Map<String, Object> param);

    void INSERT_LOGIN_HISTORY(Map<String, Object> param);
    void DELETE_USER_CONN(Map<String, Object> param);
}
