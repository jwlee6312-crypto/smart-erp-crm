package com.crmbank.erp.crm.service;

import lombok.RequiredArgsConstructor;
import com.crmbank.erp.crm.dto.CtiEscalationDto;
import com.crmbank.erp.crm.mapper.inbound.InboundMapper; // 💡 InboundMapper로 통합 사용
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CtiEscalationService {

    private final InboundMapper inboundMapper;

    @Transactional
    public int registEscalation(CtiEscalationDto dto) {
        return inboundMapper.insertEscalation(dto);
    }

    @Transactional
    public int modifyEscalation(CtiEscalationDto dto) {
        // XML에 updateEscalation이 정의되어 있는지 확인 필요
        return 0; 
    }

    @Transactional
    public int removeEscalation(Integer escalationNo) {
        // XML에 deleteEscalation이 정의되어 있는지 확인 필요
        return 0;
    }

    public List<CtiEscalationDto> getEscalationList(Map<String, Object> params) {
        // XML에 selectEscalationList가 정의되어 있는지 확인 필요
        return List.of();
    }
}
