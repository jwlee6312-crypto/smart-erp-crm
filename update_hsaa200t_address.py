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
            SELECT CMPYCD, CUSTCD, ADDRESS, D_ADDRESS, POSTNO
            FROM hsba130t_tbl
            WHERE (CMPYCD, CUSTCD, TRANCD) IN (
                SELECT CMPYCD, CUSTCD, MIN(TRANCD)
                FROM hsba130t_tbl
                GROUP BY CMPYCD, CUSTCD
            )
        ) b ON a.CMPYCD = b.CMPYCD AND a.CUSTCD = b.CUSTCD
        SET a.ADDRESS = b.ADDRESS, a.D_ADDRESS = b.D_ADDRESS, a.POSTNO = b.POSTNO;
    """
    affected = cursor.execute(sql)
    conn.commit()
    print(f"✅ Successfully updated {affected} sales records in hsaa200t_tbl with addresses from hsba130t_tbl!")

conn.close()
