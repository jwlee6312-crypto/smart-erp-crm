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
    cursor.execute("SHOW TABLES FROM smartcrm;")
    tables = [row[0] for row in cursor.fetchall()]

    for table in tables:
        lower_name = table.lower()
        if table != lower_name:
            try:
                cursor.execute(f"RENAME TABLE `{table}` TO `{lower_name}`;")
                print(f"Renamed {table} -> {lower_name}")
            except Exception as e:
                print(f"Error renaming {table}: {e}")

conn.commit()
conn.close()
print("All tables renamed to lowercase successfully!")
