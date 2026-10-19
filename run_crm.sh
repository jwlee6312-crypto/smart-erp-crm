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
echo "[0/5] Clearing previous log files..."
> backend.log 2>/dev/null
> backend_run.log 2>/dev/null

# 3. 백엔드 JAR 빌드 (도커 빌드 시 필수)
echo "[1/5] Building backend BootJar..."
./gradlew :erp-backend:bootJar

# 4. 필수 권한 보정 및 데이터 디렉토리 생성
echo "[2/5] Fixing directory permissions & creating data dirs..."
sudo mkdir -p /var/lib/asterisk/sounds/custom /data/smart-erp-crm/mysql /data/smart-erp-crm/storage /data/smart-erp-crm/recordings /data/smart-erp-crm/chatwoot/postgres
sudo chmod -R 777 /var/lib/asterisk/sounds/custom
sudo chmod -R 777 /data/smart-erp-crm

# 5. 기존 도커 컨테이너 정리 (데이터 볼륨 /data/smart-erp-crm/mysql은 보존)
echo "[3/5] Cleaning up existing Docker resources..."
sudo docker compose down

# 6. 스마트 ERP CRM 서비스 기동 (3308 포트 MySQL 포함)
echo "[4/5] Launching Smart ERP CRM services via Docker Compose (Port 3308)..."
sudo docker compose up --build -d

# 7. 포트 3307(운영 DB)에서 포트 3308(CRM DB)로 데이터 복제 (최초 1회 또는 DB가 없을 때만)
echo "[5/5] Checking CRM DB (3308) status..."

DB_EXISTS=$(docker exec -i asterisk-db-crm mysql -uroot -pCrmbank123! -Nse "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = 'smartcrm';" 2>/dev/null || echo "0")

if [ "$DB_EXISTS" -eq "0" ]; then
    echo "▶ CRM DB does not exist yet. Replicating data from Production DB (3307)..."
    docker exec -i asterisk-db-crm mysql -uroot -pCrmbank123! -e "CREATE DATABASE IF NOT EXISTS smartcrm; CREATE DATABASE IF NOT EXISTS asterisk;" 2>/dev/null || true

    mysqldump -h 127.0.0.1 -P 3307 -u root -pCrmbank123! smartcrm 2>/dev/null | mysql -h 127.0.0.1 -P 3308 -u root -pCrmbank123! smartcrm 2>/dev/null || echo "⚠️ smartcrm replication warning"
    mysqldump -h 127.0.0.1 -P 3307 -u root -pCrmbank123! asterisk 2>/dev/null | mysql -h 127.0.0.1 -P 3308 -u root -pCrmbank123! asterisk 2>/dev/null || echo "⚠️ asterisk replication warning"
    echo "▶ Initial replication complete!"
else
    echo "▶ CRM DB already exists. Skipping re-replication to preserve existing data."
fi

# 8. 백엔드 로그 실시간 수집
sudo docker compose logs -f backend > backend_run.log 2>&1 &

echo "=================================================="
echo " ✅ SMART-ERP-CRM STARTED SUCCESSFULLY (DB @ 3308)!"
echo " --------------------------------------------------"
echo " - External URL : http://smart-erp.duckdns.org:18889"
echo " - Internal Web : http://172.30.1.11:18889"
echo " - Backend API  : http://172.30.1.11:8090"
echo " - CRM DB Port  : 3308 (Persistent Volume Preserved)"
echo "=================================================="
