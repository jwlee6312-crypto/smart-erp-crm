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
    sql = """
        UPDATE hsaa200t_tbl a
        JOIN (
            SELECT CMPYCD, SALESID, MAX(CONTDT) as max_contdt
            FROM hsaa300t_tbl
            WHERE CMPYCD = 'COIT'
            GROUP BY CMPYCD, SALESID
        ) b ON a.CMPYCD = b.CMPYCD AND a.SALESID = b.SALESID
        SET a.LASTMTDT = b.max_contdt;
    """
    affected = cursor.execute(sql)
    conn.commit()
    print(f"✅ Successfully updated LASTMTDT for {affected} sales records in hsaa200t_tbl with latest CONTDT from hsaa300t_tbl!")

conn.close()
