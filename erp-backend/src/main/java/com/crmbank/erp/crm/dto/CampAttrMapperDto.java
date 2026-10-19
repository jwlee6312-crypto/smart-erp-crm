package com.crmbank.erp.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 캠페인 속성 매핑 정의 (CAMP_ATTR_MAPPER) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampAttrMapperDto {
    private String cmpycd;      // 회사코드
    private String surv_gb;     // 설문유형
    private String surv_gb_nm;  // 설문유형명 (조회용)
    private String attr_nm;     // 엑셀 헤더명 (한글)
    private String attr_key;    // 표준 영문 키
    private String data_type;   // 데이터타입
    private String stats_yn;    // 통계 활용 여부
    private String useyn;       // 사용여부
    private LocalDateTime addtime;
    private LocalDateTime updtime;
    private String updemp;      // 수정자
}
