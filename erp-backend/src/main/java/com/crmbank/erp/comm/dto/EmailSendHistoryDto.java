/*
 * ===========================================
 * 프로그램명		: 명세서 메일전송, Invoice 발행, 메일전송기록조회
 * 프로그램 ID		: HECG050U, HECG040U, HECG060U
 * 작성일자			: 25.11.18
 * 작성자				: 이현준
 * 수정일자			: 25.11.18
 * 수정자				: 이현준
 * 설명					: Email History 관련 DTO
 * 수정 내용			: 필드 추가
 * ===========================================
 */

package com.crmbank.erp.comm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailSendHistoryDto {
	private int serial;
	private String cmpycd;
	private String docgb;
	private String docgbnm;
	private String sendymd;
	private String sendno;
	private String userid;
	private String usernm;
	private LocalDateTime senddate;
	private String custcd;
	private String custnm;
	private String email;
	private String sendemail;
	private String subject;
	private String attachurl;
	private String scheduletype;
	private String reserveddate;
	private String reservedh;
	private String conninfo;
	private String sendyn;
	private LocalDateTime addtime;
	private LocalDateTime updtime;
	private String updemp;
}