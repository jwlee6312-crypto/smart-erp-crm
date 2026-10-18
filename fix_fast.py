import pymssql
import pymysql

tables = [
    "hsba900t_tbl", "hsba010t_tbl", "haba930t_tbl", "haba940t_tbl",
    "hsba020t_tbl", "hsba030t_tbl", "hsba040t_tbl", "hsba100t_tbl"
]

try:
    mssql_conn = pymssql.connect(server='172.30.1.11', port=1434, user='sa', password='Crmbank123!', database='SMARTDB', charset='cp949')
    mssql_cursor = mssql_conn.cursor(as_dict=True)

    mysql_conn = pymysql.connect(host='172.30.1.11', port=3308, user='root', password='Crmbank123!', database='smartcrm', charset='utf8mb4')
    mysql_cursor = mysql_conn.cursor()

    for table in tables:
        try:
            print(f"🚀 {table} 빠른 이관 중...")
            mssql_cursor.execute(f"SELECT * FROM {table}")
            rows = mssql_cursor.fetchall()
            if not rows:
                print(f"⚠️ {table}: 이관할 데이터 없음")
                continue

            mysql_cursor.execute(f"DELETE FROM `{table}`")

            cols = list(rows[0].keys())
            col_list = ", ".join([f"`{c}`" for c in cols])
            placeholders = ", ".join(["%s"] * len(cols))
            insert_sql = f"INSERT INTO `{table}` ({col_list}) VALUES ({placeholders})"

            batch_data = []
            for r in rows:
                batch_data.append(tuple(r[c] for c in cols))

            mysql_cursor.executemany(insert_sql, batch_data)
            mysql_conn.commit()
            print(f"✅ {table} 완료! ({len(rows)}건)")
        except Exception as e:
            mysql_conn.rollback()
            print(f"❌ {table} 에러: {e}")

    mssql_conn.close()
    mysql_conn.close()
    print("🎉 지정된 테이블 한글 복구 완료!")
except Exception as global_e:
    print(f"❌ 전역 연결 에러: {global_e}")
