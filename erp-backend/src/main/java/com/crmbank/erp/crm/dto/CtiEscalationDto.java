package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 업무 이관 및 피드백 관리 (CTI_ESCALATION_TBL) DTO
 * 소문자 표준화 적용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CtiEscalationDto {
    private Integer escalation_no;   // 이관 번호 (PK, AI)
    private String svcno;            // 상담 접수번호
    private String linkedid;         // Asterisk LinkedID
    private LocalDateTime reg_date;  // 이관 요청일
    private String sender_id;        // 이관 요청자 ID
    private String deptcd;           // 이관 대상 부서코드
    private String summary;          // 상담 요약
    private String esc_memo;         // 이관 요청 메모
    private String status;           // 상태 (100: REQUEST, 200: PROGRESS, 300: DONE)
    private String feedback_memo;    // 피드백 내용
    private LocalDateTime feedback_date; // 피드백 완료일
    private String feedback_user;    // 피드백 작성자
    private LocalDateTime addtime;   // 등록일시
    private LocalDateTime updtime;   // 수정일시
    private String updemp;           // 수정자
}
