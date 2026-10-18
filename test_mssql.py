import pymssql

try:
    conn = pymssql.connect(server='172.30.1.11:1434', user='sa', password='Crmbank123!', database='SMARTDB', charset='cp943c')
    cur = conn.cursor(as_dict=True)
    cur.execute('SELECT TOP 5 CODENM FROM HABA900T_TBL')
    print("SUCCESS:", cur.fetchall())
except Exception as e:
    print("ERROR:", e)
