package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 상담예약 (RESERVE_LIST) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReserveListDto {
    private String cmpycd;           // 회사코드
    private Integer resv_no;         // 예약번호
    private String resv_gb;          // 예약구분
    private Integer call_seq;        // 콜번호
    private String userid;           // 사용자ID
    private String resv_dt;          // 예약일자
    private String resv_h;           // 예약시
    private String resv_m;           // 예약분
    private String phone_type;       // 전화유형
    private String telno;            // 전화번호
    private String resv_memo;        // 예약메모
    private LocalDateTime alarm_dt;  // 알람일시
    private String flag;             // 플래그
    private LocalDateTime addtime;   // 등록일시
    private LocalDateTime updtime;   // 수정일시
    private String updemp;           // 수정자
}
