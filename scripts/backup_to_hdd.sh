#!/bin/bash
# 🚀 Smart ERP - HDD 백업 스크립트
# 대상: MSSQL(ERP), MySQL(Asterisk), 첨부파일, 녹취파일
# 저장소: /data/storage/backups

BACKUP_ROOT="/data/storage/backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="$BACKUP_ROOT/$DATE"

echo "=========================================="
echo "   Smart ERP System: HDD BACKUP START    "
echo "   Target: $BACKUP_DIR"
echo "=========================================="

# 1. 백업 디렉토리 생성
mkdir -p $BACKUP_DIR

# 2. MSSQL (SMARTDB) 백업
echo "[1/4] Backing up MSSQL (SMARTDB)..."
sudo docker exec smartdb /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P Crmbank123! -Q "BACKUP DATABASE [SMARTDB] TO DISK = N'/var/opt/mssql/data/SMARTDB_$DATE.bak' WITH NOFORMAT, NOINIT, NAME = 'SMARTDB-Full Database Backup', SKIP, NOREWIND, NOUNLOAD, STATS = 10"
sudo docker cp smartdb:/var/opt/mssql/data/SMARTDB_$DATE.bak $BACKUP_DIR/
sudo docker exec smartdb rm -f /var/opt/mssql/data/SMARTDB_$DATE.bak

# 3. MySQL (Asterisk) 백업
echo "[2/4] Backing up MySQL (Asterisk)..."
sudo docker exec asterisk-db mysqldump -u root -pCrmbank123! asterisk > $BACKUP_DIR/asterisk_$DATE.sql

# 4. 첨부파일 및 녹취파일 백업 (압축)
echo "[3/4] Compressing Storage & Recordings..."
sudo tar -czf $BACKUP_DIR/storage_files_$DATE.tar.gz -C /data/storage common recordings

# 5. 오래된 백업 삭제 (7일 경과분)
echo "[4/4] Cleaning up old backups (over 7 days)..."
find $BACKUP_ROOT -type d -mtime +7 -exec rm -rf {} +

echo "=========================================="
echo " ✅ BACKUP COMPLETED SUCCESSFULLY!       "
echo " Location: $BACKUP_DIR"
echo "=========================================="
