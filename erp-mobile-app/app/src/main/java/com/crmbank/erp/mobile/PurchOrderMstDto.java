package com.crmbank.erp.mobile;

import com.google.gson.annotations.SerializedName;

public class PurchOrderMstDto {
    @SerializedName("cmpycd")
    public String cmpycd; // ?뚯궗肄붾뱶

    @SerializedName("balno")
    public String balno; // 諛쒖＜踰덊샇

    @SerializedName("balgb")
    public String balgb; // 諛쒖＜援щ텇

    @SerializedName("deptcd")
    public String deptcd; // 遺??

    @SerializedName("deptnm")
    public String deptnm; // 遺?쒕챸

    @SerializedName("custcd")
    public String custcd; // 嫄곕옒泥?

    @SerializedName("custnm")
    public String custnm; // 嫄곕옒泥섎챸

    @SerializedName("balymd")
    public String balymd; // 諛쒖＜?쇱옄

    @SerializedName("reqymd")
    public String reqymd; // ?⑺뭹?붿껌?쇱옄

    @SerializedName("email")
    public String email; // ?대찓??

    @SerializedName("bal_userid")
    public String bal_userid; // 諛쒖＜?대떦??

    @SerializedName("remark")
    public String remark; // ?뱀씠?ы빆

    @SerializedName("cfmyn")
    public String cfmyn; // ?뺤젙?좊Т

    @SerializedName("reqno")
    public String reqno; // 援щℓ?붿껌踰덊샇

    @SerializedName("sumamt")
    public Double sumamt; // ?⑷퀎

    @SerializedName("flag")
    public boolean flag;

    @SerializedName("addtime")
    public String addtime;

    @SerializedName("updtime")
    public String updtime;

    @SerializedName("updemp")
    public String updemp;
}