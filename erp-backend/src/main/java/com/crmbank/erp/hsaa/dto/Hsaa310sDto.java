package com.crmbank.erp.hsaa.dto;

import lombok.Data;

@Data
public class Hsaa310sDto {
    private String deptcd;
    private String deptnm;
    private String userid;
    private String usernm;
    
    // Monthly Enterprise (010) counts
    private Integer m01; private Integer m02; private Integer m03; private Integer m04;
    private Integer m05; private Integer m06; private Integer m07; private Integer m08;
    private Integer m09; private Integer m10; private Integer m11; private Integer m12;
    private Integer mtotcnt;

    // Monthly Individual (020) counts
    private Integer s01; private Integer s02; private Integer s03; private Integer s04;
    private Integer s05; private Integer s06; private Integer s07; private Integer s08;
    private Integer s09; private Integer s10; private Integer s11; private Integer s12;
    private Integer stotcnt;

    private Integer totcnt;
}
