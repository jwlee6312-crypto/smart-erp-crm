package com.crmbank.erp.comm.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 사용자 세션 DTO
 * 완전 소문자 표준화 적용 (JsonProperty 제거)
 * [INDEX_FORCE_COMMENT]
 */
@Data
public class UserSession implements Serializable {
    private static final long serialVersionUID = 1L;

    private String cmpycd;
    private String cmpynm;
    private String userid;
    private String usernm;
    private String inner_no;
    private String hpno;         // 🚀 추가: 휴대폰 번호
    private String email;
    private String deptcd;
    private String deptnm;
    private String empno;
    private String domain;
    private String salsyn;
    private String usergrp;
    private String status;       // 🚀 신규: 근무상태
    private String routing_mode; // 🚀 신규: 전화 연결모드
    private String datename;
    private String photo_path; // 🚀 사진 경로 추가

    private String leftlogo;
    private String rightimg;
    private String acctmenu;
    private String puchmenu;
    private String salsmenu;
    private String insamenu;
    private String mngtmenu;
    private String etc1menu;
    private String etc2menu;
    private String acctcode;
    private String puchcode;
    private String salscode;
    private String insacode;
    private String mngtcode;
    private String etc1code;
    private String etc2code;
    private String etc3code;
    private String etc4code;
    private String capyright;
}
