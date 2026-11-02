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
    cursor.execute("SELECT DISTINCT IMPORT FROM hsaa200t_tbl;")
    print("Distinct IMPORT values in hsaa200t_tbl:", cursor.fetchall())

    # 공통코드 테이블에서 중요도(IMPORT) 관련 코드 확인 (보통 750, 730 등)
    cursor.execute("SELECT CODE, CDNM FROM haba900t_tbl WHERE CDGBN LIKE '%IMPORT%' OR CDGBN IN ('730', '750', '200');")
    print("Code options:", cursor.fetchall())

conn.close()
