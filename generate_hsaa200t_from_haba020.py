import pymysql
import random

conn = pymysql.connect(
    host='172.30.1.11',
    port=3308,
    user='root',
    password='Crmbank123!',
    database='smartcrm',
    charset='utf8mb4'
)

titles = [
    "스마트 ERP 구축 제안 및 시연", "클라우드 인프라 전환 상담", "CRM 시스템 고도화 컨설팅",
    "그룹웨어 연동 프로젝트 계약", "AI 에이전트 솔루션 도입 미팅", "보안 솔루션 업그레이드 협의",
    "모바일 SFA 앱 구축 건", "데이터베이스 이관 및 최적화", "BI 대시보드 도입 제안",
    "전자결재 시스템 연동 검토"
]

users = ["COIT", "admin", "USER1", "USER2", "SALES1"]
states = ["100", "200", "300", "900", "910"]

with conn.cursor() as cursor:
    # haba020t_tbl에서 실제 거래처 코드(CUSTCD)와 거래처명(CUSTNM) 가져오기
    cursor.execute("SELECT CUSTCD, CUSTNM FROM haba020t_tbl WHERE CMPYCD = 'COIT' LIMIT 50;")
    customers = cursor.fetchall()

    if not customers:
        print("❌ haba020t_tbl에 거래처 데이터가 없습니다.")
    else:
        print(f"📌 총 {len(customers)}개의 실제 거래처를 조회했습니다.")

        for i in range(1, 31):
            salesid = f"202610{i:05d}"
            cust = random.choice(customers)
            custcd = cust[0]
            custnm = cust[1] if cust[1] else "일반거래처"

            title = f"{custnm.strip()} - {random.choice(titles)}"
            userid = random.choice(users)
            state = random.choice(states)
            foreamt = random.randint(500, 5000) * 10000
            addtime = f"202610{(i % 28) + 1:02d}"
            foredt = f"202611{(i % 28) + 1:02d}"

            sql = """
                INSERT INTO hsaa200t_tbl
                (CMPYCD, SALESID, CUSTCD, SALESTITLE, USERID, STATE, FOREAMT, FOREDT, CUSTNM, ADDTIME, UPDTIME, UPDEMP)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW(), %s)
                ON DUPLICATE KEY UPDATE
                CUSTCD = VALUES(CUSTCD), CUSTNM = VALUES(CUSTNM), SALESTITLE = VALUES(SALESTITLE), FOREAMT = VALUES(FOREAMT);
            """
            cursor.execute(sql, ('COIT', salesid, custcd, title, userid, state, foreamt, foredt, custnm.strip(), addtime, userid))

        conn.commit()
        print("✅ haba020t_tbl의 실제 거래처 코드를 참조하여 hsaa200t_tbl 데모 데이터 30건 생성 완료!")

conn.close()
