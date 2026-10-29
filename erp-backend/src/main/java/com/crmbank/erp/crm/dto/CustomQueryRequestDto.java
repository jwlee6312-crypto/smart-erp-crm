package com.crmbank.erp.crm.dto;

import lombok.Data;
import java.util.List;

@Data
public class CustomQueryRequestDto {
    private String campno;
    private List<ConditionItem> conditions;

    @Data
    public static class ConditionItem {
        private String survno;
        private String ansno;
        private String keyword;
        private String operator;
    }
}
