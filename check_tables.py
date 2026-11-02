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
    print("--- DESCRIBE hsaa100t_tbl ---")
    cursor.execute("DESCRIBE hsaa100t_tbl;")
    for row in cursor.fetchall():
        print(row)

    print("--- DESCRIBE hsaa300t_tbl ---")
    cursor.execute("DESCRIBE hsaa300t_tbl;")
    for row in cursor.fetchall():
        print(row)

conn.close()
