package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 인바운드상담녹취파일 (CALL_MONITOR_TBL) DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallMonitorDto {

    private String cmpycd;      // 회사코드 (PK)
    private String svcno;       // 접수번호 (PK)
    private String row;         // 녹취SERIAL (PK)
    private String moniternm;   // 녹취파일
    private LocalDateTime addtime; // 등록일시
    private LocalDateTime updtime; // 수정일시
    private String updemp;      // 수정자ID
}
