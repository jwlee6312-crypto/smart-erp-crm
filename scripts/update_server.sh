#!/bin/bash

# 💡 사용법: ./update_server.sh [새로운_IP]
# 예: ./update_server.sh 172.30.1.99

NEW_IP=$1

if [ -z "$NEW_IP" ]; then
    echo "❌ 에러: 새로운 IP 주소를 입력해주세요."
    echo "사용법: ./update_server.sh 172.30.1.99"
    exit 1
fi

echo "🚀 서버 IP 업데이트를 시작합니다: $NEW_IP"

# 1. 설정 파일 경로 (사용자 환경에 맞게 조정됨)
ENV_FILE="/home/smart/restore/erp.crmbank.co.kr/erp-backend/src/main/resources/env.properties"

# 2. 기존 IP 패턴을 찾아 새로운 IP로 일괄 변경 (172.30.1.xx 형태 자동 감지)
if [ -f "$ENV_FILE" ]; then
    echo "📝 env.properties 수정 중..."
    sed -i "s/172\.30\.1\.[0-9]\{1,3\}/$NEW_IP/g" "$ENV_FILE"
    echo "✅ IP 수정 완료."
else
    echo "❌ 에러: $ENV_FILE 파일을 찾을 수 없습니다."
    exit 1
fi

# 3. 백엔드 재빌드
echo "🔨 백엔드 재빌드 시작 (Gradle)..."
cd /home/smart/restore/erp.crmbank.co.kr
./gradlew :erp-backend:clean :erp-backend:bootJar -x test

if [ $? -eq 0 ]; then
    echo "✅ 빌드 성공!"
else
    echo "❌ 빌드 실패. 로그를 확인하세요."
    exit 1
fi

# 4. 기존 프로세스 종료 및 재시작
echo "🔄 백엔드 서버 재시작 중..."
fuser -k 8080/tcp
nohup java -jar erp-backend/build/libs/*.jar > backend.log 2>&1 &

echo "✨ 모든 작업이 완료되었습니다!"
echo "📡 실시간 로그 확인: tail -f backend.log"
