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
    cursor.execute("DESCRIBE hsba130t_tbl;")
    print("--- DESCRIBE hsba130t_tbl ---")
    for row in cursor.fetchall():
        print(row)
    cursor.execute("SELECT * FROM hsba130t_tbl LIMIT 5;")
    print("--- ROWS ---")
    for row in cursor.fetchall():
        print(row)

conn.close()
