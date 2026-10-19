package com.crmbank.erp.mobile;

import com.google.gson.annotations.SerializedName;

public class ItemSearchRequest {
    @SerializedName("cmpycd")
    private String cmpycd;

    @SerializedName("nacd")
    private String nacd;

    @SerializedName("sch_itemnm")
    private String sch_itemnm;

    @SerializedName("sch_astkind")
    private String sch_astkind;

    public ItemSearchRequest(String cmpycd, String nacd, String sch_itemnm, String sch_astkind) {
        this.cmpycd = cmpycd;
        this.nacd = nacd;
        this.sch_itemnm = sch_itemnm;
        this.sch_astkind = sch_astkind;
    }
}