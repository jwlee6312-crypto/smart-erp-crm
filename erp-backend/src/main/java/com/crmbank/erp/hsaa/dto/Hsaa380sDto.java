package com.crmbank.erp.hsaa.dto;

import lombok.Data;

@Data
public class Hsaa380sDto {
    private String deptcd;
    private String deptnm;
    private String userid;
    private String usernm;
    private String custgbn;
    private String itemcd;
    private String itemnm;
    
    // 당기실적 (Previous week)
    private Integer s100;
    private Integer s200;
    private Integer s300;
    private Integer s400;
    private Integer s900;
    private Integer s910;
    private Integer s920;
    private Integer s930;
    
    // 차기계획 (Next week)
    private Integer n400;
    private Integer n900;
    
    // 설치 후 미수주누적
    private Integer n999;
}
