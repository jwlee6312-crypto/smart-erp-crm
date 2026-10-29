USE smartcrm;

-- 1. update surv_mst with proper Korean questions
UPDATE surv_mst SET question = 'Q1. 당사 서비스 이용 만족도를 평가해주세요.' WHERE surv_no = '00001';
UPDATE surv_mst SET question = 'Q2. 향후 추가 도입 의향이 있으신가요?' WHERE surv_no = '00002';
UPDATE surv_mst SET question = 'Q3. 상담원의 응대 태도는 만족스러우셨습니까?' WHERE surv_no = '00003';
UPDATE surv_mst SET question = 'Q4. 신상품 안내를 받으셨습니까?' WHERE surv_no = '00004';
UPDATE surv_mst SET question = 'Q5. 가입 의사가 있으십니까?' WHERE surv_no = '00005';
UPDATE surv_mst SET question = 'Q6. 기타 건의사항이 있으신가요?' WHERE surv_no = '00006';
UPDATE surv_mst SET question = 'Q7. 재가입 의향이 있으신가요?' WHERE surv_no = '00007';
UPDATE surv_mst SET question = 'Q8. 고객 지원 서비스에 만족하십니까?' WHERE surv_no = '00008';
UPDATE surv_mst SET question = 'Q9. 담당 매니저 응대 만족도' WHERE surv_no = '00009';
UPDATE surv_mst SET question = 'Q10. 종합 만족도 평가' WHERE surv_no = '00010';

-- 2. update surv_dtl with proper Korean answers
UPDATE surv_dtl SET ans_cont = '매우 만족' WHERE surv_no = '00001' AND ans_no = '01';
UPDATE surv_dtl SET ans_cont = '만족' WHERE surv_no = '00001' AND ans_no = '02';
UPDATE surv_dtl SET ans_cont = '보통' WHERE surv_no = '00001' AND ans_no = '03';
UPDATE surv_dtl SET ans_cont = '불만족' WHERE surv_no = '00001' AND ans_no = '04';
UPDATE surv_dtl SET ans_cont = '매우 불만족' WHERE surv_no = '00001' AND ans_no = '05';

UPDATE surv_dtl SET ans_cont = '있다' WHERE surv_no = '00002' AND ans_no = '01';
UPDATE surv_dtl SET ans_cont = '없다' WHERE surv_no = '00002' AND ans_no = '02';
UPDATE surv_dtl SET ans_cont = '보류' WHERE surv_no = '00002' AND ans_no = '03';

UPDATE surv_dtl SET ans_cont = '매우 친절함' WHERE surv_no = '00003' AND ans_no = '01';
UPDATE surv_dtl SET ans_cont = '친절함' WHERE surv_no = '00003' AND ans_no = '02';
UPDATE surv_dtl SET ans_cont = '보통' WHERE surv_no = '00003' AND ans_no = '03';

UPDATE surv_dtl SET ans_cont = '예' WHERE surv_no = '00004' AND ans_no = '01';
UPDATE surv_dtl SET ans_cont = '아니오' WHERE surv_no = '00004' AND ans_no = '02';

UPDATE surv_dtl SET ans_cont = '예' WHERE surv_no = '00005' AND ans_no = '01';
UPDATE surv_dtl SET ans_cont = '아니오' WHERE surv_no = '00005' AND ans_no = '02';

-- 3. update campaign_mst with proper Korean campaign names
UPDATE campaign_mst SET camp_nm = '2026 하반기 VIP 고객 만족도 조사' WHERE camp_no = '00001';
UPDATE campaign_mst SET camp_nm = '신상품 아웃바운드 프로모션' WHERE camp_no = '00002';
UPDATE campaign_mst SET camp_nm = '휴면 고객 해지 방어 캠페인' WHERE camp_no = '00003';
