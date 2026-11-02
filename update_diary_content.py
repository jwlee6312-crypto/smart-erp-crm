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
    cursor.execute("UPDATE hsaa300t_tbl SET diarycontent = content WHERE CMPYCD = 'COIT' AND (diarycontent IS NULL OR diarycontent = '');")
    conn.commit()
    print(f"✅ Successfully updated diarycontent from content in hsaa300t_tbl!")

conn.close()
