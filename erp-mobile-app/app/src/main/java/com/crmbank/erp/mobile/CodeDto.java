package com.crmbank.erp.mobile;

import com.google.gson.annotations.SerializedName;

/**
 * 💡 통합 백엔드 공통 코드 DTO
 */
public class CodeDto {
    @SerializedName("codecd")
    public String codecd;

    @SerializedName("codenm")
    public String codenm;

    public String getCodecd() { 
        return codecd != null ? codecd.trim() : ""; 
    }

    public String getCodenm() { 
        return codenm != null ? codenm.trim() : ""; 
    }

    @Override
    public String toString() { 
        return getCodenm(); 
    }
}
