import pymssql

conn = pymssql.connect(
    server='172.30.1.11',
    port=1434,
    user='sa',
    password='Crmbank123!',
    database='SMARTDB',
    charset='cp949'
)
cur = conn.cursor()
cur.execute('SELECT USERID, INNER_NO FROM haba920t_tbl')
for r in cur.fetchall():
    print(r)
conn.close()
