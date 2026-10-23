package com.crmbank.erp.hsqt.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface HsqtMapper {
    List<Map<String, Object>> selectQuotationMaster(Map<String, Object> params);
    List<Map<String, Object>> selectQuotationDetail(Map<String, Object> params);
    List<Map<String, Object>> selectQuotationStatus(Map<String, Object> params);
    
    void insertQuotationMaster(Map<String, Object> params);
    void updateQuotationMaster(Map<String, Object> params);
    void deleteQuotationMaster(Map<String, Object> params);

    void insertQuotationDetail(Map<String, Object> params);
    void updateQuotationDetail(Map<String, Object> params);
    void deleteQuotationDetail(Map<String, Object> params);
    
    String selectMaxQuatNo(@Param("cmpycd") String cmpycd, @Param("quatym") String quatym);
    String selectMaxRowNo(@Param("cmpycd") String cmpycd, @Param("quatym") String quatym, @Param("quatno") String quatno);
}
