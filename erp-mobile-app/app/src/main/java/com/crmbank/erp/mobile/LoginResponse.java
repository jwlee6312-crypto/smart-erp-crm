package com.crmbank.erp.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class LoginResponse {
    @SerializedName("cmpycd")
    public String cmpycd;

    @SerializedName("nacd")
    public String nacd;

    @SerializedName("usergrp")
    public String usergrp;

    @SerializedName("userid")
    public String userId;

    @SerializedName("usernm")
    public String userName;

    @SerializedName("deptcd")
    public String deptcd;

    @SerializedName("deptnm")
    public String deptnm;

    @SerializedName("inner_no")
    public String innerNo;

    @SerializedName("hpno")
    public String hpno;

    @SerializedName("email")
    public String email;

    @SerializedName("headerMenus")
    public List<Map<String, Object>> headerMenus;

    @SerializedName("sidebarItems")
    public Object sidebarItems;
}