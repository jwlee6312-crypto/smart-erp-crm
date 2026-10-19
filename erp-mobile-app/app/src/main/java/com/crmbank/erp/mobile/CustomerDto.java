package com.crmbank.erp.mobile;

import com.google.gson.annotations.SerializedName;

public class CustomerDto {
    @SerializedName("custcd")
    public String custcd;

    @SerializedName("custnm")
    public String custnm;

    @Override
    public String toString() {
        return custnm; // 由ъ뒪???쒖떆??
    }
}