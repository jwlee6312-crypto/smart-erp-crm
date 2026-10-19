package com.crmbank.erp.hsaa.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Hsaa810tDto {
    private String cmpycd;
    private String custcd;
    private String salesid;
    private String ser;
    private String itemcd;
    private String itemnm;
    private BigDecimal qty;
    private BigDecimal unitprice;
    private BigDecimal amt;
    
    private String addtime;
    private String updtime;
    private String updemp;
}
