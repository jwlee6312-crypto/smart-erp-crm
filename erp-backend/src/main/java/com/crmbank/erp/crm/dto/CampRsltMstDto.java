package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 캠페인 전화상담결과 마스터 (CAMPAIGN_RSLT_MST) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampRsltMstDto {
    private Integer rslt_no;         // 결과번호
    private String cmpycd;           // 회사코드
    private Integer call_seq;        // 전화목록 고유번호
    private String camp_no;          // 캠페인번호
    private LocalDateTime start_dtime; // 통화시작
    private LocalDateTime end_dtime;   // 통화종료
    private String phone_type;       // 전화유형
    private String call_telno;       // 콜번호
    private String rslt_cd;          // 통화결과
    private String remark;           // 특이사항
    private String userid;           // 사용자
    private String line_num;         // 내선번호
    private String interaction_id;   // 통합 상담 고유 ID 연동
    private String media_type;       // 상담 매체 (CALL, CHAT, SMS)
    private String chat_log;         // 실시간 채팅/SMS 대화 전체 내역
    private String ai_summary;       // AI가 요약한 상담 핵심 내용
    private String rec_file;         // 💡 추가: 녹취파일명
    private LocalDateTime addtime;   // 등록일시
    private LocalDateTime updtime;   // 수정일시
    private String updemp;           // 수정자
}
