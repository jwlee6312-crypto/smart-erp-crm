package com.crmbank.erp.mobile;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class CustDto {
    @SerializedName("cmpycd")
    public String cmpycd; // 회사코드

    @SerializedName("nacd")
    public String nacd; // 국가코드

    @SerializedName("schcustnm")
    public String schcustnm; // 검색 거래처명

    @SerializedName("schcustgbn")
    public String schcustgbn; // 검색 거래처구분

    @SerializedName("schstatus")
    public String schstatus; // 검색 상태

    @SerializedName("custcd")
    public String custcd; // 거래처

    @SerializedName("custno")
    public String custno; // 사업자번호

    @SerializedName("jongcd")
    public String jongcd; // 종사업장

    @SerializedName("custnm")
    public String custnm; // 거래처명

    @SerializedName("custsnm")
    public String custsnm; // 거래처단축명

    @SerializedName("custgbn")
    public String custgbn; // 거래처구분

    @SerializedName("bossnm")
    public String bossnm; // 대표자

    @SerializedName("juminno")
    public String juminno; // 주민번호

    @SerializedName("legalno")
    public String legalno; // 법인번호

    @SerializedName("custkind")
    public String custkind; // 업종

    @SerializedName("custtype")
    public String custtype; // 업태

    @SerializedName("telno")
    public String telno; // 전화

    @SerializedName("faxno")
    public String faxno; // 팩스

    @SerializedName("area")
    public String area; // 지역

    @SerializedName("addrcd")
    public String addrcd; // 주소코드

    @SerializedName("postno")
    public String postno; // 우편번호

    @SerializedName("address")
    public String address; // 주소

    @SerializedName("d_address")
    public String d_address; // 상세주소

    @SerializedName("stdymd")
    public String stdymd; // 시작일자

    @SerializedName("clsymd")
    public String clsymd; // 종료일자

    @SerializedName("status")
    public String status; // 거래처상태

    @SerializedName("outcustcd")
    public String outcustcd; // 더존거래처코드

    @SerializedName("email")
    public String email; // 메일주소

    @SerializedName("useyn")
    public String useyn; // 사용여부

    @SerializedName("addtime")
    public String addtime; // 생성일시

    @SerializedName("updtime")
    public String updtime; // 수정일시

    @SerializedName("updemp")
    public String updemp; // 수정자
}
