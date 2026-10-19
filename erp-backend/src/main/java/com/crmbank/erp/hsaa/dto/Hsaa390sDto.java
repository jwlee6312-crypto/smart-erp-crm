package com.crmbank.erp.hsaa.dto;

import lombok.Data;

@Data
public class Hsaa390sDto {
    private String deptcd;
    private String deptnm;
    private String userid;
    private String usernm;
    
    // 금주방문실적
    private Integer a01cnt; // 개척
    private Integer a02cnt; // 반복
    private Integer a03cnt; // 대리점
    
    // 금주변동실적
    private Integer a4001cnt; // 설치(기업)
    private Integer a4002cnt; // 설치(개인)
    private Integer a9001cnt; // 성공(기업)
    private Integer a9002cnt; // 성공(개인)
    private Integer a910cnt;   // 실패
    private Integer a920cnt;   // 보류
    
    // 차주상담계획
    private Integer b01cnt;
    
    // 차주변동계획
    private Integer b4001cnt; // 설치(기업)
    private Integer b4002cnt; // 설치(개인)
    private Integer a9003cnt; // 수주예정
    private Integer b9001cnt; // 성공(기업)
    private Integer b9002cnt; // 성공(개인)
    
    // 설치후미수주
    private Integer c990cnt; // 기업
    private Integer c999cnt; // 개인
}
