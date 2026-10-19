package com.crmbank.erp.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SalesOrderRequest {
    @SerializedName("salesOrderMstDto")
    public SalesOrderMstDto salesOrderMstDto;

    @SerializedName("salesOrderDtlDto")
    public List<SalesOrderDtlDto> salesOrderDtlDto;

    public SalesOrderRequest(SalesOrderMstDto salesOrderMstDto, List<SalesOrderDtlDto> salesOrderDtlDto) {
        this.salesOrderMstDto = salesOrderMstDto;
        this.salesOrderDtlDto = salesOrderDtlDto;
    }
}