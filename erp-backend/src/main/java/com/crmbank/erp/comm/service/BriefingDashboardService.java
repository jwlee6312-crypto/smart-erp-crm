package com.crmbank.erp.comm.service;

import com.crmbank.erp.comm.mapper.BriefingDashboardMapper;
import com.crmbank.erp.crm.service.GeminiAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BriefingDashboardService {

    private final BriefingDashboardMapper briefingMapper;
    private final GeminiAiService geminiAiService;

    public Map<String, Object> getBriefingData(String cmpycd) {
        Map<String, Object> params = Map.of("cmpycd", cmpycd);
        Map<String, Object> result = new HashMap<>();

        result.put("stats", briefingMapper.selectBriefingStats(params));
        result.put("feed", briefingMapper.selectBriefingFeed(params));
        result.put("notices", briefingMapper.selectBriefingNotices(params));
        result.put("expiry", briefingMapper.selectExpiryAlarms(params));

        result.put("aiBriefing", generateAiInsight(result));

        return result;
    }

    private String generateAiInsight(Map<String, Object> data) {
        try {
            String context = String.format("운영현황: %s", data.get("stats"));
            String prompt = context + "\n시급한 업무 1개와 격려 멘트 1개를 한국어로 요약해줘.";
            return geminiAiService.summarizeText(prompt);
        } catch (Exception e) {
            return "지표 분석 중입니다.";
        }
    }
}
