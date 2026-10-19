package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 캠페인 질문 설정 (CAMP_SURVEY) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampSurveyDto {
    private String cmpycd;           // 회사코드
    private String camp_gb;          // 캠페인구분
    private String surv_gb;          // 설문구분
    private String surv_no;          // 질문번호
    private String sortcd;           // 정렬코드
    private String useyn;            // 사용여부
    private LocalDateTime addtime;   // 등록일시
    private LocalDateTime updtime;   // 수정일시
    private String updemp;           // 수정자
}
