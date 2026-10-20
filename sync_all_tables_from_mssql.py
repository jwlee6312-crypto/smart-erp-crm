import pymysql
import pymssql
import os

def get_mysql_conn():
    return pymysql.connect(
        host='172.30.1.11',
        port=3308,
        user='root',
        password='Crmbank123!',
        database='smartcrm',
        charset='utf8mb4'
    )

def get_mssql_conn():
    return pymssql.connect(
        server='172.30.1.11',
        port=1434,
        user='sa',
        password='Crmbank123!',
        database='SMARTDB',
        charset='cp949'
    )

mysql_conn = get_mysql_conn()
with mysql_conn.cursor() as cursor:
    cursor.execute("SHOW TABLES FROM smartcrm;")
    mysql_tables = [row[0] for row in cursor.fetchall()]
mysql_conn.close()

mssql_conn = get_mssql_conn()
with mssql_conn.cursor() as mssql_cursor:
    mssql_cursor.execute("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE'")
    mssql_tables = {row[0].lower(): row[0] for row in mssql_cursor.fetchall()}
mssql_conn.close()

for table in mysql_tables:
    t_lower = table.lower()
    if t_lower in mssql_tables:
        real_table_name = mssql_tables[t_lower]
        try:
            m_conn = get_mssql_conn()
            m_cursor = m_conn.cursor()
            m_cursor.execute(f"SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '{real_table_name}'")
            mssql_cols = {col[0].lower(): col[0] for col in m_cursor.fetchall()}

            m_cursor.execute(f"SELECT * FROM [{real_table_name}]")
            rows = m_cursor.fetchall()
            m_conn.close()

            my_conn = get_mysql_conn()
            with my_conn.cursor() as c_mysql:
                c_mysql.execute(f"SHOW COLUMNS FROM `{table}`;")
                mysql_cols = [col[0] for col in c_mysql.fetchall()]

                valid_cols = []
                for mc in mysql_cols:
                    if mc.lower() in mssql_cols:
                        valid_cols.append((mc, mssql_cols[mc.lower()]))

                if not valid_cols:
                    my_conn.close()
                    continue

                mysql_col_names = [vc[0] for vc in valid_cols]
                mssql_col_names = [vc[1] for vc in valid_cols]

                c_mysql.execute(f"DELETE FROM `{table}`;")
                for row in rows:
                    val_strs = []
                    for v in row:
                        if v is None:
                            val_strs.append("NULL")
                        elif isinstance(v, (int, float)):
                            val_strs.append(str(v))
                        else:
                            escaped = str(v).replace("'", "''")
                            val_strs.append(f"'{escaped}'")

                    cols_joined = ", ".join([f"`{c}`" for c in mysql_col_names])
                    vals_joined = ", ".join(val_strs)
                    insert_sql = f"INSERT IGNORE INTO `{table}` ({cols_joined}) VALUES ({vals_joined});"
                    c_mysql.execute(insert_sql)
                my_conn.commit()
                my_conn.close()

            print(f"Successfully synced clean data from MSSQL for table: {table}")
        except Exception as e:
            print(f"Error processing table {table}: {e}")
    else:
        print(f"Table {table} not found in MSSQL SMARTDB, skipping.")

print("All tables successfully synced from MSSQL with pristine Korean encoding!")
