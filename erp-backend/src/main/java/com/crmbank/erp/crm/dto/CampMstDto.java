package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 캠페인 마스터 (CAMPAIGN_MST) DTO
 * 💡 캠페인구분(CAMP_GB)을 설문유형(SURV_GB)으로 통합 관리함
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampMstDto {
    private String cmpycd;           // 회사코드
    private String camp_no;          // 캠페인번호
    private String camp_nm;          // 캠페인명
    private String surv_gb;          // 설문유형 겸 캠페인구분 (통합 필드)
    private Long   tot_db_cnt;         // 총DB수
    private Long   succ_db_cnt;        // 성공DB수
    private String useyn;            // 사용여부
    private String callback_yn;      // 콜백여부
    private String remark;           // 비고
    private String actdate;          // 실행일자 (YYYYMMDD)
    private String status;           // 실행상태 (010:대기, 020:진행, 030:완료)
    private String start_ment;       // 시작 인사말
    private String end_ment;         // 종료 인사말
    private String connect_url;      // 연결URL
    private String sms_ment;         // SMS멘트
    private LocalDateTime addtime;   // 등록일시
    private LocalDateTime updtime;   // 수정일시
    private String updemp;           // 수정자/상담원ID
}
