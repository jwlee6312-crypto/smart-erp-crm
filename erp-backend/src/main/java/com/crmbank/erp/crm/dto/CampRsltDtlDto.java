package com.crmbank.erp.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 캠페인 전화상담결과 상세 (CAMPAIGN_RSLT_DTL) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampRsltDtlDto {
    private String cmpycd;           // 회사코드
    private Integer rslt_no;         // 상담결과번호
    private String surv_no;          // 질문번호
    private String ans_no;           // 답변번호
    private BigDecimal point;        // 점수
    private String remark;           // 비고
    private LocalDateTime addtime;   // 등록일시
    private LocalDateTime updtime;   // 수정일시
    private String updemp;           // 수정자
}
