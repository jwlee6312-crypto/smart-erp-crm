package com.crmbank.erp.mobile;

import com.google.gson.annotations.SerializedName;

public class SalesOrderDtlDto {
    @SerializedName("cmpycd")
    public String cmpycd; // ?뚯궗肄붾뱶

    @SerializedName("ordno")
    public String ordno; // 二쇰Ц踰덊샇

    @SerializedName("orowno")
    public String orowno; // 二쇰Ц?됰쾲

    @SerializedName("ordymd")
    public String ordymd; // 二쇰Ц?쇱옄

    @SerializedName("deptcd")
    public String deptcd; // 遺??

    @SerializedName("custcd")
    public String custcd; // 嫄곕옒泥?

    @SerializedName("itemcd")
    public String itemcd; // ?곹뭹肄붾뱶

    @SerializedName("itemnm")
    public String itemnm; // ?곹뭹紐?

    @SerializedName("itsize")
    public String itsize; // 洹쒓꺽

    @SerializedName("unit")
    public String unit; // ?⑥쐞

    @SerializedName("ordqty")
    public Double ordqty; // 二쇰Ц?섎웾

    @SerializedName("cunit")
    public String cunit; // ?섏궛?⑥쐞

    @SerializedName("cqty")
    public Double cqty; // ?섏궛?섎웾

    @SerializedName("ordamt")
    public Double ordamt; // 二쇰Ц怨듦툒媛

    @SerializedName("ordvat")
    public Double ordvat; // 二쇰Ц遺媛??

    @SerializedName("total")
    public Double total; // 二쇰Ц珥앹븸

    @SerializedName("price")
    public Double price; // ?④?

    @SerializedName("bal_qty")
    public Double bal_qty; // 諛쒖＜?섎웾

    @SerializedName("sts")
    public String sts; // 二쇰Ц?곹깭

    @SerializedName("quatno")
    public String quatno; // 寃ъ쟻踰덊샇

    @SerializedName("qrowno")
    public String qrowno; // 寃ъ쟻?됰쾲

    @SerializedName("outyn")
    public String outyn; // 異쒓퀬?щ?

    @SerializedName("state")
    public String state; // ?곹깭 (create, update, delete)

    @SerializedName("addtime")
    public String addtime;

    @SerializedName("updtime")
    public String updtime;

    @SerializedName("updemp")
    public String updemp;
}