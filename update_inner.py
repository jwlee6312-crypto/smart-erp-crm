import pymysql

conn = pymysql.connect(
    host='172.30.1.11',
    port=3308,
    user='root',
    password='Crmbank123!',
    database='smartcrm'
)
cur = conn.cursor()
cur.execute("UPDATE haba920t_tbl SET INNER_NO = '101' WHERE INNER_NO IS NULL OR TRIM(INNER_NO) = '';")
conn.commit()
conn.close()
print("Updated inner_no successfully!")
