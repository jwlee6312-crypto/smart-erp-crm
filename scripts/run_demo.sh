#!/bin/bash
# 🚀 Smart ERP CRM - Demo Start Script for Server (172.30.1.11 & DuckDNS 18889)
PROJECT_ROOT="/home/smart/smart-erp-crm"

echo "=================================================="
echo "   Smart ERP CRM System Start (172.30.1.11)"
echo "=================================================="

# 1. 경로 이동
if [ -d "$PROJECT_ROOT" ]; then
    cd "$PROJECT_ROOT"
else
    echo "Warning: $PROJECT_ROOT not found, using parent directory."
    PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
    cd "$PROJECT_ROOT"
fi

# 2. 필수 권한 보정 및 데이터 디렉토리 생성
echo "[1/3] Fixing directory permissions & creating data dirs..."
sudo mkdir -p /var/lib/asterisk/sounds/custom /data/smart-erp-crm/mssql /data/smart-erp-crm/mysql /data/smart-erp-crm/storage /data/smart-erp-crm/recordings /data/smart-erp-crm/chatwoot/postgres
sudo chmod -R 777 /var/lib/asterisk/sounds/custom /data/smart-erp-crm/storage

# 3. 기존 도커 컨테이너 정리
echo "[2/3] Cleaning up existing Smart ERP CRM Docker resources..."
sudo docker compose down

# 4. 스마트 ERP CRM 서비스 기동
echo "[3/3] Launching Smart ERP CRM services via Docker Compose..."
sudo docker compose up -d

echo "=================================================="
echo " ✅ SMART-ERP-CRM STARTED SUCCESSFULLY!"
echo " --------------------------------------------------"
echo " - External URL : http://smart-erp.duckdns.org:18889"
echo " - Internal Web : http://172.30.1.11:18889"
echo " - Backend API  : http://172.30.1.11:8090"
echo " - Chatwoot CRM : http://172.30.1.11:3001"
echo "=================================================="
