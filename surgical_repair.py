import pymssql
import pymysql

try:
    mssql_conn = pymssql.connect(server='172.30.1.11', port=1434, user='sa', password='Crmbank123!', database='SMARTDB', charset='cp949')
    mssql_cursor = mssql_conn.cursor(as_dict=True)

    mysql_conn = pymysql.connect(host='172.30.1.11', port=3308, user='root', password='Crmbank123!', database='smartcrm', charset='utf8mb4')
    mysql_cursor = mysql_conn.cursor(pymysql.cursors.DictCursor)

    print("🔍 [SURGICAL REPAIR] Starting non-destructive Korean text repair across all tables...")

    mssql_cursor.execute("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE'")
    tables = [row['TABLE_NAME'] for row in mssql_cursor.fetchall()]

    for table in tables:
        try:
            mysql_cursor.execute(f"""
                SELECT COLUMN_NAME
                FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
                WHERE TABLE_SCHEMA = 'smartcrm'
                AND TABLE_NAME = '{table}'
                AND CONSTRAINT_NAME = 'PRIMARY'
            """)
            pk_cols = [row['COLUMN_NAME'] for row in mysql_cursor.fetchall()]

            if not pk_cols:
                continue

            mssql_cursor.execute(f"SELECT * FROM [{table}]")
            ms_rows = mssql_cursor.fetchall()
            if not ms_rows:
                continue

            sample = ms_rows[0]
            string_cols = [k for k, v in sample.items() if isinstance(v, str)]

            if not string_cols:
                continue

            updated_count = 0
            for r in ms_rows:
                where_clauses = []
                where_vals = []
                is_valid = True
                for pk in pk_cols:
                    if pk in r and r[pk] is not None:
                        where_clauses.append(f"`{pk}` = %s")
                        where_vals.append(r[pk])
                    else:
                        is_valid = False
                        break

                if not is_valid:
                    continue

                where_sql = " AND ".join(where_clauses)
                mysql_cursor.execute(f"SELECT * FROM `{table}` WHERE {where_sql}", tuple(where_vals))
                mysql_row = mysql_cursor.fetchone()

                if mysql_row:
                    set_clauses = []
                    set_vals = []
                    for col in string_cols:
                        if col in r and r[col] is not None:
                            set_clauses.append(f"`{col}` = %s")
                            set_vals.append(r[col])

                    if set_clauses:
                        set_sql = ", ".join(set_clauses)
                        update_sql = f"UPDATE `{table}` SET {set_sql} WHERE {where_sql}"
                        mysql_cursor.execute(update_sql, tuple(set_vals) + tuple(where_vals))
                        updated_count += 1

            mysql_conn.commit()
            print(f"✅ Table {table}: {updated_count} rows surgically repaired with pristine Korean text!")

        except Exception as e:
            mysql_conn.rollback()
            print(f"❌ Table {table} repair error: {e}")

    mssql_conn.close()
    mysql_conn.close()
    print("🎉 [SURGICAL REPAIR] All tables scanned and repaired successfully without deleting user data!")
except Exception as global_e:
    print(f"❌ Global error: {global_e}")
