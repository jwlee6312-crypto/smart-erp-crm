package com.crmbank.erp.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class CalcRequest {
    // 諛깆뿏??getCalc_MstDto() / getCalc_DtlDto() 紐낆꽭??留욎땄
    @SerializedName("calc_MstDto")
    public Map<String, Object> calcMstDto;

    @SerializedName("calc_DtlDto")
    public List<Map<String, Object>> calcDtlDto;

    public CalcRequest(Map<String, Object> calcMstDto, List<Map<String, Object>> calcDtlDto) {
        this.calcMstDto = calcMstDto;
        this.calcDtlDto = calcDtlDto;
    }
}
