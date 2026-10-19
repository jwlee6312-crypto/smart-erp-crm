package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 질문 상세/답변 (SURV_DTL) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurvDtlDto {
    private String cmpycd;           // 회사코드
    private String surv_no;          // 질문번호
    private String ans_no;           // 답변번호
    private String ans_cont;         // 답변내용
    private Integer ans_point;       // 답변점수
    private String useyn;            // 사용여부
    private LocalDateTime addtime;   // 등록일시
    private LocalDateTime updtime;   // 수정일시
    private String updemp;           // 수정자
    private String essay_yn;         // 주관식여부
}
