package com.crmbank.erp.hsaa.dto;

import lombok.Data;

@Data
public class Hsaa600tDto {
    private String cmpycd;
    private String yymm;
    private String deptcd;
    private String userid;
    private Long contactcnt;
    private Long consultcnt;
    private Long salesidcnt;
    private Long planamt;
    private String addtime;
    private String updtime;
    private String updemp;
    
    // For UI display and mapping
    private String mm;
}
