package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 인바운드상담 (CALL_MST_TBL) DTO - 대문자 원칙 적용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallMstDto {
    private String cmpycd;           // 회사코드 (PK)
    private String svcno;            // 접수번호 (PK)
    private String deptcd;           // 부서코드
    private String custcd;           // 거래처코드
    private String svcymd;           // 접수일자
    private String iono;             // 출고번호
    private String itemcd;           // 가입상품코드
    private String trb_ment;         // 문의내용
    private String ans_ment;         // 답변내용
    private String ai_summary;       // AI 요약 내용
    private LocalDateTime start_time; // 상담시작일시
    private LocalDateTime end_time;   // 상담종료일시
    private String consultid;        // 상담원ID
    private String rec_file;         // 녹취파일명
    private String escalation_yn;    // 이관여부
    private String escalation_no;    // 이관번호
    private String esc_memo;         // 이관내용
    private String happycall_yn;     // 해피콜여부
    private String interaction_id;   // 통합 상담 고유 ID
    private String linkedid;         // Asterisk 고유 ID
    private String call_telno;       // 상담시 수신번호
    private String call_usernm;      // 상담시 담당자명
    private String call_email;       // 상담시 이메일
    private LocalDateTime addtime;
    private LocalDateTime updtime;
    private String updemp;
}
