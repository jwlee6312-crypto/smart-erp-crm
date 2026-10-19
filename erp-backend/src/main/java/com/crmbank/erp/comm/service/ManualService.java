package com.crmbank.erp.comm.service;

import com.crmbank.erp.comm.mapper.ManualMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ManualService {

    private final ManualMapper manualMapper;

    public Map<String, Object> getManual(String progid) {
        return manualMapper.getManual(progid);
    }

    @Transactional("erpTransactionManager")
    public void saveManual(Map<String, Object> params) {
        manualMapper.saveManual(params);
    }
}
