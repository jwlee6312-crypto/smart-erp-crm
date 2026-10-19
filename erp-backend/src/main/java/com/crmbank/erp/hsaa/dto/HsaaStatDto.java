package com.crmbank.erp.hsaa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HsaaStatDto {
    private String gubun;
    private String code;
    private String code1;
    private String code2;
    private BigDecimal val;
}
