package com.crmbank.erp.crm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.Map;

/**
 * 캠페인 전화 발신 대상 목록 (CAMPAIGN_CALL_LIST) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampCallListDto {
    private Integer call_seq;        // 전화목록 고유번호
    private String cmpycd;           // 회사코드
    private String camp_no;          // 캠페인번호
    private String surv_gb;          // 설문유형
    private String cust_nm;          // 고객명
    private String email;            // 메일주소
    private String tel_no;           // 연락처
    private String status;           // 상담상태코드
    private String status_nm;        // 상담상태명
    private String rslt_cd;          // 상담결과코드
    private String rslt_nm;          // 상담결과명
    private String userid;           // 배정상담원
    private Map<String, Object> ext_data; // 가변 속성 데이터 (JSON)
    private Integer read_cnt;        // 조회횟수
    private String redial_yn;        // 재통화필요여부
    private String resv_dtime;       // ✅ 약속시간 (추가)
    private String resv_memo;        // ✅ 예약메모 (추가)
    private String updemp;           // 수정자
}
