package com.crmbank.erp.hsaa.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Hsaa370sDto {
    private String deptcd;
    private String deptnm;
    private String userid;
    private String usernm;
    
    private Integer totcnt;
    private Integer wincdcnt;
    private Integer failcdcnt;
    private Integer holdcnt;
    
    // Percentages (Calculated in Service or UI)
    private Double succrate;
    private Double failrate;
    private Double holdrate;
}
