package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 통합 통화 행위 및 실패 이력 (TOTAL_CALL_LOG) DTO - 대문자 원칙 적용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TotalCallLogDto {
    private String cmpycd;           // 회사코드
    private String uniqueid;         // Asterisk 고유 ID (Asterisk 유지)
    private String linkedid;         // 연결 ID (전화돌려주기 등) (Asterisk 유지)
    private String keyword;          // 검색 키워드
    private String call_type;        // 통화유형
    private String media_type;       // 매체유형
    private String direction;        // 방향
    private String src_no;           // 발신번호
    private String dst_no;           // 수신번호
    private String userid;           // 상담원ID
    private String exten;            // 내선번호
    private LocalDateTime start_time; // 시작시간
    private LocalDateTime answer_time;// 응답시간
    private LocalDateTime end_time;   // 종료시간
    private Integer wait_sec;        // 대기시간
    private Integer talk_sec;        // 통화시간
    private String result_cd;        // 결과코드
    private Integer hangup_cause;    // Asterisk 종료 코드
    private String fail_reason;      // 상세 실패 사유
    private String redial_yn;        // 재시도 여부
    private Integer redial_count;    // 재시도 횟수
    private String rec_file;         // 녹취파일명
    private String call_memo;        // 통화 메모

    // 💡 콜백 관련 필드 추가 (대문자 원칙)
    private String callback_yn;      // 콜백여부 (y/n)
    private String callback_no;      // 고객이 남긴 콜백 연락처
    private Integer callback_retry_cnt; // 콜백 시도 횟수
    private LocalDateTime callback_req_time; // 콜백 요청 시각
}
