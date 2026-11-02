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

cust_names = [
    "(주)대한컴퓨터", "서울소프트", "(주)미래아이엔씨", "한국테크놀로지", "비전솔루션즈",
    "한라시스템", "영남디지털", "(주)케이엘씨", "태평양네트웍스", "동방데이타",
    "세종이노베이션", "한성정보기술", "(주)금강시스템", "부산디지털", "대구소프트웨어"
]

users = ["COIT", "admin", "USER1", "USER2", "SALES1"]
states = ["100", "200", "300", "900", "910"]

with conn.cursor() as cursor:
    # 기존 데모 데이터 중복 방지 정리 또는 추가
    for i in range(1, 31):
        salesid = f"202610{i:05d}"
        custcd = f"C{(i % 15) + 1:06d}"
        custnm = random.choice(cust_names)
        title = f"{custnm} - {random.choice(titles)}"
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
            SALESTITLE = VALUES(SALESTITLE), FOREAMT = VALUES(FOREAMT), CUSTNM = VALUES(CUSTNM);
        """
        cursor.execute(sql, ('COIT', salesid, custcd, title, userid, state, foreamt, foredt, custnm, addtime, userid))

    conn.commit()
    print("✅ Successfully generated 30 demo records in hsaa200t_tbl!")

conn.close()
