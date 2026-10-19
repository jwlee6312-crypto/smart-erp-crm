<!--
	=============================================================
	프로그램명	  : 인바운드 상담 (CtiDrawer 기능 100% 통합)
    프로그램 ID	: HGIA010U
	작성일자	    : 25.02.24
	작성자	      : AI Assistant
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="container-fluid py-2 bg-light min-vh-100 text-start">
		<!-- 1. 상단 CTI 제어 툴바 -->
		<div class="drawer-header d-flex justify-content-between align-items-center p-3 text-white rounded shadow-sm mb-3 transition-all"
			 :class="ctiStore.isTalking ? 'bg-danger' : 'bg-primary'">
			<div class="d-flex align-items-center gap-3">
				<h5 class="m-0 fw-bold">
					<i class="bi bi-headset me-2"></i>
					<span v-if="ctiStore.incomingCall?.queue" class="badge bg-warning text-dark me-2 shadow-sm">
						{{ queueMap[ctiStore.incomingCall.queue] || '상담그룹' }}
					</span>
					인바운드 상담 관리
				</h5>
				<div v-if="ctiStore.isTalking" class="talking-status-badge d-flex align-items-center gap-2 px-3 py-1 rounded-pill bg-white text-danger shadow-sm">
					<span class="dot-blink"></span>
					<span class="fw-bold small">TALKING...</span>
				</div>
			</div>
			<div class="d-flex gap-2">
				<button class="btn btn-sm btn-outline-light fw-bold px-3 shadow-sm" @click="handleNew">신규</button>
				<button v-if="!ctiStore.isTalking && ctiStore.incomingCall" class="btn btn-sm btn-light fw-bold text-success px-3 shadow-sm" @click="handleAnswer">전화받기</button>
				<button v-if="ctiStore.incomingCall" class="btn btn-sm btn-info text-white fw-bold px-3 shadow-sm" :disabled="isTransferLimitReached" @click="openTransferModal">
					{{ isTransferLimitReached ? '이관한도초과' : '돌려주기' }}
				</button>
				<button class="btn btn-sm btn-danger fw-bold px-3 shadow-sm border-white" @click="handleHangup">전화끊기</button>
				<button
					class="btn btn-sm fw-bold border-white px-3 ms-2 shadow-sm"
					:class="ctiStore.isTalking ? 'btn-secondary opacity-50' : 'btn-success'"
					:disabled="ctiStore.isTalking"
					@click="handleSave">
					<i class="bi bi-save me-1"></i>상담저장
				</button>
			</div>
		</div>

		<!-- 2. 상담 본문 -->
		<div class="p-0 transition-all" :class="ctiStore.isTalking ? 'bg-talking' : ''">
			<div v-if="ctiStore.isTalking" class="alert alert-danger d-flex align-items-center mb-3 shadow-sm border-0">
				<div class="spinner-grow spinner-grow-sm text-danger me-3"></div>
				<div class="fw-bold">현재 고객과 통화 중입니다. 통화가 종료되면 상담 내용을 저장할 수 있습니다.</div>
			</div>

			<div class="row g-3 mb-4">
				<!-- [좌측] 고객기본정보 + 가입상품 -->
				<div class="col-md-4 d-flex flex-column gap-3">
					<div class="card shadow-sm border-0">
						<div class="card-header bg-white fw-bold border-bottom-0 pt-3 text-secondary small d-flex justify-content-between align-items-center">
							<span>고객기본정보</span>
							<span v-if="ctiStore.incomingCall?.queue" class="text-primary" style="font-size: 0.7rem;">
								Route: {{ queueMap[ctiStore.incomingCall.queue] }} (Linear)
							</span>
						</div>
						<div class="card-body pt-0 pb-3">
							<div class="row g-2">
								<div class="col-12 mb-1">
									<label class="form-label small fw-bold text-muted mb-1">거래처명 [코드]</label>
									<div class="input-group input-group-sm">
										<input type="text" class="form-control bg-light fw-bold text-primary" :value="customerInfo.custnm + (customerInfo.custcd ? ' [' + customerInfo.custcd + ']' : '')" readonly />
										<button class="btn btn-dark" type="button" @click="openCustModal"><i class="bi bi-search"></i></button>
									</div>
								</div>
								<div class="col-6"><label class="form-label mini-label fw-bold text-muted mb-0">담당자/대표자</label><input type="text" class="form-control form-control-sm border-0 bg-white" v-model="customerInfo.usernm" readonly /></div>
								<div class="col-6"><label class="form-label mini-label fw-bold text-muted mb-0">수신번호(CID)</label><input type="text" class="form-control form-control-sm border-0 bg-white" v-model="customerInfo.hpno" readonly /></div>
								<div class="col-12"><label class="form-label mini-label fw-bold text-muted mb-0">이메일</label><input type="text" class="form-control form-control-sm border-0 bg-white" :value="customerInfo.email" readonly /></div>
								<div class="col-12"><label class="form-label mini-label fw-bold text-muted mb-0">주소</label><textarea class="form-control form-control-sm border-0 bg-white" rows="2" style="resize:none;" :value="customerInfo.address" readonly></textarea></div>
							</div>
						</div>
					</div>
					<div class="card shadow-sm border-0 flex-grow-1">
						<div class="card-header bg-white fw-bold border-bottom-0 pt-3 text-secondary small d-flex justify-content-between align-items-center">
							<span>가입상품 / 매출내역</span><span class="badge bg-primary rounded-pill">{{ itemList.length }}건</span>
						</div>
						<div class="card-body p-0 overflow-auto" style="max-height: 250px;">
							<table class="table table-hover table-sm mb-0 small text-center align-middle">
								<thead class="table-light sticky-top"><tr><th>매출일자</th><th>상품명</th><th>출고번호</th></tr></thead>
								<tbody class="cursor-pointer">
									<tr v-for="item in itemList" :key="item.iono" @click="selectProduct(item)" :class="{'table-primary fw-bold': consultData.iono === item.iono}">
										<td>{{ item.salsymd }}</td><td class="text-start ps-3">{{ item.itemnm }}</td><td>{{ item.iono }}</td>
									</tr>
									<tr v-if="itemList.length === 0"><td colspan="3" class="py-4 text-muted small italic text-center">내역 없음</td></tr>
								</tbody>
							</table>
						</div>
					</div>
				</div>

				<!-- [우측] 상담내용 작성 + 에스컬레이션 -->
				<div class="col-md-8 d-flex flex-column gap-3">
					<div class="card shadow-sm border-0 border-top border-primary border-3 transition-all flex-grow-1" :class="{'border-danger': ctiStore.isTalking}">
						<div class="card-header bg-white fw-bold border-bottom-0 pt-3 text-secondary small">상담내용 작성</div>
						<div class="card-body">
							<div class="row g-3">
								<div class="col-md-3">
									<label class="form-label small fw-bold text-muted">상담일자</label>
									<input type="date" class="form-control form-control-sm" v-model="consultData.date" />
								</div>
								<div class="col-md-6">
									<label class="form-label small fw-bold text-muted">상담근거-상품명</label>
									<input type="text" class="form-control form-control-sm border-primary-subtle bg-primary-subtle fw-bold" :value="consultData.itemnm" readonly />
								</div>
								<div class="col-md-3">
									<label class="form-label small fw-bold text-muted">출고번호</label>
									<input type="text" class="form-control form-control-sm border-primary-subtle bg-primary-subtle fw-bold" :value="consultData.iono" readonly />
								</div>

								<div class="col-12">
									<label class="form-label small fw-bold text-muted">상담요약 (AI 분석 추천)</label>
									<div class="input-group input-group-sm">
										<input type="text" class="form-control border-warning-subtle" v-model="consultData.summary" placeholder="문의/답변 입력 후 AI 요약을 실행하세요." />
										<button class="btn btn-warning fw-bold shadow-sm" type="button" @click="handleAiSummarize" :disabled="isSummarizing">
											<span v-if="isSummarizing" class="spinner-border spinner-border-sm me-1"></span>
											<i v-else class="bi bi-robot me-1"></i> AI 자동요약
										</button>
									</div>
								</div>
								<div class="col-md-6"><label class="form-label small fw-bold text-muted">문의내용</label><textarea class="form-control" rows="5" v-model="consultData.inquiry" placeholder="고객 문의 내용을 입력하세요."></textarea></div>
								<div class="col-md-6"><label class="form-label small fw-bold text-muted">답변내용</label><textarea class="form-control" rows="5" v-model="consultData.answer" placeholder="상담 답변 내용을 입력하세요."></textarea></div>
							</div>
						</div>
					</div>

					<!-- 에스컬레이션(이관) 섹션 -->
					<div class="card shadow-sm border-0 border-top border-warning border-3">
						<div class="card-body p-3">
							<div class="form-check form-switch mb-2">
								<input class="form-check-input" type="checkbox" id="escCheck" v-model="consultData.escalation_yn" true-value="Y" false-value="N">
								<label class="form-check-label fw-bold text-danger" for="escCheck">타 부서 업무 이관 (에스컬레이션/VOC 등록)</label>
							</div>
							<div v-if="consultData.escalation_yn === 'Y'" class="mt-2 scale-in">
								<label class="form-label mini-label fw-bold text-danger mb-1">
									<i class="bi bi-send-plus-fill me-1"></i>업무 이관 상세 (전달 부서/담당자/내용)
								</label>
								<textarea class="form-control form-control-sm border-danger-subtle" rows="3" v-model="consultData.esc_memo" placeholder="어느 부서의 누구에게 어떤 내용을 전달해야 하는지 구체적으로 작성해 주세요."></textarea>
							</div>
						</div>
					</div>

					<!-- 녹취 정보 알림 -->
					<div class="col-12" v-if="ctiStore.recordingFile || ctiStore.isTalking">
						<div class="alert p-2 mb-0 d-flex align-items-center shadow-sm border"
							 :class="ctiStore.isTalking ? 'alert-danger border-danger-subtle bg-danger-subtle' : 'alert-success border-success-subtle bg-success-subtle'">
							<template v-if="ctiStore.isTalking">
								<div class="spinner-grow spinner-grow-sm text-danger me-2"></div>
								<span class="small fw-bold text-danger">실시간 녹취 진행 중...</span>
							</template>
							<template v-else-if="ctiStore.recordingFile">
								<i class="bi bi-check-circle-fill me-2 text-success"></i>
								<span class="small fw-bold text-dark">녹취 생성 완료: </span>
								<span class="small fw-bold text-primary ms-2">{{ ctiStore.recordingFile }}</span>
							</template>
						</div>
					</div>
				</div>
			</div>

			<!-- 탭 기반 이력 조회 -->
			<div class="card shadow-sm border-0 overflow-hidden">
				<ul class="nav nav-tabs px-3 pt-2 bg-white border-bottom">
					<li class="nav-item"><button class="nav-link" :class="{ active: activeTab === 1 }" @click="activeTab = 1">과거 상담 이력</button></li>
					<li class="nav-item"><button class="nav-link" :class="{ active: activeTab === 2 }" @click="activeTab = 2">서비스처리내역</button></li>
					<li class="nav-item"><button class="nav-link" :class="{ active: activeTab === 3 }" @click="activeTab = 3">정산/수금내역</button></li>
				</ul>
				<div class="tab-content bg-white" style="min-height: 400px;">
					<div v-show="activeTab === 1" class="p-3"><div ref="tableRef1"></div></div>
					<div v-show="activeTab === 2" class="p-3"><div ref="tableRef2"></div></div>
					<div v-show="activeTab === 3" class="p-3"><div ref="tableRef3"></div></div>
				</div>
			</div>
		</div>

		<!-- 전화 돌려주기 모달 -->
		<div v-if="showTransferModal" class="transfer-modal-overlay d-flex justify-content-center align-items-center">
			<div class="transfer-modal card shadow-lg border-0" style="width: 550px;">
				<div class="card-header bg-info text-white fw-bold d-flex justify-content-between align-items-center py-3">
					<div class="d-flex align-items-center"><i class="bi bi-telephone-forward-fill me-2"></i>전화 돌려주기</div>
					<button type="button" class="btn-close btn-close-white" @click="showTransferModal = false"></button>
				</div>
				<div class="card-body p-0">
					<div class="p-3 bg-primary-subtle border-bottom small text-start">
						<div class="fw-bold text-primary mb-1">전달될 상담 정보</div>
						<div><strong>거래처:</strong> {{ customerInfo.custnm }}</div>
						<div><strong>문의요약:</strong> {{ consultData.summary || '(입력없음)' }}</div>
					</div>
					<div class="agent-list overflow-auto" style="max-height: 350px;">
						<table class="table table-hover table-sm align-middle mb-0 text-center">
							<thead class="table-light sticky-top">
								<tr><th>내선</th><th>이름</th><th>부서</th><th>상태</th><th>연결</th></tr>
							</thead>
							<tbody>
								<tr v-for="agent in filteredAgents" :key="agent.inner_no">
									<td class="fw-bold text-primary">{{ agent.inner_no }}</td>
									<td>{{ agent.usernm }}</td>
									<td class="small text-muted text-start">{{ agent.deptnm }}</td>
									<td>
										<span v-if="agent.isOnline" class="badge bg-success rounded-pill px-2" style="font-size: 0.65rem;">ON</span>
										<span v-else class="badge bg-secondary rounded-pill px-2" style="font-size: 0.65rem;">OFF</span>
									</td>
									<td>
										<button class="btn btn-xs btn-primary py-0" :disabled="!agent.isOnline" @click="executeTransfer(agent.inner_no)">연결</button>
									</td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
			</div>
		</div>

		<!-- 거래처 검색 모달 -->
		<Modal v-model:visible="showCustModal" :modalProps="custModalProps" />
	</div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onBeforeUnmount, computed, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import { useCtiStore } from '@/stores/ctiStore'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()
const ctiStore = useCtiStore()
const activeTab = ref(1)
const itemList = ref<any[]>([])
const isSummarizing = ref(false)
const showTransferModal = ref(false)
const searchAgent = ref('')
const agents = ref<any[]>([])

const queueMap: Record<string, string> = { 'sales_group': '영업상담', 'support_group': '고객지원상담', 'billing_group': '요금상담' };

const customerInfo = ref({ custcd: '', custnm: '미등록 고객', usernm: '', hpno: '', email: '', address: '' })
const consultData = ref({
	date: new Date().toISOString().substring(0, 10),
	summary: '', inquiry: '', answer: '',
	itemcd: '', itemnm: '', iono: '',
	deptcd: '', esc_memo: '', escalation_no: '', escalation_yn: 'N'
})

// 거래처 검색 모달
const showCustModal = ref(false)
const custModalProps = ref<ModalProps>({
	type: 'table', large: false, title: '거래처검색', defaultField: 'custnm',
	columns: [
		{ title: '코드', field: 'custcd', width: 100 },
		{ title: '거래처', field: 'custnm', hozAlign: 'left' },
		{ title: '전화번호', field: 'telno', hozAlign: 'center' }
	],
	path: '/popup/pop-cust',
	data: { sch_custnm: '' },
	onConfirm: (modalData) => {
		customerInfo.value.custcd = modalData.custcd;
		customerInfo.value.custnm = modalData.custnm;
		customerInfo.value.hpno = modalData.telno || customerInfo.value.hpno;
		showCustModal.value = false;
		loadTabData(1);
	},
})
const openCustModal = () => { showCustModal.value = true; }

const isTransferLimitReached = computed(() => (ctiStore.incomingCall?.transfer_cnt || 0) >= 2);

const handleAiSummarize = async () => {
	if (!consultData.value.inquiry) { vAlertError('문의 내용을 입력해 주세요.'); return; }
	isSummarizing.value = true;
	try {
		const res = await api.post('/crm/inbound/ai-summarize', {
			trb_ment: consultData.value.inquiry,
			ans_ment: consultData.value.answer
		});
		if (res.data) { consultData.value.summary = res.data.summary; consultData.value.deptcd = res.data.deptcd; }
	} catch (e) { vAlertError('AI 요약 실패'); }
	finally { isSummarizing.value = false; }
}

const loadAgents = async () => {
	try {
		const [userRes, activeExtensRes] = await Promise.all([
			api.get('/common/user/list', { params: { schuseyn: 'Y' } }),
			api.get('/crm/cti/active-agents')
		]);
		const activeExtens = activeExtensRes.data || [];
		agents.value = (userRes.data || []).filter((u: any) => u.inner_no).map((u: any) => ({
			...u, isOnline: activeExtens.includes(u.inner_no)
		}));
	} catch (e) {}
}

const filteredAgents = computed(() => {
	if (!searchAgent.value) return agents.value;
	const search = searchAgent.value.toLowerCase();
	return agents.value.filter(a => (a.usernm?.toLowerCase().includes(search)) || (a.inner_no?.includes(search)));
})

const openTransferModal = async () => { await loadAgents(); showTransferModal.value = true; }

const executeTransfer = async (targetExten: string) => {
	if (!confirm(`${targetExten}번 상담원에게 전화를 돌려주시겠습니까?`)) return;
	try {
		await api.get(`/crm/cti/transfer`, {
			params: {
				exten: ctiStore.incomingCall?.exten,
				target: targetExten,
				custcd: customerInfo.value.custcd,
				itemnm: consultData.value.itemnm,
				iono: consultData.value.iono,
				summary: consultData.value.summary,
				inquiry: consultData.value.inquiry,
				answer: consultData.value.answer
			}
		});
		showTransferModal.value = false;
		vAlert('전화 돌려주기가 시도되었습니다.');
	} catch (e) { vAlertError('돌려주기 실패'); }
}

const tableRef1 = ref<HTMLDivElement | null>(null); const tableRef2 = ref<HTMLDivElement | null>(null); const tableRef3 = ref<HTMLDivElement | null>(null);
let tableInstance1: Tabulator | null = null; let tableInstance2: Tabulator | null = null; let tableInstance3: Tabulator | null = null;

const loadTabData = async (tab: number) => {
	if (!customerInfo.value.custcd) return;
	try {
		let url = tab === 1 ? '/crm/inbound/call-history' : tab === 2 ? '/crm/inbound/service-history' : '/crm/inbound/settle-history';
		const res = await api.get(url, { params: { custcd: customerInfo.value.custcd } });
		if (tab === 1) tableInstance1?.setData(res.data || []);
		else if (tab === 2) tableInstance2?.setData(res.data || []);
		else if (tab === 3) tableInstance3?.setData(res.data || []);
	} catch (e) {}
};

watch(activeTab, async (tab) => { await loadTabData(tab); });

const initGrids = async () => {
	await nextTick();
	if (tableRef1.value && !tableInstance1) tableInstance1 = new Tabulator(tableRef1.value, {
		layout: 'fitColumns',
		height: '350px',
		columns: [
			{ title: "접수번호", field: "svcno", width: 120 },
			{ title: "상담일시", field: "start_time", width: 140 },
			{ title: "발신번호", field: "hpno", width: 110 },
			{ title: "발신자", field: "call_usernm", width: 90 },
			{ title: "상담원", field: "consultnm", width: 90 },
			{ title: "상담요약", field: "ai_summary", widthGrow: 1 }
		]
	});
	if (tableRef2.value && !tableInstance2) tableInstance2 = new Tabulator(tableRef2.value, { layout: 'fitColumns', height: '350px', columns: [{ title: "접수번호", field: "svcno" }, { title: "접수일자", field: "acceptymd" }, { title: "수리기사", field: "fixed_usernm" }, { title: "수리결과", field: "fixed_ment" }]});
	if (tableRef3.value && !tableInstance3) tableInstance3 = new Tabulator(tableRef3.value, { layout: 'fitColumns', height: '350px', columns: [{ title: "발생일", field: "ymd" }, { title: "품목/적요", field: "itemnm" }, { title: "합계", field: "totamt", hozAlign: 'right', formatter: 'money' }]});
};

const setCallData = async (data: any) => {
	if (!data) return;
	customerInfo.value = { custcd: data.custcd || '', custnm: data.custnm || '미등록', usernm: data.usernm || '', hpno: data.hpno || data.callerid || '', email: data.email || '', address: data.address || '' };
	itemList.value = data.itemlist || [];
	if (data.isTransferred || data.queue) {
		consultData.value.itemnm = data.itemnm || ''; consultData.value.iono = data.iono || ''; consultData.value.itemcd = data.itemcd || '';
		consultData.value.inquiry = data.inquiry || ''; consultData.value.answer = data.answer || '';
		if (data.queue && queueMap[data.queue]) consultData.value.summary = `[${queueMap[data.queue]} 인입] `;
	}
	activeTab.value = 1; await loadTabData(1);
};

onMounted(() => { initGrids(); if (ctiStore.incomingCall) setCallData(ctiStore.incomingCall); });
watch(() => ctiStore.incomingCall, (newCall) => { if (newCall) setCallData(newCall); }, { deep: true });

const handleAnswer = async () => { try { await api.get(`/crm/cti/answer?exten=${ctiStore.incomingCall?.exten}`); } catch (e) {} }
const handleHangup = async () => { try { await api.get(`/crm/cti/hangup?exten=${ctiStore.incomingCall?.exten}`); } catch (e) {} }
const selectProduct = (item: any) => { consultData.value.itemnm = item.itemnm; consultData.value.iono = item.iono; consultData.value.itemcd = item.itemcd; }
const handleNew = () => { if(confirm('새 상담을 시작하시겠습니까?')) location.reload(); };

const handleSave = async () => {
	if (ctiStore.isTalking) { vAlertError('통화 중에는 저장할 수 없습니다.'); return; }
	if (!customerInfo.value.custcd) { vAlertError('거래처를 선택해 주세요.'); return; }
	try {
		await api.post('/crm/inbound/save', {
			dto: {
				...consultData.value,
				custcd: customerInfo.value.custcd,
				cmpycd: 'HAIONNET',
				svcymd: consultData.value.date.replaceAll('-', ''),
				linkedid: ctiStore.incomingCall?.linkedid,
				call_telno: customerInfo.value.hpno,
				call_usernm: customerInfo.value.usernm,
				call_email: customerInfo.value.email
			},
			recordings: ctiStore.recordingFile ? [ctiStore.recordingFile] : []
		});
		vAlert('상담 내용이 저장되었습니다.');
	} catch (e) { vAlertError('저장 실패'); }
};

onBeforeUnmount(() => { tableInstance1?.destroy(); tableInstance2?.destroy(); tableInstance3?.destroy(); });
</script>

<style scoped>
.dot-blink { width: 10px; height: 10px; background-color: #dc3545; border-radius: 50%; animation: blinker 1s linear infinite; }
@keyframes blinker { 50% { opacity: 0; } }
.transfer-modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0,0,0,0.5); z-index: 11000; }
.btn-xs { padding: 1px 5px; font-size: 0.75rem; }
.mini-label { font-size: 0.7rem; color: #6c757d; }
.scale-in { animation: scaleIn 0.2s ease-out; }
@keyframes scaleIn { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
.bg-talking { background-color: #fcf2f2 !important; }
.transition-all { transition: all 0.3s ease; }
</style>