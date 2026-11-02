import pymysql

conn = pymysql.connect(
    host='172.30.1.11',
    port=3308,
    user='root',
    password='Crmbank123!',
    database='smartcrm',
    charset='utf8mb4'
)

with conn.cursor() as cursor:
    cursor.execute("SHOW TABLES LIKE '%cust%';")
    print("Cust tables:", cursor.fetchall())
    cursor.execute("SHOW TABLES LIKE '%hsba%';")
    print("Hsba tables:", cursor.fetchall())

conn.close()
