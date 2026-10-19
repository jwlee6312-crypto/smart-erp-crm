package com.crmbank.erp.mobile;

import com.google.gson.annotations.SerializedName;

public class ItemDto {
    @SerializedName("itemcd")
    public String itemcd;

    @SerializedName("itemnm")
    public String itemnm;

    @SerializedName("price") // ?ㅼ젣 ?④? ?꾨뱶紐낆뿉 留욊쾶 ?섏젙 ?꾩슂 (?? iprice, oprice ??
    public int price;

    // ?꾩슂???꾨뱶 異붽? 媛??(洹쒓꺽, ?⑥쐞 ??
    
    @Override
    public String toString() {
        return itemcd + " - " + itemnm; // 由ъ뒪???쒖떆??
    }
}