/*
 * ===========================================
 * 프로그램명		: 명세서 메일전송, Invoice 발행, 메일전송기록조회
 * 프로그램 ID		: HECG050U, HECG040U, HECG060U
 * 작성일자			: 25.11.03
 * 작성자				: 이현준
 * 수정일자			: 25.11.18
 * 수정자				: 이현준
 * 설명					: Email History Mapper
 * 수정 내용			: -
 * ===========================================
 */

package com.crmbank.erp.comm.mapper;

import com.crmbank.erp.comm.dto.EmailSendHistoryDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface EmailMapper {
	int saveMailHist (EmailSendHistoryDto emailSendHistoryDto);

	List<EmailSendHistoryDto> getMailHistory(Map<String, Object> params);
}
