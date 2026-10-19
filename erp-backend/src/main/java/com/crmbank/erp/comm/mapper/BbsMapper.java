package com.crmbank.erp.comm.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface BbsMapper {
    List<Map<String, Object>> selectBbsList(Map<String, Object> params);
    int countBbsList(Map<String, Object> params);
    Map<String, Object> selectBbsDetail(Map<String, Object> params);
    void insertBbs(Map<String, Object> params);
    void updateBbs(Map<String, Object> params);
    void deleteBbs(Map<String, Object> params);
    void updateReadCount(Map<String, Object> params);
}
