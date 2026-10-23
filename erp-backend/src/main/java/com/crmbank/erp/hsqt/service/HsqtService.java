package com.crmbank.erp.hsqt.service;

import com.crmbank.erp.hsqt.mapper.HsqtMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HsqtService {

    private final HsqtMapper hsqtMapper;

    public List<Map<String, Object>> getQuotationMaster(Map<String, Object> params) {
        return hsqtMapper.selectQuotationMaster(params);
    }

    public List<Map<String, Object>> getQuotationDetail(Map<String, Object> params) {
        return hsqtMapper.selectQuotationDetail(params);
    }

    public List<Map<String, Object>> getQuotationStatus(Map<String, Object> params) {
        return hsqtMapper.selectQuotationStatus(params);
    }

    @Transactional
    public void saveQuotation(Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        Map<String, Object> master = (Map<String, Object>) payload.get("master");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> details = (List<Map<String, Object>>) payload.get("details");

        if (master == null) return;

        // Normalize master fields between frontend and backend
        if (master.get("quatymd") == null && master.get("qutdt") != null) {
            master.put("quatymd", String.valueOf(master.get("qutdt")).replace("-", ""));
        }
        String quatymd = String.valueOf(master.getOrDefault("quatymd", ""));
        if (quatymd.length() >= 6 && (master.get("quatym") == null || String.valueOf(master.get("quatym")).isEmpty())) {
            master.put("quatym", quatymd.substring(0, 6));
        }
        if (master.get("quatno") == null && master.get("qutno") != null) {
            master.put("quatno", master.get("qutno"));
        }
        if (master.get("remark") == null && master.get("remk") != null) {
            master.put("remark", master.get("remk"));
        }

        String cmpycd = String.valueOf(master.getOrDefault("cmpycd", "COIT"));
        String quatym = String.valueOf(master.getOrDefault("quatym", "202610"));
        String quatno = String.valueOf(master.getOrDefault("quatno", ""));

        if (quatno == null || quatno.isEmpty() || "0000".equals(quatno) || "null".equals(quatno)) {
            quatno = hsqtMapper.selectMaxQuatNo(cmpycd, quatym);
            master.put("quatno", quatno);
            master.put("qutno", quatno);
            hsqtMapper.insertQuotationMaster(master);
        } else {
            try {
                hsqtMapper.updateQuotationMaster(master);
            } catch (Exception e) {
                hsqtMapper.insertQuotationMaster(master);
            }
        }

        if (details != null) {
            for (Map<String, Object> detail : details) {
                detail.put("cmpycd", cmpycd);
                detail.put("quatym", quatym);
                detail.put("quatno", quatno);

                // Normalize detail fields
                if (detail.get("quatqty") == null && detail.get("quty") != null) {
                    detail.put("quatqty", detail.get("quty"));
                }
                if (detail.get("quatamt") == null) {
                    if (detail.get("uprc") != null) {
                        detail.put("quatamt", detail.get("uprc"));
                    } else if (detail.get("amtx") != null) {
                        detail.put("quatamt", detail.get("amtx"));
                    } else {
                        detail.put("quatamt", 0);
                    }
                }
                if (detail.get("quatvat") == null) {
                    detail.put("quatvat", 0);
                }
                if (detail.get("pchamt") == null) {
                    detail.put("pchamt", 0);
                }
                if (detail.get("cost") == null) {
                    detail.put("cost", 0);
                }
                if (detail.get("profit") == null) {
                    detail.put("profit", 0);
                }

                String qrowno = String.valueOf(detail.getOrDefault("qrowno", ""));

                if (qrowno == null || qrowno.isEmpty() || "null".equals(qrowno)) {
                    qrowno = hsqtMapper.selectMaxRowNo(cmpycd, quatym, quatno);
                    detail.put("qrowno", qrowno);
                    hsqtMapper.insertQuotationDetail(detail);
                } else {
                    try {
                        hsqtMapper.updateQuotationDetail(detail);
                    } catch (Exception e) {
                        hsqtMapper.insertQuotationDetail(detail);
                    }
                }
            }
        }
    }

    @Transactional
    public void deleteQuotation(Map<String, Object> params) {
        if (params.get("quatno") == null && params.get("qutno") != null) {
            params.put("quatno", params.get("qutno"));
        }
        if (params.get("quatym") == null && params.get("quatymd") != null) {
            String ymd = String.valueOf(params.get("quatymd")).replace("-", "");
            if (ymd.length() >= 6) params.put("quatym", ymd.substring(0, 6));
        }
        hsqtMapper.deleteQuotationDetail(params);
        hsqtMapper.deleteQuotationMaster(params);
    }
}
