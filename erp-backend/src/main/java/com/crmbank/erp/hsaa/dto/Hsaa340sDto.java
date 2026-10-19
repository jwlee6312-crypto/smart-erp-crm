package com.crmbank.erp.hsaa.dto;

import lombok.Data;

@Data
public class Hsaa340sDto {
    private String deptcd;
    private String deptnm;
    private String userid;
    private String usernm;
    
    private Integer s100; // 고객선정
    private Integer s200; // 고객접촉
    private Integer s300; // 제안견적
    private Integer s400; // 설치
    private Integer s900; // 성공
    private Integer s910; // 실패
    private Integer s920; // 보류
    private Integer s930; // 포기
}
