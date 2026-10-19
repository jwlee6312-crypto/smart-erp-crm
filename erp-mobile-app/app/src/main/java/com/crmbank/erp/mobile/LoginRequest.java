package com.crmbank.erp.mobile;

import com.google.gson.annotations.SerializedName;

/**
 * 🚀 로그인 요청 DTO (백엔드 passwd 규격과 일치)
 */
public class LoginRequest {
    @SerializedName("cmpycd")
    private String cmpycd;

    @SerializedName("nacd")
    private String nacd;

    @SerializedName("userid")
    private String userid;

    @SerializedName("passwd") // 💡 pw에서 passwd로 변경하여 400 에러 해결
    private String passwd;

    public LoginRequest(String cmpycd, String nacd, String userid, String passwd) {
        this.cmpycd = cmpycd;
        this.nacd = nacd;
        this.userid = userid;
        this.passwd = passwd;
    }
}
