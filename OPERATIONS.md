# ⚙️ 운영 및 유지보수 가이드 (Operations Guide)

## 1. 데이터베이스 관리

### ERP DB (MSSQL)
- **도구**: SSMS
- **주소**: `localhost` (포트 1433)
- **비번**: `crmbank` (로컬 기본값)

### CTI DB (MySQL)
- **도구**: DBeaver / HeidiSQL
- **주소**: `localhost` (포트 3306)
- **비번**: `gkdldhs12#$`

## 2. 데이터 백업 (중요)
정기적으로 다음 파일들을 안전한 외장 하드나 클라우드에 복사해 두세요.
1. **DB 백업**: SQL Server의 `.bak` 파일과 MySQL의 `.sql` 덤프 파일.
2. **첨부파일**: `D:/erp.crmbank.co.kr/storage/` 폴더 전체.

## 3. 로그 확인
프로그램 에러 시 다음 위치를 확인하세요.
- **Backend**: 안드로이드 스튜디오의 `Run` 탭 로그.
- **Frontend**: 브라우저의 `F12 (개발자 도구)` -> `Console` 탭.
