package com.crmbank.erp.crm.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface CustomQueryMapper {
    List<Map<String, Object>> selectQueryList(Map<String, Object> params);
    int insertQuery(Map<String, Object> params);
    int updateQuery(Map<String, Object> params);
    int deleteQuery(Map<String, Object> params);
    List<Map<String, Object>> selectCustomQueryExecution(Map<String, Object> params);
}
