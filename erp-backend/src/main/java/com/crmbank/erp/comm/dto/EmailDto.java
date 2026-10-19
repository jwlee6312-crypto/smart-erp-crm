/*
 * ===========================================
 * 프로그램명		: 명세서 메일전송, Invoice 발행
 * 프로그램 ID		: HECG050U, HECG040U
 * 작성일자			: 25.10.17
 * 작성자				: 이현준
 * 수정일자			: 25.11.12
 * 수정자				: 이현준
 * 설명					: Email 전송 관련 DTO
 * 수정 내용			: 이메일 전송 관련 필드 추가
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
public class EmailDto {
	private String docgb; // 문서구분
	private String year; // 청구년도
	private String month; // 청구월
	private String custcd; // 거래처 코드
	private String custnm; // 거래처 이름
	private String email; // 보낼 메일 주소
	private String name; //  거래처 담당자
	    private String no;
    private String htmlcontent;
    private String fromnm; // 🚀 [추가] 보내는 사람 회사명
 // 과금번호 등
	private String url; // 첨부파일 URL
	private String subject; // 메일 제목
	private String scheduletype; // 예약구분
	private String invoiceform;
	private String invoicesign;
	private String balno;
	private String reqymd;
	private String balymd;
	private String remark;
}
