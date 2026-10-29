package com.crmbank.erp.crm.service;

import com.crmbank.erp.crm.mapper.CustomQueryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomQueryService {

    private final CustomQueryMapper customQueryMapper;

    public List<Map<String, Object>> getQueryList(Map<String, Object> params) {
        return customQueryMapper.selectQueryList(params);
    }

    @Transactional
    public void saveQuery(Map<String, Object> params) {
        String queryId = (String) params.get("query_id");
        if (queryId == null || queryId.trim().isEmpty()) {
            queryId = "Q-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            params.put("query_id", queryId);
        }
        int cnt = customQueryMapper.updateQuery(params);
        if (cnt == 0) {
            customQueryMapper.insertQuery(params);
        }
    }

    @Transactional
    public void deleteQuery(Map<String, Object> params) {
        customQueryMapper.deleteQuery(params);
    }

    public List<Map<String, Object>> executeCustomQuery(Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> conditions = (List<Map<String, Object>>) params.get("conditions");

        StringBuilder sb = new StringBuilder("1=1");
        if (conditions != null && !conditions.isEmpty()) {
            for (int i = 0; i < conditions.size(); i++) {
                Map<String, Object> cond = conditions.get(i);
                if (cond == null) continue;

                String op = String.valueOf(cond.getOrDefault("operator", "AND"));
                if (i > 0) {
                    sb.append(" ").append("OR".equalsIgnoreCase(op) ? "OR" : "AND").append(" ");
                } else {
                    sb.append(" AND ");
                }

                sb.append("(");
                String survno = String.valueOf(cond.getOrDefault("survno", ""));
                String ansno = String.valueOf(cond.getOrDefault("ansno", ""));
                String keyword = String.valueOf(cond.getOrDefault("keyword", ""));

                sb.append("EXISTS (SELECT 1 FROM campaign_rslt_dtl rd LEFT JOIN surv_dtl sd ON rd.cmpycd = sd.cmpycd AND rd.surv_no = sd.surv_no AND rd.ans_no = sd.ans_no WHERE rd.cmpycd = rm.cmpycd AND rd.rslt_no = rm.rslt_no");
                if (!survno.isEmpty() && !"null".equals(survno)) {
                    sb.append(" AND rd.surv_no = '").append(survno.replace("'", "''")).append("'");
                }
                if (!ansno.isEmpty() && !"null".equals(ansno)) {
                    sb.append(" AND rd.ans_no = '").append(ansno.replace("'", "''")).append("'");
                }
                if (!keyword.isEmpty() && !"null".equals(keyword)) {
                    String safeKw = keyword.replace("'", "''");
                    sb.append(" AND (sd.ans_cont LIKE '%").append(safeKw).append("%' OR rd.remark LIKE '%").append(safeKw).append("%')");
                }
                sb.append(")");
                sb.append(")");
            }
        }
        params.put("dynamicWhere", sb.toString());
        return customQueryMapper.selectCustomQueryExecution(params);
    }
}
