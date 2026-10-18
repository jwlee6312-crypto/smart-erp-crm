# Smart ERP CRM - Database Creation & Table Schema Script

> [!NOTE]
> Database creation and table DDL schemas for MySQL (`smartcrm`) extracted with explicit UTF-8 (`utf8mb4`) support.

## 1. Database Creation Script

```sql
-- Create databases with utf8mb4 character set and collation
CREATE DATABASE IF NOT EXISTS smartcrm CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS asterisk CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE smartsys; -- or smartcrm
```

## 2. Core Table Schemas

You can inspect or apply the full schema file at [schema.sql](file:///C:/smart-erp-crm/.artifacts/76d6af91-c7dd-4352-b428-f897e393c0b6/schema.sql).

### `HABA900T_TBL` (Common Code Master)
```sql
CREATE TABLE `HABA900T_TBL` (
  `CDTYPE` char(3) COLLATE utf8mb4_unicode_ci NOT NULL,
  `CODECD` char(3) COLLATE utf8mb4_unicode_ci NOT NULL,
  `CODENM` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `REMARK` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `UPDTIME` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `UPDEMP` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DSPORD` int DEFAULT NULL,
  `CRTTIME` datetime DEFAULT CURRENT_TIMESTAMP,
  `UPDYN` char(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `USEYN` char(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`CDTYPE`,`CODECD`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### `HABA920T_TBL` (User Master)
```sql
CREATE TABLE `HABA920T_TBL` (
  `USERNO` int NOT NULL AUTO_INCREMENT,
  `CMPYCD` char(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `USERID` char(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `USERNM` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DEPTCD` char(5) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DEPTNM` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `EMPNO` char(7) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `PW` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `SALSYN` char(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `TELNO` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `HPNO` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `EMAIL` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `SDEPT` char(5) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `USERGRP` varchar(3) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `PRICEGBN` char(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `POSITIONOFF` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `SIGNATURE` char(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DISTGB` char(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ALARM` char(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `FILENAME` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `BANK_INFO` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `SMART_TELNO` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `INNER_NO` char(5) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `USEYN` char(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `PHOTO_PATH` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `STATUS` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ROUTING_MODE` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ADDTIME` datetime DEFAULT CURRENT_TIMESTAMP,
  `UPDTIME` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `UPDEMP` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`USERNO`),
  KEY `CMPYCD` (`CMPYCD`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### `HABA990T_TBL` (Company Master)
```sql
CREATE TABLE `HABA990T_TBL` (
  `CMPYCD` char(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `CMPYNM` varchar(40) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `SAUPNO` char(12) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `LEGALNO` char(14) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `CMPYENM` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `BOSSNM` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `BOSSENM` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `JUMINNO` char(14) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `POSTNO` char(6) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ADDRESS` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `D_ADDRESS` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `UPJONG` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `UPTAE` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `TELNO` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `FAXNO` char(14) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `FONDYMD` char(8) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `CLOSEMM` char(2) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DOMAIN` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `EIPYN` char(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ELCYN` char(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `USEYN` char(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ADDTIME` datetime DEFAULT CURRENT_TIMESTAMP,
  `UPDTIME` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `UPDEMP` char(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`CMPYCD`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```
