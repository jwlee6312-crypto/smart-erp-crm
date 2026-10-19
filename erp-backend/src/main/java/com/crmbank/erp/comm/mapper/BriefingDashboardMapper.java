package com.crmbank.erp.comm.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface BriefingDashboardMapper {
    Map<String, Object> selectBriefingStats(Map<String, Object> params);
    List<Map<String, Object>> selectBriefingFeed(Map<String, Object> params);
    List<Map<String, Object>> selectBriefingNotices(Map<String, Object> params);
    List<Map<String, Object>> selectExpiryAlarms(Map<String, Object> params);
}
