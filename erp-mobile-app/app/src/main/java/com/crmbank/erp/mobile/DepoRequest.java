package com.crmbank.erp.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DepoRequest {
    @SerializedName("depo_MstDto")
    public Depo_MstDto depo_MstDto;

    @SerializedName("depo_DtlDto")
    public List<Depo_DtlDto> depo_DtlDto;

    public DepoRequest(Depo_MstDto mst, List<Depo_DtlDto> dtls) {
        this.depo_MstDto = mst;
        this.depo_DtlDto = dtls;
    }
}
