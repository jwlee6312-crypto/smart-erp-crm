package com.crmbank.erp.mobile;

import com.google.gson.annotations.SerializedName;

public class Depo_DtlDto {
    @SerializedName("cmpycd") public String cmpycd;
    @SerializedName("imno") public String imno;
    @SerializedName("imrowno") public String imrowno;
    @SerializedName("deptcd") public String deptcd;
    @SerializedName("deptnm") public String deptnm;
    @SerializedName("custcd") public String custcd;
    @SerializedName("custnm") public String custnm;
    @SerializedName("imtype") public String imtype;
    @SerializedName("imymd") public String imymd;
    @SerializedName("imamt") public Double imamt;
    @SerializedName("mgtno") public String mgtno;
    @SerializedName("billgbn") public String billgbn;
    @SerializedName("pubymd") public String pubymd;
    @SerializedName("endymd") public String endymd;
    @SerializedName("billamt") public Double billamt;
    @SerializedName("pubbank") public String pubbank;
    @SerializedName("pubman") public String pubman;
    @SerializedName("slipyn") public String slipyn;
    @SerializedName("cnfmyn") public String cnfmyn;
    @SerializedName("state") public String state;
    @SerializedName("updemp") public String updemp;
}
