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
        upper_name = table.upper()
        lower_name = table.lower()
        if table != upper_name:
            try:
                cursor.execute(f"CREATE OR REPLACE VIEW `{upper_name}` AS SELECT * FROM `{table}`;")
            except Exception as e:
                print(f"Error creating view {upper_name}: {e}")
        if table != lower_name:
            try:
                cursor.execute(f"CREATE OR REPLACE VIEW `{lower_name}` AS SELECT * FROM `{table}`;")
            except Exception as e:
                print(f"Error creating view {lower_name}: {e}")

conn.commit()
conn.close()
print("All case-insensitive views created successfully!")
