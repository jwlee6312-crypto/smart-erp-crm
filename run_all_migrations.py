import os
import subprocess

migration_dir = "/home/smart/smart-erp-crm/migration"
os.chdir(migration_dir)

for f in sorted(os.listdir(".")):
    if f.endswith(".py") and f != "run_all_migrations.py":
        with open(f, "r", encoding="utf-8") as file:
            content = file.read()
        content = content.replace("port=3307", "port=3308")
        with open(f, "w", encoding="utf-8") as file:
            file.write(content)

        print(f"🚀 Running {f}...")
        subprocess.run(["python3", f])

print("✅ All migrations completed successfully!")
