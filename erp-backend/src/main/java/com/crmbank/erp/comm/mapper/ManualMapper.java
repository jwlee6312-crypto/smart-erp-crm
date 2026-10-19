package com.crmbank.erp.comm.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.Map;

@Mapper
public interface ManualMapper {
    Map<String, Object> getManual(String progid);
    int saveManual(Map<String, Object> params);
}
