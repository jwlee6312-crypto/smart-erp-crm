package com.crmbank.erp.crm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * AS결과 (AS_MST_TBL) DTO - 대문자 원칙 적용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsMstDto {

    private String cmpycd;           // 회사코드
    private String svcno;            // 접수번호
    private String accept_userid;    // 접수자
    private String acceptymd;        // 접수일
    private String custcd;           // 거래처
    private String iono;             // 출고번호
    private String itemcd;           // 품목코드
    private String fixed_userid;     // 수리기사
    private String fixedymd;         // 수리일자
    private String fixed_ment;       // 수리결과
    private String completeymd;      // 완료일
    private String expireymd;        // AS만료일
    private String chargeyn;         // 유상여부
    private Long fixamt;             // 수리비용
    private Long fixvat;             // 부가세
    private LocalDateTime addtime;   // 등록일시
    private LocalDateTime updtime;   // 수정일시
    private String updemp;           // 수정자
}
