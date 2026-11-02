import pymysql
import random

conn = pymysql.connect(
    host='172.30.1.11',
    port=3308,
    user='root',
    password='Crmbank123!',
    database='smartcrm',
    charset='utf8mb4'
)

grades = ['100', '200', '300'] # 상, 중, 하

with conn.cursor() as cursor:
    cursor.execute("SELECT SALESID FROM hsaa200t_tbl WHERE CMPYCD = 'COIT';")
    salesids = cursor.fetchall()

    for row in salesids:
        salesid = row[0]
        grade = random.choice(grades) # 기본 중급('200') 위주 또는 상/중/하 랜덤
        sql = "UPDATE hsaa200t_tbl SET IMPORT = %s WHERE CMPYCD = 'COIT' AND SALESID = %s;"
        cursor.execute(sql, (grade, salesid))

    conn.commit()
    print(f"✅ Successfully updated IMPORT grades for {len(salesids)} sales records in hsaa200t_tbl!")

conn.close()
