package com.crmbank.erp.mobile;

import java.util.List;

public class PurchOrderRequest {
    private PurchOrderMstDto purchOrderMstDto;
    private List<PurchOrderDtlDto> purchOrderDtlDto;

    public PurchOrderRequest() {}

    public PurchOrderRequest(PurchOrderMstDto purchOrderMstDto, List<PurchOrderDtlDto> purchOrderDtlDto) {
        this.purchOrderMstDto = purchOrderMstDto;
        this.purchOrderDtlDto = purchOrderDtlDto;
    }

    public PurchOrderMstDto getPurchOrderMstDto() {
        return purchOrderMstDto;
    }

    public void setPurchOrderMstDto(PurchOrderMstDto purchOrderMstDto) {
        this.purchOrderMstDto = purchOrderMstDto;
    }

    public List<PurchOrderDtlDto> getPurchOrderDtlDto() {
        return purchOrderDtlDto;
    }

    public void setPurchOrderDtlDto(List<PurchOrderDtlDto> purchOrderDtlDto) {
        this.purchOrderDtlDto = purchOrderDtlDto;
    }
}
