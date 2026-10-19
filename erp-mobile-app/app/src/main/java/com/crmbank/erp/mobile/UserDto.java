package com.crmbank.erp.mobile;

import com.google.gson.annotations.SerializedName;

public class UserDto {
    @SerializedName("USERID")
    public String userid;
    
    @SerializedName("USERNM")
    public String usernm;
    
    @SerializedName("DEPTCD")
    public String deptcd;
    
    @SerializedName("DEPTNM")
    public String deptnm;
    
    @SerializedName("HPNO")
    public String hpno;
    
    @SerializedName("EMAIL")
    public String email;
}