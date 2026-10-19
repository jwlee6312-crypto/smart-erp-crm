package com.crmbank.erp.crm.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 상담 통합 저장 요청 DTO
 * 완전 소문자 표준화 적용 및 interaction_id 필드 추가
 */
@Data
public class ConsultSaveRequest {
    private String cmpycd;
    private String camp_no;
    private int call_seq;
    private String userid;
    private String line_num;      // 내선번호
    private String cust_email;
    private String cust_nm;
    private String call_telno;    // 고객연락처
    private String rslt_cd;
    private String memo;
    private String resv_dtime;
    private String resv_memo;
    private List<Map<String, Object>> surveys; 
    private String chat_history;
    private String rec_file;
    private String start_time;
    private String end_time;
    private String media_type;
    private String interaction_id; // 통합 상담 고유 ID 추가
}
