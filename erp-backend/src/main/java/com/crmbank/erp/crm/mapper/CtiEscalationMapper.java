package com.crmbank.erp.crm.mapper;

import com.crmbank.erp.crm.dto.CtiEscalationDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface CtiEscalationMapper {
    int insertEscalation(CtiEscalationDto dto);
    int updateEscalation(CtiEscalationDto dto);
    int deleteEscalation(Integer escalation_no);
    List<CtiEscalationDto> selectEscalationList(Map<String, Object> params);
    CtiEscalationDto selectEscalation(Integer escalation_no);
}
