package com.crmbank.erp.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class InoutRequest {
    // 諛깆뿏???먮윭 濡쒓렇??湲곕컲?섏뿬 ?꾨뱶紐??섏젙 (mst -> inout_MstDto)
    @SerializedName("inout_MstDto")
    public Map<String, Object> mst;

    // dtl -> inout_DtlDto (諛깆뿏??愿濡??留욎땄)
    @SerializedName("inout_DtlDto")
    public List<Map<String, Object>> dtl;

    public InoutRequest(Map<String, Object> mst, List<Map<String, Object>> dtl) {
        this.mst = mst;
        this.dtl = dtl;
    }
}
