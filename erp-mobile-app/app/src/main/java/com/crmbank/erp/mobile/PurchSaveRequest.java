package com.crmbank.erp.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// ???대옒?ㅻ뒗 PurchOrderRequest濡??泥대릺?덉뒿?덈떎. 異뷀썑 ??젣?섏꽭??
public class PurchSaveRequest {
    @SerializedName("mst")
    public PurchOrderMstDto mst;

    @SerializedName("dtl")
    public List<PurchOrderDtlDto> dtl;

    public PurchSaveRequest(PurchOrderMstDto mst, List<PurchOrderDtlDto> dtl) {
        this.mst = mst;
        this.dtl = dtl;
    }
}