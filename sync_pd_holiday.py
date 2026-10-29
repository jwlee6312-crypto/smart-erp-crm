import pymysql
import pymssql

def get_mysql_conn():
    return pymysql.connect(
        host='172.30.1.11',
        port=3308,
        user='root',
        password='Crmbank123!',
        database='smartcrm',
        charset='utf8mb4'
    )

def get_mssql_conn():
    return pymssql.connect(
        server='172.30.1.11',
        port=1434,
        user='sa',
        password='Crmbank123!',
        database='SMARTDB',
        charset='cp949'
    )

# 1. Ensure table exists in MySQL
mysql_conn = get_mysql_conn()
with mysql_conn.cursor() as cursor:
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS `pd_holiday` (
      `cmpycd` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
      `holi_dt` varchar(8) COLLATE utf8mb4_unicode_ci NOT NULL,
      `holi_nm` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
      `gubun` varchar(1) COLLATE utf8mb4_unicode_ci DEFAULT '1',
      PRIMARY KEY (`cmpycd`, `holi_dt`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
    """)
    mysql_conn.commit()
mysql_conn.close()

# 2. Fetch from MSSQL and Insert into MySQL
mssql_conn = get_mssql_conn()
with mssql_conn.cursor() as m_cursor:
    m_cursor.execute("SELECT cmpycd, holi_dt, holi_nm, gubun FROM PD_HOLIDAY")
    rows = m_cursor.fetchall()
mssql_conn.close()

mysql_conn = get_mysql_conn()
with mysql_conn.cursor() as c_mysql:
    c_mysql.execute("DELETE FROM pd_holiday;")
    for row in rows:
        cmpycd, holi_dt, holi_nm, gubun = row
        sql = "INSERT INTO pd_holiday (cmpycd, holi_dt, holi_nm, gubun) VALUES (%s, %s, %s, %s)"
        c_mysql.execute(sql, (cmpycd, holi_dt, holi_nm, gubun))
    mysql_conn.commit()
mysql_conn.close()

print(f"Successfully synced {len(rows)} rows into pd_holiday from MSSQL!")
