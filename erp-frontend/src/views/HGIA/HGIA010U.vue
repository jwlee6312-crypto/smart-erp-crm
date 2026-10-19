<!--
	=============================================================
	프로그램명	  : 통합고객지원 (수동 등록 및 CTI 통합)
    프로그램 ID	: HGIA010U
	작성일자	    : 2026.05.17
	Description	: CTI 자동 팝업 및 수동 상담 등록 기능 통합 (MSSQL 최적화)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hgia-workspace bg-light text-start d-flex flex-column overflow-hidden">
		<!-- 1. 상단 액션 바 -->
		<header class="omni-header d-flex align-items-center justify-content-between px-3 shadow-sm bg-white border-bottom py-2 flex-shrink-0">
			<div class="d-flex align-items-center gap-3">
				<h5 class="m-0 fw-bold text-dark d-flex align-items-center" style="font-size: 1.1rem;">
					<i class="bi bi-headset text-primary me-2 fs-5"></i>
					통합고객지원 관리
					<span v-if="ctiStore.incomingCall?.queue" class="badge bg-warning text-dark ms-3 fw-bold shadow-sm" style="font-size: 0.75rem;">
						{{ queueMap[ctiStore.incomingCall.queue] || '상담그룹' }}
					</span>
				</h5>
				<div v-if="ctiStore.isTalking" class="talking-status-badge d-flex align-items-center gap-2 px-3 py-1 rounded-pill bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25 shadow-sm">
					<span class="dot-blink bg-danger"></span>
					<span class="fw-bold small" style="font-size: 0.75rem;">상담 중 (녹취중)</span>
				</div>
			</div>
			<div class="d-flex gap-1">
				<button class="btn btn-sm btn-outline-secondary rounded-pill px-3 fw-bold" @click="handleNew">초기화</button>
				<button v-if="!ctiStore.isTalking && ctiStore.incomingCall" class="btn btn-sm btn-success rounded-pill px-3 fw-bold shadow-sm" @click="handleAnswer">전화받기</button>
				<button v-if="ctiStore.incomingCall" class="btn btn-sm btn-info text-white rounded-pill px-3 fw-bold shadow-sm" :disabled="isTransferLimitReached" @click="openTransferModal">돌려주기</button>
				<button class="btn btn-sm btn-danger rounded-pill px-3 fw-bold shadow-sm" @click="handleHangup">전화끊기</button>
				<button
					class="btn btn-sm btn-primary rounded-pill px-4 fw-bold shadow-sm ms-2"
					:disabled="ctiStore.isTalking || !customerInfo.custcd"
					@click="handleSave">
					<i class="bi bi-save me-1"></i>상담저장
				</button>
			</div>
		</header>

		<!-- 2. 메인 상담 본문 -->
		<div class="flex-grow-1 d-flex flex-column overflow-hidden p-2 gap-2" :class="ctiStore.isTalking ? 'bg-talking' : ''">

			<div class="row g-2 flex-shrink-0" style="height: 60%;">
				<!-- [좌측] 고객정보 및 상품 정보 -->
				<div class="col-md-4 d-flex flex-column gap-2 h-100">
					<div class="card shadow-sm border-0 border-top border-4 border-primary flex-shrink-0">
						<div class="card-header bg-white py-1 px-3 fw-bold extra-small text-primary d-flex justify-content-between align-items-center">
							<span><i class="bi bi-person-badge-fill me-2"></i>고객 정보 (수동 입력 가능)</span>
                            <i class="bi bi-info-circle text-muted" title="거래처 선택 후 연락처 정보를 수동으로 수정할 수 있습니다."></i>
						</div>
						<div class="card-body p-2 pt-1">
							<div class="row g-0">
								<div class="col-12 mb-2">
									<label class="mini-label fw-bold text-muted mb-0">거래처</label>
									<div class="input-group input-group-sm">
										<input type="text" class="form-control bg-white fw-bold text-primary border-0 border-bottom" :value="customerInfo.custnm + (customerInfo.custcd ? ' [' + customerInfo.custcd + ']' : '')" readonly placeholder="거래처 검색" />
										<button class="btn btn-sm btn-dark px-2" type="button" @click="handleOpenHelp('CUST')"><i class="bi bi-search"></i></button>
									</div>
								</div>
								<div class="col-6 pr-1">
									<label class="mini-label fw-bold text-muted mb-0">수신번호</label>
									<input type="text" class="form-control form-control-sm border-0 border-bottom bg-light bg-opacity-50 fw-bold text-primary py-0" v-model="customerInfo.hpno" placeholder="수동 입력 가능" />
								</div>
								<div class="col-6">
									<label class="mini-label fw-bold text-muted mb-0">담당자</label>
									<input type="text" class="form-control form-control-sm border-0 border-bottom bg-light bg-opacity-50 fw-bold py-0" v-model="customerInfo.usernm" placeholder="수동 입력 가능" />
								</div>
								<div class="col-12 mt-1">
									<label class="mini-label fw-bold text-muted mb-0">이메일 주소</label>
									<input type="text" class="form-control form-control-sm border-0 border-bottom bg-light bg-opacity-50 py-0" v-model="customerInfo.email" placeholder="수동 입력 가능" />
								</div>
							</div>
						</div>
					</div>
					<!-- 가입상품 리스트 -->
					<div class="card shadow-sm border-0 border-top border-4 border-info flex-grow-1 overflow-hidden">
						<div class="card-header bg-white py-2 px-3 fw-bold small text-info d-flex justify-content-between align-items-center">
							<span><i class="bi bi-box-fill me-2"></i>가입상품 / 매출내역</span>
							<span class="badge bg-info bg-opacity-10 text-info rounded-pill px-2" style="font-size: 0.7rem;">{{ itemList.length }}건</span>
						</div>
						<div class="card-body p-0 overflow-auto">
							<table class="table table-hover table-sm mb-0 extra-small align-middle">
								<thead class="table-light sticky-top">
									<tr><th class="ps-3 text-start">매출일자</th><th class="text-start">상품명</th><th class="pe-3">출고번호</th></tr>
								</thead>
								<tbody class="cursor-pointer">
									<tr v-for="item in itemList" :key="item.iono" @click="selectProduct(item)" :class="{'table-primary-subtle': consultData.iono === item.iono}">
										<td class="ps-3 text-start">{{ item.salsymd }}</td>
										<td class="text-start fw-bold text-truncate" style="max-width: 150px;">{{ item.itemnm }}</td>
										<td class="pe-3">{{ item.iono }}</td>
									</tr>
									<tr v-if="itemList.length === 0"><td colspan="3" class="py-5 text-muted italic text-center">내역이 없습니다.</td></tr>
								</tbody>
							</table>
						</div>
					</div>
				</div>

				<!-- [우측] 상담작성 -->
				<div class="col-md-8 d-flex flex-column gap-2 h-100">
					<div class="card shadow-sm border-0 border-top border-4 border-success h-100 overflow-hidden d-flex flex-column" :class="{'border-danger': ctiStore.isTalking}">
						<div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
							<div class="d-flex align-items-center gap-3">
								<span class="fw-bold small text-success"><i class="bi bi-pencil-square me-2"></i>상담 내용 작성</span>
								<!-- 🚀 [신규] AI 기록 모드 선택 (HGPA030U 스타일) -->
								<div class="btn-group btn-group-xs ms-2" role="group" style="scale: 0.9;">
									<button type="button" class="btn btn-outline-secondary fw-bold py-0" :class="consultData.ai_mode === 'manual' ? 'btn-secondary text-white' : ''" @click="consultData.ai_mode = 'manual'">수동 기록</button>
									<button type="button" class="btn btn-outline-primary fw-bold py-0" :class="consultData.ai_mode === 'auto' ? 'btn-primary text-white' : ''" @click="consultData.ai_mode = 'auto'"><i class="bi bi-robot me-1"></i>AI 자동 요약</button>
								</div>
							</div>
							<div class="form-check form-switch mb-0">
								<input class="form-check-input" type="checkbox" id="escCheck" v-model="consultData.escalation_yn" true-value="Y" false-value="N">
								<label class="form-check-label extra-small fw-bold text-danger" for="escCheck">타 부서 업무 이관</label>
							</div>
						</div>
						<div class="card-body p-3 pt-2 overflow-hidden d-flex flex-column gap-2">
							<div class="row g-2 flex-shrink-0">
								<div class="col-md-2">
									<label class="mini-label fw-bold mb-0">상담일자</label>
									<input type="date" class="form-control form-control-sm border-light shadow-none" v-model="consultData.date" />
								</div>
								<div class="col-md-5">
									<label class="mini-label fw-bold mb-0">상담근거 상품 (리스트 선택 또는 검색)</label>
									<div class="input-group input-group-sm">
										<input type="text" class="form-control form-control-sm bg-primary-subtle border-0 fw-bold" :value="consultData.itemnm" readonly placeholder="리스트 선택 또는 검색" />
										<button class="btn btn-outline-primary px-2" type="button" @click="handleOpenHelp('ITEM')"><i class="bi bi-search"></i></button>
									</div>
								</div>
								<div class="col-md-5">
									<label class="mini-label fw-bold mb-0">AI 상담 요약</label>
									<div class="input-group input-group-sm">
										<input type="text" class="form-control border-warning-subtle bg-warning bg-opacity-10 fw-bold" v-model="consultData.ai_summary" placeholder="자동 생성">
										<button class="btn btn-warning fw-bold text-dark px-3" type="button" @click="handleAiSummarize" :disabled="isSummarizing">
											<i class="bi bi-robot me-1"></i> AI 생성
										</button>
									</div>
								</div>
							</div>

							<!-- 문의/답변 텍스트 영역 -->
							<div class="row g-2 flex-grow-1 overflow-hidden">
								<div class="col-md-6 d-flex flex-column h-100">
									<label class="mini-label fw-bold text-primary mb-0"><i class="bi bi-question-circle-fill me-1"></i>문의 내용</label>
									<textarea class="form-control form-control-sm border-light bg-light bg-opacity-50 flex-grow-1" v-model="consultData.trb_ment" style="resize:none; font-size: 0.85rem;" placeholder="상담 문의 내용을 입력하세요."></textarea>
								</div>
								<div class="col-md-6 d-flex flex-column h-100">
									<label class="mini-label fw-bold text-success mb-0"><i class="bi bi-check-circle-fill me-1"></i>답변 내용</label>
									<textarea class="form-control form-control-sm border-light bg-light bg-opacity-50 flex-grow-1" v-model="consultData.ans_ment" style="resize:none; font-size: 0.85rem;" placeholder="상담 답변 내용을 입력하세요."></textarea>
								</div>
							</div>

							<!-- 에스컬레이션 메모 -->
							<div v-if="consultData.escalation_yn === 'Y'" class="flex-shrink-0 pt-2 border-top border-light">
								<div class="p-2 bg-danger bg-opacity-10 rounded border border-danger border-opacity-25">
									<label class="mini-label fw-bold text-danger mb-1"><i class="bi bi-exclamation-triangle-fill me-1"></i>이관 요청 상세 메모 (담당자 전달용)</label>
									<textarea class="form-control form-control-sm bg-white border-danger border-opacity-25" rows="2" v-model="consultData.esc_memo" placeholder="이관 부서에 전달할 내용을 입력하세요."></textarea>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>

			<!-- 3. 하단 통합 이력 탭 -->
			<div class="card shadow-sm border-0 overflow-hidden flex-grow-1 d-flex flex-column border-top border-4 border-dark">
				<div class="bg-white px-3 pt-2 d-flex justify-content-between align-items-center flex-shrink-0">
					<ul class="nav nav-tabs border-bottom-0 gap-1">
						<li class="nav-item"><button class="nav-link small py-1 px-3" :class="{ active: activeTab === 1 }" @click="activeTab = 1">과거 상담 이력</button></li>
						<li class="nav-item"><button class="nav-link small py-1 px-3" :class="{ active: activeTab === 2 }" @click="activeTab = 2">서비스 처리 내역</button></li>
						<li class="nav-item"><button class="nav-link small py-1 px-3" :class="{ active: activeTab === 3 }" @click="activeTab = 3">정산 / 수금 내역</button></li>
					</ul>
				</div>
				<div class="tab-content bg-white flex-grow-1 overflow-hidden">
					<div v-show="activeTab === 1" class="h-100 p-2"><div ref="tableRef1" class="tabulator-omni-fit"></div></div>
					<div v-show="activeTab === 2" class="h-100 p-2"><div ref="tableRef2" class="tabulator-omni-fit"></div></div>
					<div v-show="activeTab === 3" class="h-100 p-2"><div ref="tableRef3" class="tabulator-omni-fit"></div></div>
				</div>
			</div>
		</div>

		<!-- 공통 팝업 모달 -->
		<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
	</div>
</template>

<script lang="ts">
export default {
    name: 'HGIA010U'
}
</script>

<script setup lang="ts">
import { ref, watch, nextTick, onBeforeUnmount, computed, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import { useCtiStore } from '@/stores/ctiStore'
import { useAuthStore } from '@/stores/authStore'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import { useCommonHelp } from '@/composables/useCommonHelp'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()
const { modalVisible, modalProps, openHelp } = useCommonHelp()
const ctiStore = useCtiStore(); const authStore = useAuthStore()

const activeTab = ref(1); const itemList = ref<any[]>([]); const isSummarizing = ref(false)
const queueMap: Record<string, string> = { 'sales_group': '영업상담', 'support_group': '고객지원상담', 'billing_group': '요금상담' };

const customerInfo = ref({ custcd: '', custnm: '미등록 고객', usernm: '', hpno: '', email: '', address: '' })
const consultData = ref({
	date: new Date().toISOString().substring(0, 10),
	ai_summary: '', trb_ment: '', ans_ment: '',
	itemcd: '', itemnm: '', iono: '',
	deptcd: '', esc_memo: '', escalation_no: '', escalation_yn: 'N',
	ai_mode: 'auto' // 🚀 [최종] AI 자동 요약을 기본 모드로 설정 (상담원 편의 극대화)
})

const handleOpenHelp = (type: string) => {
	if (type === 'CUST') {
		openHelp('CUST', (modalData) => {
			customerInfo.value.custcd = modalData.custcd;
			customerInfo.value.custnm = modalData.custnm;
			customerInfo.value.usernm = modalData.bossnm || modalData.name || '';
			customerInfo.value.hpno = modalData.telno || modalData.PHONENO || '';
			customerInfo.value.email = modalData.email || '';
			loadCustomerDetails(modalData.custcd);
		});
	} else if (type === 'ITEM') {
		openHelp('ITEM', (modalData) => {
			consultData.value.itemcd = modalData.itemcd;
			consultData.value.itemnm = modalData.itemnm;
			consultData.value.iono = '';
		});
	}
}

const loadCustomerDetails = async (custcd: string) => {
	if (!custcd) return;
	try {
		const [items, hist] = await Promise.all([
			api.get('/crm/inbound/item-list', { params: { custcd } }),
			api.get('/crm/inbound/call-history', { params: { custcd } })
		]);
		itemList.value = items.data || [];
		tableInstance1?.setData(hist.data || []);
	} catch (e) {}
}

const handleAiSummarize = async () => {
    // 🚀 [해결] 헬프 모달이 직접 백엔드 API를 호출하도록 정석대로 구성
    Object.assign(modalProps, {
        title: '최근 녹취 파일 선택 (AI 분석용)',
        path: '/crm/inbound/recording-list-pop', // 🚀 새로 만든 전용 API 주소
        data: {}, // 서버에 전달할 파라미터
        columns: [
            { title: "파일명", field: "filename", widthGrow: 2 },
            { title: "생성시간", field: "mtime", width: 160 },
            { title: "크기", field: "size", width: 80 }
        ],
        onConfirm: (selectedFile: any) => {
            startAiAnalysis(selectedFile.filename);
        }
    });
    modalVisible.value = true;
}

// 🚀 실제 AI 분석 수행 로직 분리
const startAiAnalysis = async (filename: string) => {
    isSummarizing.value = true;
    vAlert('🤖 AI가 상담 내용을 분석 중입니다...');

    try {
        const res = await api.get('/crm/inbound/analyze-audio', { params: { file: filename } });
        console.log('📥 [AI 분석 결과 수신]:', res.data);

        if (res.data) {
            // 🚀 [해결] 어떤 상황에서도 수신된 데이터는 즉시 화면에 노출합니다.
            const data = res.data;
            consultData.value.trb_ment = data.trb_ment || '내용 없음';
            consultData.value.ans_ment = data.ans_ment || '내용 없음';
            consultData.value.ai_summary = data.summary || '요약 실패';

            if (String(data.summary).includes('오류')) {
                vAlertError('⚠️ 상담 내용은 분석되었으나, AI 요약은 사용량 초과로 실패했습니다.');
            } else {
                vAlert('✅ AI 분석 결과가 화면에 반영되었습니다.');
            }

            // 🚀 강제 렌더링 유도를 위해 넥스트틱 활용
            await nextTick();
        }
    } catch (e) {
        console.error('❌ AI 분석 API 오류:', e);
        vAlertError('AI 서버와 통신할 수 없습니다.');
    } finally {
        isSummarizing.value = false;
    }
}

const handleSave = async () => {
	if (ctiStore.isTalking) { vAlertError('통화 중에는 저장할 수 없습니다.'); return; }
	if (!customerInfo.value.custcd) { vAlertError('거래처를 선택해 주세요.'); return; }
	try {
		const payload = {
			dto: {
				...consultData.value,
				custcd: customerInfo.value.custcd,
				cmpycd: authStore.cmpycd,
				svcymd: consultData.value.date.replaceAll('-', ''),
				linkedid: ctiStore.incomingCall?.linkedid,
				interaction_id: ctiStore.incomingCall?.uniqueid || 'MANUAL_' + new Date().getTime(),
				call_telno: customerInfo.value.hpno,
				call_usernm: customerInfo.value.usernm,
				call_email: customerInfo.value.email
			},
			recordings: ctiStore.recordingFile ? [ctiStore.recordingFile] : [],
			ai_mode: consultData.value.ai_mode // 🚀 [추가] AI 모드 전송
		};
		await api.post('/crm/inbound/save', payload);
		vAlert('상담 내용이 성공적으로 저장되었습니다.');
	} catch (e) { vAlertError('저장 처리 중 오류가 발생했습니다.'); }
};

const tableRef1 = ref(null); const tableRef2 = ref(null); const tableRef3 = ref(null);
let tableInstance1: Tabulator | null = null; let tableInstance2: Tabulator | null = null; let tableInstance3: Tabulator | null = null;

const initGrids = async () => {
	await nextTick();
    const commonConfig = { layout: 'fitColumns', height: '100%', placeholder: "내역이 없습니다." };
	if (tableRef1.value) {
        if (tableInstance1) tableInstance1.destroy();
        tableInstance1 = new Tabulator(tableRef1.value, { ...commonConfig, columns: [{ title: "접수번호", field: "svcno", width: 120 }, { title: "상담일시", field: "start_time", width: 150 }, { title: "상담원", field: "consultnm", width: 100 }, { title: "상담요약", field: "ai_summary", hozAlign: 'left' }]});
    }
	if (tableRef2.value) {
        if (tableInstance2) tableInstance2.destroy();
        tableInstance2 = new Tabulator(tableRef2.value, { ...commonConfig, columns: [{ title: "접수번호", field: "svcno", width: 120 }, { title: "접수일자", field: "acceptymd", width: 120 }, { title: "결과", field: "fixed_ment", hozAlign: 'left' }]});
    }
	if (tableRef3.value) {
        if (tableInstance3) tableInstance3.destroy();
        tableInstance3 = new Tabulator(tableRef3.value, { ...commonConfig, columns: [{ title: "발생일", field: "ymd", width: 120 }, { title: "품목", field: "itemnm", hozAlign: 'left' }, { title: "합계", field: "totamt", hozAlign: 'right', formatter: 'money', width: 120 }]});
    }
};

const selectProduct = (item: any) => { consultData.value.itemnm = item.itemnm; consultData.value.iono = item.iono; consultData.value.itemcd = item.itemcd; }

const handleNew = () => {
	if(confirm('모든 입력을 초기화하고 새 상담을 시작하시겠습니까?')) {
		customerInfo.value = { custcd: '', custnm: '미등록 고객', usernm: '', hpno: '', email: '', address: '' };
		consultData.value = {
			date: new Date().toISOString().substring(0, 10),
			ai_summary: '', trb_ment: '', ans_ment: '',
			itemcd: '', itemnm: '', iono: '',
			deptcd: '', esc_memo: '', escalation_no: '', escalation_yn: 'N',
			ai_mode: 'manual'
		};
		itemList.value = [];
		tableInstance1?.clearData();
	}
};

const handleAnswer = async () => { try { await api.get(`/crm/cti/answer?exten=${ctiStore.incomingCall?.exten}`); } catch (e) {} }
const handleHangup = async () => { try { await api.get(`/crm/cti/hangup?exten=${ctiStore.incomingCall?.exten}`); } catch (e) {} }

const setCallData = async (data: any) => {
	if (!data) return;
	customerInfo.value = {
		custcd: data.custcd || '',
		custnm: data.custnm || '미등록',
		usernm: data.usernm || '',
		hpno: data.hpno || data.callerid || '',
		email: data.email || '',
		address: data.address || ''
	};
	itemList.value = data.itemlist || [];
	if (data.custcd) loadCustomerDetails(data.custcd);
};

// 🚀 [추가] 회사별 기본 AI 설정 로드
const fetchCompanyConfig = async () => {
    try {
        const res = await api.post('/haba/HABA_100U_STR', { actkind: 'S0', cmpycd: authStore.cmpycd });
        if (res.data && res.data.length > 0) {
            consultData.value.ai_mode = res.data[0].ai_mode || 'manual';
            console.log('🤖 [AI Mode Config] Default:', consultData.value.ai_mode);
        }
    } catch (e) { console.error('환경설정 로드 실패') }
}

onMounted(() => {
	initGrids();
	if (ctiStore.incomingCall) setCallData(ctiStore.incomingCall);
    fetchCompanyConfig(); // 🚀 로드 시 설정 가져오기
});

// 💡 [최종 종결] 인바운드 신호 감지 시 어떤 조건에서도 화면을 즉시 팝업 데이터로 채움
watch(() => ctiStore.incomingCall, (newCall) => {
	if (newCall && newCall.type === 'INBOUND_CALL') {
		console.log('🚀 [CTI POPUP] 수신 신호 처리 시작:', newCall.callerid);

		const cleanCustCd = String(newCall.custcd || '').trim();

		// 1. 고객 기본 정보 매핑 (전화번호, 이름 등)
		customerInfo.value = {
			custcd: cleanCustCd,
			custnm: (newCall.custnm || '미등록 고객').trim(),
			usernm: (newCall.usernm || '').trim(),
			hpno: newCall.callerid || '',
			email: (newCall.email || '').trim(),
			address: (newCall.address || '').trim()
		};

		// 2. 상담 데이터 영역 초기화
		consultData.value.date = new Date().toISOString().substring(0, 10);
		consultData.value.trb_ment = '';
		consultData.value.ans_ment = '';
        consultData.value.ai_summary = ''; // 초기화

		// 3. 화면 탭 강제 이동 및 데이터 로드 (과거이력 등)
		activeTab.value = 1;
		if (cleanCustCd) {
			loadCustomerDetails(cleanCustCd);
		}

		// 4. 시각적 알림 (vAlert)
		vAlert(`📞 [전화 수신] ${customerInfo.value.custnm} (${newCall.callerid})`);
	}
}, { deep: true });

// 🚀 [신규] 통화 종료 시 자동 AI 분석 및 필드 채우기
watch(() => ctiStore.recordingFile, async (newFile) => {
    if (newFile && consultData.value.ai_mode === 'auto') {
        console.log('🤖 [AI 자동화] 통화 종료 감지, AI 분석 시작:', newFile);
        isSummarizing.value = true;
        vAlert('🤖 AI가 통화 내용을 분석 중입니다. 잠시만 기다려 주세요...');

        try {
            // 방금 만든 분석 API 호출
            const res = await api.get('/crm/inbound/analyze-audio', { params: { file: newFile } });
            if (res.data) {
                console.log('✅ [AI 분석 완료]:', res.data);
                consultData.value.trb_ment = res.data.trb_ment || '';
                consultData.value.ans_ment = res.data.ans_ment || '';
                consultData.value.ai_summary = res.data.summary || '';
                vAlert('✅ AI 분석이 완료되어 상담 내용이 자동 입력되었습니다.');
            }
        } catch (e) {
            console.error('AI 자동 분석 실패:', e);
            vAlertError('AI 자동 분석에 실패했습니다.');
        } finally {
            isSummarizing.value = false;
        }
    }
});

onBeforeUnmount(() => { tableInstance1?.destroy(); tableInstance2?.destroy(); tableInstance3?.destroy(); });
</script>

<style scoped>
.hgia-workspace { height: 100vh; font-family: 'Pretendard', sans-serif; }
.omni-header { min-height: 56px; }
.dot-blink { width: 10px; height: 10px; border-radius: 50%; animation: blinker 1s linear infinite; }
@keyframes blinker { 50% { opacity: 0; } }
.mini-label { font-size: 0.75rem; color: #6c757d; display: block; margin-bottom: 2px; }
.tabulator-omni-fit { height: 100% !important; border: 1px solid #dee2e6; font-size: 0.85rem; }
.nav-tabs .nav-link { border: 0; color: #666; font-weight: 600; cursor: pointer; }
.nav-tabs .nav-link.active { color: #0d6efd; border-bottom: 2px solid #0d6efd; background: transparent; }
.bg-talking { background-color: #fcf2f2 !important; }
</style>