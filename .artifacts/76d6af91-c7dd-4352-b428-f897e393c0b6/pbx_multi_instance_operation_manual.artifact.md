# 스마트 ERP CRM - PBX 멀티 인스턴스 운영 매뉴얼

본 매뉴얼은 단일 코드베이스 내에서 중앙 집중식 환경 변수 프로파일과 자동화된 스크립트를 활용하여, 독립된 여러 대의 Asterisk PBX(교환기) 인스턴스를 프로비저닝, 설정 및 관리하기 위한 표준 운영 절차(SOP)를 정의합니다.

---

## 1. 아키텍처 개요

여러 개의 `.conf` 파일에 설정값을 하드코딩하는 방식 대신, **템플릿 기반 프로비저닝 파이프라인(Template-Driven Provisioning Pipeline)**을 사용합니다:
1. **환경 변수 프로파일 (`*.env`)**: 모든 포트, RTP 대역, AMI 계정, SIP 트렁크 설정을 중앙 집중식으로 관리합니다.
2. **설정 템플릿 (`asterisk_config/templates/*.template`)**: `${변수명}` 플레이스홀더를 포함하는 재사용 가능한 뼈대 파일입니다.
3. **자동화 프로비저닝 스크립트 (`provision-pbx.sh`)**: 환경 변수 값을 템플릿에 자동으로 주입하여 최종 Asterisk 설정 파일(`sip.conf`, `rtp.conf`, `manager.conf`)을 동적으로 생성합니다.

---

## 2. 디렉토리 구조

```text
/smart-erp-crm
├── instance.env                  # 기본 인스턴스 환경 변수 파일
├── instance-02.env               # 선택사항: 추가 인스턴스 프로파일
├── provision-pbx.sh              # 자동 설정 파일 생성 프로비저닝 스크립트
├── docker-compose.yml            # 멀티 컨테이너 오케스트레이션
└── asterisk_config/
    ├── templates/                # 뼈대 설정 템플릿 폴더
    │   ├── sip.conf.template
    │   ├── rtp.conf.template
    │   └── manager.conf.template
    ├── sip.conf                  # 스크립트에 의해 자동 생성됨
    ├── rtp.conf                  # 스크립트에 의해 자동 생성됨
    └── manager.conf              # 스크립트에 의해 자동 생성됨
```

---

## 3. 단계별 운영 가이드

### 1단계: 환경 변수 정의 (`instance.env`)
프로젝트 루트에 환경 변수 파일을 생성하거나 수정합니다 (예: `instance.env`):

```env
# ==========================================
# PBX 인스턴스 설정 및 트렁크 프로필
# ==========================================
INSTANCE_NAME=crm-instance-01

# 네트워크 및 포트
SIP_EXTERNAL_PORT=5062
SIP_INTERNAL_PORT=5060
AMI_EXTERNAL_PORT=5039
AMI_INTERNAL_PORT=5038
RTP_PORT_START=10200
RTP_PORT_END=10300

# Asterisk 관리자(AMI) 인증 정보
AMI_USER=admin
AMI_SECRET=Crmbank123!

# SIP 트렁크 정보
TRUNK_NAME=twilio-trunk
TRUNK_HOST=crm-bank-erp.pstn.tokyo.twilio.com
TRUNK_USER=my_twilio_user
TRUNK_SECRET=my_twilio_secret
```

### 2단계: 프로비저닝 스크립트 실행
자동화 스크립트를 실행하여 템플릿으로부터 설정 파일을 생성합니다 (필요시 커스텀 프로파일 파일을 인자로 전달 가능):

```bash
# 기본 'instance.env' 사용 시
./provision-pbx.sh

# 커스텀 프로파일 사용 시 (예: 멀티 인스턴스 확장 시)
./provision-pbx.sh instance-02.env
```

### 3단계: Asterisk 컨테이너 재시작
새롭게 생성된 설정 파일을 PBX 교환기 컨테이너에 반영합니다:

```bash
sudo docker compose restart asterisk
```

---

## 4. 멀티 인스턴스 확장 워크플로우

별도의 프로젝트 폴더를 새로 만들지 않고 추가적인 독립 PBX 인스턴스를 배포하는 방법:
1. 기본 환경 변수 프로파일을 복사합니다:
   ```bash
   cp instance.env instance-02.env
   ```
2. `instance-02.env`를 열고 **겹치지 않는 포트 및 RTP 대역**을 할당합니다:
   ```env
   INSTANCE_NAME=crm-instance-02
   SIP_EXTERNAL_PORT=5064
   AMI_EXTERNAL_PORT=5040
   RTP_PORT_START=10400
   RTP_PORT_END=10500
   ```
3. 새 프로파일을 지정하여 프로비저닝 스크립트를 실행합니다:
   ```bash
   ./provision-pbx.sh instance-02.env
   ```
4. 두 번째 인스턴스를 위한 `docker-compose.yml` 포트 맵핑을 조정하고 컨테이너를 구동합니다.
