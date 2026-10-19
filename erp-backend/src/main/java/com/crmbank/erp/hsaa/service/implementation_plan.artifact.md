# DTO 필드명 카멜케이스 준수 및 서비스 컴파일 오류 수정

HsaaService.java에서 발생하는 컴파일 오류는 DTO 클래스들의 필드명이 자바 명명 규칙(camelCase)을 따르지 않아 Lombok이 생성하는 getter/setter 이름이 서비스에서 호출하는 이름과 일치하지 않기 때문에 발생합니다. 또한 MyBatis Mapper XML에서도 일부 필드가 카멜케이스로 참조되고 있어 런타임 오류 가능성도 있습니다.

## Proposed Changes

### [Component] DTO Classes (com.crmbank.erp.hsaa.dto)

필드명을 자바 표준 카멜케이스로 변경하여 Lombok이 올바른 getter/setter를 생성하도록 합니다.

#### [MODIFY] [Hsaa300tDto.java](file:///D:/erp.crmbank.co.kr/erp-backend/src/main/java/com/crmbank/erp/hsaa/dto/Hsaa300tDto.java)
- `diarycontent` -> `diaryContent`
- `channelkind` -> `channelKind`

#### [MODIFY] [Hsaa200tDto.java](file:///D:/erp.crmbank.co.kr/erp-backend/src/main/java/com/crmbank/erp/hsaa/dto/Hsaa200tDto.java)
- `importrank` -> `importRank`
- `salesremark` -> `salesRemark`
- `svcno` -> `svcNo`
- `rtncd` -> `rtnCd` (필요시)
- `foredelivdt` -> `foreDelivDt` (필요시)

#### [MODIFY] [Hsaa210tDto.java](file:///D:/erp.crmbank.co.kr/erp-backend/src/main/java/com/crmbank/erp/hsaa/dto/Hsaa210tDto.java)
- `fromuserid` -> `fromUserid`
- `fromdeptcd` -> `fromDeptcd`
- `touserid` -> `toUserid`
- `todeptcd` -> `toDeptcd`
- `chngreason` -> `chngReason`

#### [MODIFY] [CallMstDto.java](file:///D:/erp.crmbank.co.kr/erp-backend/src/main/java/com/crmbank/erp/hsaa/dto/CallMstDto.java)
- `svcno` -> `svcNo`
- `trbment` -> `trbMent`
- `ansment` -> `ansMent`
- `transyn` -> `transYn`
- `transmemo` -> `transMemo`
- `hpno` -> `hpNo`
- `innumber` -> `inNumber`
- `emailid` -> `emailId`
- `abandonyn` -> `abandonYn`
- `custsnm` -> `custSnm` (Service에서 getCustSnm 호출 여부 확인)

#### [MODIFY] [Hsaa100tDto.java](file:///D:/erp.crmbank.co.kr/erp-backend/src/main/java/com/crmbank/erp/hsaa/dto/Hsaa100tDto.java)
- `hpno` -> `hpNo`
- `innumber` -> `inNumber`
- `custdept` -> `custDept`
- `custtel` -> `custTel`
- `haddress` -> `hAddress`

#### [MODIFY] [Hsaa600tDto.java](file:///D:/erp.crmbank.co.kr/erp-backend/src/main/java/com/crmbank/erp/hsaa/dto/Hsaa600tDto.java)
- `contactcnt` -> `contactCnt`
- `consultcnt` -> `consultCnt`
- `salesidcnt` -> `salesidCnt`
- `planamt` -> `planAmt`

### [Component] Service Layer

#### [MODIFY] [HsaaService.java](file:///D:/erp.crmbank.co.kr/erp-backend/src/main/java/com/crmbank/erp/hsaa/service/HsaaService.java)
- 오타 수정: `n= null` -> `null`, `mmethod` -> `method` (필요시 재확인)
- DTO 필드 변경에 따른 getter/setter 호출 확인 (대부분 이미 카멜케이스로 작성되어 있어 DTO 수정만으로 해결될 것으로 보임)

## Verification Plan

### Automated Tests
- `gradlew :erp-backend:compileJava` 명령어를 실행하여 컴파일 오류가 해결되었는지 확인합니다.

### Manual Verification
- MyBatis 로그를 확인하여 런타임에 DB 컬럼과 DTO 필드 매핑이 정상적으로 이루어지는지 확인합니다. (undercore to camelCase 자동 매핑 활용)
