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

keyman_names = ["김철수", "이영희", "박민수", "정지영", "오동현", "한지민", "서준호", "강서윤", "윤도현", "임지현"]
jikch_list = ["대표이사", "이사", "부장", "차장", "과장", "대리", "팀장"]
diary_contents = [
    "고객사와 시스템 도입 일정 협의 완료 및 계약서 초안 전달.",
    "요구사항 정의서 검토 및 추가 커스텀 개발 범위 논의.",
    "가격 및 견적서 승인 요청, 내부 결재 진행 중.",
    "사용자 교육 일정 조율 및 데모 시연 진행.",
    "유지보수 계약 조건 협의 및 최종 검토."
]

with conn.cursor() as cursor:
    # 1. hsaa200t_tbl에 있는 모든 고유 거래처(CMPYCD, CUSTCD) 조회
    cursor.execute("SELECT DISTINCT CMPYCD, CUSTCD, SALESID FROM hsaa200t_tbl WHERE CMPYCD = 'COIT';")
    sales_records = cursor.fetchall()

    print(f"📌 총 {len(sales_records)}개의 영업건 발견.")

    keyman_counter = 1
    for row in sales_records:
        cmpycd = row[0]
        custcd = row[1]
        salesid = row[2]

        # 해당 거래처에 키맨이 이미 있는지 확인
        cursor.execute("SELECT CUSTID FROM hsaa100t_tbl WHERE CMPYCD = %s AND CUSTCD = %s LIMIT 1;", (cmpycd, custcd))
        existing_keyman = cursor.fetchone()

        if existing_keyman:
            custid = existing_keyman[0]
        else:
            custid = f"K{keyman_counter:06d}"
            keyman_counter += 1
            name = random.choice(keyman_names)
            jikch = random.choice(jikch_list)
            hpno = f"010-{random.randint(1000,9999)}-{random.randint(1000,9999)}"
            mail = f"user{keyman_counter}@company.com"

            sql_keyman = """
                INSERT INTO hsaa100t_tbl
                (CMPYCD, CUSTID, SALESID, NAME, JIKCH, CUSTCD, KEYMAN, HPNO, MAIL, USEYN, ADDTIME, UPDTIME, UPDEMP)
                VALUES (%s, %s, %s, %s, %s, %s, '040', %s, %s, 'Y', NOW(), NOW(), 'admin')
                ON DUPLICATE KEY UPDATE NAME = VALUES(NAME);
            """
            cursor.execute(sql_keyman, (cmpycd, custid, salesid, name, jikch, custcd, hpno, mail))

    conn.commit()
    print("✅ 1. 각 거래처별 키맨(hsaa100t_tbl) 생성 완료!")

    # 2. hsaa300t_tbl 상담일지 30건 이상 임의 등록
    # 기존 상담일지 삭제 후 재생성 또는 추가
    cursor.execute("DELETE FROM hsaa300t_tbl WHERE CMPYCD = 'COIT';")

    diary_count = 0
    for i, row in enumerate(sales_records[:35]): # 30건 이상
        cmpycd = row[0]
        custcd = row[1]
        salesid = row[2]

        # 해당 영업건의 키맨 ID 가져오기
        cursor.execute("SELECT CUSTID FROM hsaa100t_tbl WHERE CMPYCD = %s AND CUSTCD = %s LIMIT 1;", (cmpycd, custcd))
        km = cursor.fetchone()
        custid = km[0] if km else "K000001"

        contdt = f"202610{(i % 28) + 1:02d}"
        ser = "001"
        title = f"고객 상담일지 기록 #{i+1}"
        content = random.choice(diary_contents)
        userid = "COIT"

        sql_diary = """
            INSERT INTO hsaa300t_tbl
            (CMPYCD, CONTDT, SER, SALESID, USERID, TITLE, CONTENT, CUSTCD, CUSTID, CHANNEL, CONTHH, CONTMM, STARTDATE, ADDTIME, UPDTIME, UPDEMP)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, '010', '10', '00', %s, NOW(), NOW(), 'admin')
            ON DUPLICATE KEY UPDATE TITLE = VALUES(TITLE), CONTENT = VALUES(CONTENT);
        """
        cursor.execute(sql_diary, (cmpycd, contdt, ser, salesid, userid, title, content, custcd, custid, contdt))
        diary_count += 1

    conn.commit()
    print(f"✅ 2. 연계된 상담일지(hsaa300t_tbl) 총 {diary_count}건 생성 완료!")

conn.close()
