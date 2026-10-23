package com.crmbank.erp.comm.controller;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface ErpPopupMapper {
    List<Map<String, Object>> selectCustomerList(Map<String, Object> params);
    List<Map<String, Object>> selectItemList(Map<String, Object> params);
    List<Map<String, Object>> selectCodeList(Map<String, Object> params);
}
