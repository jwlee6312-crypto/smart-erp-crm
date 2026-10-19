package com.crmbank.erp.comm.service;

import com.crmbank.erp.comm.mapper.BbsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BbsService {

    private final BbsMapper bbsMapper;

    public Map<String, Object> getBbsList(Map<String, Object> params) {
        int page = Integer.parseInt(String.valueOf(params.getOrDefault("page", "1")));
        int limit = Integer.parseInt(String.valueOf(params.getOrDefault("limit", "15")));
        int offset = (page - 1) * limit;

        params.put("offset", offset);
        params.put("limit", limit);

        Map<String, Object> result = new HashMap<>();
        result.put("list", bbsMapper.selectBbsList(params));
        result.put("total", bbsMapper.countBbsList(params));
        return result;
    }

    @Transactional
    public Map<String, Object> getBbsDetail(Map<String, Object> params) {
        bbsMapper.updateReadCount(params);
        return bbsMapper.selectBbsDetail(params);
    }

    @Transactional
    public void saveBbs(Map<String, Object> params) {
        String boardid = String.valueOf(params.getOrDefault("boardid", ""));
        if (boardid == null || boardid.isEmpty() || "null".equals(boardid)) {
            bbsMapper.insertBbs(params);
        } else {
            bbsMapper.updateBbs(params);
        }
    }

    @Transactional
    public void deleteBbs(Map<String, Object> params) {
        bbsMapper.deleteBbs(params);
    }
}
