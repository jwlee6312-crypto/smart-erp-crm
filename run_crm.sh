#!/bin/bash
# 🚀 Smart ERP CRM - Start Script for Server (172.30.1.11 & DuckDNS 18889)
PROJECT_ROOT="/home/smart/smart-erp-crm"

echo "=================================================="
echo "   Smart ERP CRM System Start (172.30.1.11 @ 3308)"
echo "=================================================="

# 1. 경로 이동
if [ -d "$PROJECT_ROOT" ]; then
    cd "$PROJECT_ROOT"
else
    echo "Warning: $PROJECT_ROOT not found, using current directory."
    PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
    cd "$PROJECT_ROOT"
fi

# 2. 백엔드 로그 초기화 (Clear log)
echo "[0/4] Clearing previous log files..."
> backend.log 2>/dev/null
> backend_run.log 2>/dev/null

# 3. 백엔드 JAR 빌드
echo "[1/4] Building backend BootJar..."
chmod +x gradlew
./gradlew :erp-backend:clean :erp-backend:bootJar --no-daemon

# 4. 필수 권한 보정 및 데이터 디렉토리 생성
echo "[2/4] Fixing directory permissions & creating data dirs..."
sudo mkdir -p /var/lib/asterisk/sounds/custom /data/smart-erp-crm/mysql /data/smart-erp-crm/storage /data/smart-erp-crm/recordings /data/smart-erp-crm/chatwoot/postgres
sudo chmod -R 777 /var/lib/asterisk/sounds/custom
sudo chmod -R 777 /data/smart-erp-crm

# 5. 기존 도커 컨테이너 정리 (데이터 볼륨 /data/smart-erp-crm/mysql은 보존)
echo "[3/4] Cleaning up existing Docker resources..."
sudo docker compose down

# 6. 스마트 ERP CRM 서비스 기동 (3308 포트 MySQL 포함)
echo "[4/4] Launching Smart ERP CRM services via Docker Compose (Port 3308)..."
sudo docker compose up --build --force-recreate -d

# 7. 백엔드 로그 실시간 수집
sudo docker compose logs -f backend > backend_run.log 2>&1 &

echo "=================================================="
echo " ✅ SMART-ERP-CRM STARTED SUCCESSFULLY (DB @ 3308)!"
echo " --------------------------------------------------"
echo " - External URL : http://smart-erp.duckdns.org:18889"
echo " - Internal Web : http://172.30.1.11:18889"
echo " - Backend API  : http://172.30.1.11:8090"
echo " - CRM DB Port  : 3308 (Persistent Volume Preserved)"
echo "=================================================="
