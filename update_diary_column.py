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
    cursor.execute("UPDATE hsaa300t_tbl SET diary_content = content WHERE CMPYCD = 'COIT' AND (diary_content IS NULL OR diary_content = '');")
    conn.commit()
    print("✅ Successfully updated diary_content from content in hsaa300t_tbl!")

conn.close()
