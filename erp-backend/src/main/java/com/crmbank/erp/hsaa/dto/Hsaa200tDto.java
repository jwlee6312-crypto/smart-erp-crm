package com.crmbank.erp.hsaa.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Hsaa200tDto {
    private String cmpycd;
    private String salesid;
    private String salestitle;
    private String state;
    private String statenm;
    private String addtime;
    private String custcd;
    private String custnm;
    private String bossnm;
    private String telno;
    private String faxno;
    private String trancd;
    private String postno;
    private String address;
    private String d_address;
    private String userid;
    private String usernm;
    private String deptcd;
    private String deptnm;
    private BigDecimal foreamt;
    private BigDecimal realamt;
    private String foredt;
    private Integer succrate;
    private String rtncd;
    private String usecd;
    private String foredelivdt;
    private String conditions;
    private String lastmtdt;
    private String realdt; // 수주일자
    private String faildt; // 실패일자
    private String holdondt; // 보류기한
    private String wincd; // 성공사유
    private String failcd; // 실패사유
    private String holdcd; // 보류사유
    private String salesremark;
    private String salescoaching; // 상담코칭
    private String choice;
    
    private String reportcd; // 보고분류 (HABA900T 883)
    private String reportdt; // 보고일자
    private String coachingcd; // 팀장분류 (HABA900T 882)
    
    private String importrank; // IMPORT column
    private String svcno;
    private String updtime;
    private String updemp;
}
