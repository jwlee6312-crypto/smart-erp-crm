package com.crmbank.erp.hsaa.dto;

import lombok.Data;

@Data
public class CallMstDto {
    private String svcno;
    private String cmpycd;
    private String custcd;
    private String custnm;
    private String svcymd;
    private String trbment;
    private String ansment;
    private String aisummary;
    private String starttime;
    private String endtime;
    private String consultid;
    private String consultnm;
    private String recfile;
    private String escalationno;
    private String escmemo;
    private String calltelno;
    private String callusernm;
    private String callemail;
    private String feedbackuser;
    private String feedbackusernm;
    
    // Additional fields for processTransfer
    private String userid;
    private String custsnm;
    private String telno;
    private String hpno;
    private String innumber;
    private String emailid;
    private String remark;
    private String transyn;
    private String transno;
    private String transmemo;
    
    // For processing in UI
    private boolean useyn;
    private String abandonyn;
}
