import os
import subprocess

target_tables = [
    "HSBA900T_TBL", "HSBA010T_TBL", "HABA930T_TBL", "HABA940T_TBL",
    "HSBA020T_TBL", "HSBA030T_TBL", "HSBA040T_TBL", "HSBA100T_TBL"
]

migration_dir = "/home/smart/smart-erp-crm/migration"
os.chdir(migration_dir)

for t in target_tables:
    py_file = f"{t}.py"
    if os.path.exists(py_file):
        with open(py_file, "r", encoding="utf-8") as f:
            code = f.read()

        code = code.replace("port=3307", "port=3308")
        code = code.replace("172.30.1.11:1435", "172.30.1.11:1434")
        code = code.replace("172.30.1.11:1434", "172.30.1.11:1434")

        if "charset='cp943c'" not in code:
            code = code.replace("database='SMARTDB'", "database='SMARTDB', charset='cp943c'")

        with open(py_file, "w", encoding="utf-8") as f:
            f.write(code)

        print(f"🚀 Running {py_file}...")
        subprocess.run(["python3", py_file])

print("✅ Specific tables fixed instantly!")
