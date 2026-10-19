package com.crmbank.erp.haba.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface HabaMapper {
    List<Map<String, Object>> HABA_YYYY_S(Map<String, Object> params);
    List<Map<String, Object>> HABA_010U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_020U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_021U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_022U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_030U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_040U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_050U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_060U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_070U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_071U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_080U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_090U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_100U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_110U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_120U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_130U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_140U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_150U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_160U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_170U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_180U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_190S_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_210U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_220U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_230U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_240U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_250U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_260U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_510U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_900U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_910U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_920U_STR(Map<String, Object> params);
    List<Map<String, Object>> HABA_935U_STR(Map<String, Object> params);
}
