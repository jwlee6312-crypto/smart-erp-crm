<!--
	=============================================================
	프로그램명  : 수신그룹 관리 (Queue Management)
    프로그램 ID	: HGPA020U
	작성일자	    : 2025.03.14
	작성자      : AI Assistant
    설명        : 수신 그룹(Queue) 및 소속 상담원 관리 (표준 UI 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	
    <!-- 수신그룹 상세 정책 설정 모달 -->
    <teleport to="body">
        <div v-if="showQueueModal" class="modal-overlay" @click.self="showQueueModal = false">
            <div class="modal-content card shadow-lg border-0" style="width: 550px;">
                <div class="card-header bg-primary text-white fw-bold py-2 d-flex justify-content-between">
                    <span><i class="bi bi-patch-question-fill me-2"></i>수신그룹 정책 설정 (Queue Policy)</span>
                    <button type="button" class="btn-close btn-close-white" @click="showQueueModal = false"></button>
                </div>
                <div class="card-body p-3 text-start">
                    <table class="table table-sm table-bordered mb-2 small form-table">
                        <colgroup><col style="width: 30%" /><col style="width: 70%" /></colgroup>
                        <tbody>
                            <tr>
                                <th class="bg-light fw-bold text-end pe-2">그룹명(ID)</th>
                                <td><input v-model="tempQueue.name" class="form-control form-control-sm fw-bold" placeholder="예: 8000" /></td>
                            </tr>
                            <tr>
                                <th class="bg-light fw-bold text-end pe-2">전략 (Strategy)</th>
                                <td>
                                    <select v-model="tempQueue.strategy" class="form-select form-select-sm border-primary fw-bold text-primary">
                                        <option value="ringall">전체벨(ringall)</option>
                                        <option value="linear">순차벨(linear)</option>
                                        <option value="leastrecent">최근업무무관(leastrecent)</option>
                                        <option value="fewestcalls">최소통화자(fewestcalls)</option>
                                        <option value="random">무작위(random)</option>
                                        <option value="roundrobin">순환벨(roundrobin)</option>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2" class="p-3 border-0" style="background-color: #fff9db;">
                                    <div class="fw-bold mb-1" style="color: #856404; font-size: 0.8rem;"><i class="bi bi-lightbulb-fill me-1"></i> 전략 동작 가이드:</div>
                                    <div class="fw-bold" style="color: #533f03; line-height: 1.5; font-size: 0.85rem;">{{ strategyGuides[tempQueue.strategy] }}</div>
                                </td>
                            </tr>
                            <tr>
                                <th class="bg-light fw-bold text-end pe-2">타임아웃(초)</th>
                                <td>
                                    <div class="d-flex align-items-center gap-2">
                                        <input v-model.number="tempQueue.timeout" type="number" class="form-control form-control-sm w-25 fw-bold" />
                                        <span class="text-danger fw-bold" style="font-size: 0.7rem;">(벨 울림 제한 시간)</span>
                                    </div>
                                </td>
                            </tr>
                            <tr><th class="bg-light fw-bold text-end pe-2">컨텍스트 (Context)</th><td><input v-model="tempQueue.context" class="form-control form-control-sm" /></td></tr>
                            <tr><th class="bg-light fw-bold text-end pe-2">대기음 (MOH)</th><td><input v-model="tempQueue.musiconhold" class="form-control form-control-sm" /></td></tr>
                            <tr>
                                <th class="bg-light fw-bold text-end pe-2 text-primary">당직자번호(Failover)</th>
                                <td>
                                    <input v-model="tempQueue.failover_dest" class="form-control form-control-sm border-primary" placeholder="예: 01012345678" />
                                    <div class="small text-muted mt-1" style="font-size: 0.65rem;">(상담원 전원 부재/미응답 시 연결될 번호)</div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <div class="d-flex justify-content-end gap-2 mt-3">
                        <button class="btn btn-sm btn-secondary px-3" @click="showQueueModal = false">닫기</button>
                        <button class="btn btn-sm btn-primary px-4 fw-bold shadow-sm" @click="confirmQueueAdd">정책 적용</button>
                    </div>
                </div>
            </div>
        </div>
    </teleport>

    <div class="erp-container d-flex flex-column h-100 bg-white">
        <!-- 🚀 1. 상단 액션 바 -->
        <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
            <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
                <i class="bi bi-diagram-3-fill me-2 text-primary" style="font-size: 18px;"></i>
                시스템관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
                통신관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
                <span class="text-primary fw-bolder">수신그룹 관리 (HGPA020U)</span>
            </div>
            <div class="btn-group-erp d-flex gap-1 pe-3">
                <button class="btn-erp btn-init" @click="initialize">초기화</button>
                <button class="btn-erp btn-search" @click="searchQueues">조회</button>
                <button class="btn-erp btn-save" @click="saveAll">리얼타임 적용</button>
            </div>
        </div>

        <div class="row g-1 flex-grow-1 overflow-hidden p-2 bg-light main-content-wrapper">
            <!-- ⬅️ 2. 좌측: 수신그룹 목록 -->
            <div class="col-md-2 h-100 d-flex flex-column">
                <div class="card border shadow-sm h-100 overflow-hidden d-flex flex-column">
                    <div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
                        <span class="fw-bold small text-dark">Queue List</span>
                        <button class="btn btn-xs btn-outline-primary fw-bold py-0" @click="openQueueAddModal">추가</button>
                    </div>
                    <div class="card-body p-0 flex-grow-1 bg-white position-relative overflow-hidden d-flex flex-column">
                        <div ref="queueTableRef" class="tabulator-instance flex-grow-1" />
                    </div>
                </div>
            </div>

            <!-- 🔄 3. 중앙: 소속 상담원 -->
            <div class="col h-100 d-flex flex-column">
                <div class="card border shadow-sm h-100 overflow-hidden d-flex flex-column">
                    <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
                        <span class="fw-bold small text-dark">Assigned Members</span>
                    </div>
                    <div class="card-body p-0 flex-grow-1 bg-white position-relative overflow-hidden d-flex flex-column">
                        <div ref="memberTableRef" class="tabulator-instance flex-grow-1" />
                    </div>
                </div>
            </div>

            <!-- ⬅️➡️ 4. 이동 버튼 -->
            <div class="col-auto d-flex flex-column justify-content-center px-1 gap-2">
                <button class="btn btn-primary shadow-sm mini-move-btn" @click="moveRightToLeft" title="소속 추가"><i class="bi bi-chevron-left"></i></button>
                <button class="btn btn-outline-secondary shadow-sm mini-move-btn" @click="moveLeftToRight" title="소속 제거"><i class="bi bi-chevron-right"></i></button>
            </div>

            <!-- ➡️ 5. 우측: 전체 내선 명단 -->
            <div class="col h-100 d-flex flex-column">
                <div class="card border shadow-sm h-100 overflow-hidden d-flex flex-column">
                    <div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
                        <div class="d-flex align-items-center gap-2">
                            <span class="fw-bold small text-dark">Available Endpoints</span>
                            <input type="text" v-model="searchAgentName" @input="filterAgents" class="form-control form-control-sm py-0 ms-2" placeholder="검색" style="width: 120px; height: 24px;">
                        </div>
                    </div>
                    <div class="card-body p-0 flex-grow-1 bg-white position-relative overflow-hidden d-flex flex-column">
                        <div ref="agentTableRef" class="tabulator-instance flex-grow-1" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

const selectedQueueName = ref<string | null>(null)
const searchAgentName = ref('')
const showQueueModal = ref(false)

const strategyGuides: Record<string, string> = {
    ringall: "모든 상담원의 전화가 동시에 울립니다. 가장 먼저 받는 상담원에게 연결됩니다.",
    linear: "등록된 상담원 순서대로 벨이 울립니다. 대기 행렬을 위해 순번대로 통화를 배분합니다.",
    leastrecent: "가장 오랫동안 통화를 받지 않은 유휴 상담원에게 우선적으로 벨이 울립니다.",
    fewestcalls: "오늘 하루 통화 건수가 가장 적은 상담원에게 통화를 우선 배분합니다.",
    random: "상담원 순서와 상관없이 무작위로 벨이 울립니다.",
    roundrobin: "상담원들을 돌아가면서 균등하게 벨을 울립니다."
}

const tempQueue = reactive({ name: '', strategy: 'ringall', timeout: 15, context: 'from-internal', musiconhold: 'default', failover_dest: '' })

const queueTableRef = ref<HTMLDivElement | null>(null)
const memberTableRef = ref<HTMLDivElement | null>(null)
const agentTableRef = ref<HTMLDivElement | null>(null)

let queueTableInstance: Tabulator | null = null
let memberTableInstance: Tabulator | null = null
let agentTableInstance: Tabulator | null = null


const initTables = () => {
    if (queueTableInstance) queueTableInstance.destroy();
	queueTableInstance = new Tabulator(queueTableRef.value!, {
		placeholder: '데이터 없음', layout: 'fitColumns', selectable: 1, height: '100%',
        columnDefaults: { headerSort: false, headerHozAlign: 'center', vertAlign: 'middle' },
		columns: [{ title: '그룹명(Name)', field: 'name', hozAlign: 'left', editor: 'input', cssClass: 'fw-bold text-primary' }]
	})
	queueTableInstance.on("rowClick", (e, row) => {
        const data = row.getData();
		selectedQueueName.value = data.name;
		searchMembers(data.name);
	})

    if (memberTableInstance) memberTableInstance.destroy();
	memberTableInstance = new Tabulator(memberTableRef.value!, {
		placeholder: '소속 상담원이 없습니다.', layout: 'fitColumns', selectable: true, height: '100%',
        columnDefaults: { headerSort: false, headerHozAlign: 'center', hozAlign: 'center', vertAlign: 'middle' },
		columns: [
			{ formatter: "rowSelection", titleFormatter: "rowSelection", width: 40 },
			{ title: '상담원명', field: 'membername', editor: 'input', hozAlign: 'left' },
			{ title: '내선', field: 'interface_no', hozAlign: 'center' },
			{ title: '우선순위', field: 'penalty', editor: 'number', width: 80 },
            { title: '후처리시간', field: 'wrapuptime', editor: 'number', width: 80 },
			{ title: '상태', field: 'paused', editor: 'list', width: 80,
                editorParams: { values: { "0": "정상", "1": "중지" } },
                formatter: (cell) => (cell.getValue() || '0') == "1" ? "<span class='text-danger fw-bold'>중지</span>" : "<span class='text-success fw-bold'>정상</span>"
            }
		],
	})

    if (agentTableInstance) agentTableInstance.destroy();
	agentTableInstance = new Tabulator(agentTableRef.value!, {
		placeholder: '내선 정보가 없습니다.', layout: 'fitColumns', selectable: true, height: '100%',
        columnDefaults: { headerSort: false, headerHozAlign: 'center', hozAlign: 'center', vertAlign: 'middle' },
		columns: [
            { formatter: "rowSelection", titleFormatter: "rowSelection", width: 40 },
			{ title: '내선번호', field: 'id', hozAlign: 'center', cssClass: 'fw-bold' },
			{ title: '상담원명', field: 'callerid', hozAlign: 'left' }
		],
	})
}

const openQueueAddModal = () => { Object.assign(tempQueue, { name: '', strategy: 'ringall', timeout: 15, context: 'from-internal', musiconhold: 'default', failover_dest: '' }); showQueueModal.value = true; }
const confirmQueueAdd = () => { if(!tempQueue.name) return vAlertError('그룹명을 입력하세요.'); queueTableInstance?.addRow({ ...tempQueue }, true); showQueueModal.value = false; }

async function searchQueues() {
    try { 
        const { data } = await api.get('/crm/asterisk/queue/search');
        queueTableInstance?.setData(data || []);
        vAlert('수신그룹 목록을 조회했습니다.')
    } catch (e) { vAlertError('조회 중 오류가 발생했습니다.') }
}

async function searchAllEndpoints() {
    try { 
        const { data } = await api.get('/crm/asterisk/pjsip/search');
        agentTableInstance?.setData(data || []);
    } catch (e) {} 
}

async function searchMembers(name: string) {
    try { 
        const { data } = await api.get('/crm/asterisk/queue/member/search', { params: { queue_name: name } });
        memberTableInstance?.setData(data || []);
    } catch (e) {} 
}

function moveRightToLeft() {
    if (!selectedQueueName.value) return vAlertError('그룹을 먼저 선택하세요.');
    const agents = agentTableInstance?.getSelectedData();
    if (!agents || agents.length === 0) return vAlertError('추가할 내선을 선택하세요.');
    
    const current = memberTableInstance?.getData() || [];
    agents.forEach(a => { 
        if (!current.find(m => m.interface_no === a.id)) {
            memberTableInstance?.addRow({
                queue_name: selectedQueueName.value,
                membername: a.callerid || '상담원',
                interface_no: a.id,
                penalty: 0, paused: "0", wrapuptime: 0
            }); 
        }
    });
    agentTableInstance?.deselectRow();
}

function moveLeftToRight() {
    const selectedRows = memberTableInstance?.getSelectedRows();
    if (!selectedRows || selectedRows.length === 0) return vAlertError('제거할 상담원을 선택하세요.');
    if (!confirm('소속 상담원에서 제외하시겠습니까?')) return
    selectedRows.forEach(row => row.delete());
}

async function saveAll() {
    const queueData = queueTableInstance?.getData();
    if (!queueData || queueData.length === 0) return vAlertError('저장할 그룹 정보가 없습니다.');
    if (!confirm('리얼타임 시스템에 적용하시겠습니까?')) return

	try {
        const resQ = await api.post('/crm/asterisk/queue/save', queueData);
        if (resQ.data?.[0]?.result === 'N') return vAlertError(resQ.data[0].msg || '그룹 저장 실패');

		if (selectedQueueName.value) {
            const resM = await api.post('/crm/asterisk/queue/member/save', {
                queue_name: selectedQueueName.value,
                members: memberTableInstance?.getData()
            });
            if (resM.data?.[0]?.result === 'N') return vAlertError(resM.data[0].msg || '멤버 저장 실패');
        }
		vAlert('성공적으로 적용되었습니다.');
        searchQueues();
	} catch (e) { vAlertError('저장 중 오류가 발생했습니다.') }
}

function initialize() { selectedQueueName.value = null; memberTableInstance?.clearData(); queueTableInstance?.deselectRow(); vAlert('초기화되었습니다.'); }

function filterAgents() {
    agentTableInstance?.setFilter([
        [ {field:"id", type:"like", value:searchAgentName.value}, {field:"callerid", type:"like", value:searchAgentName.value} ]
    ]);
}

onMounted(() => {
	nextTick(() => {
        initTables();
        searchQueues();
        searchAllEndpoints();
    })
})

onUnmounted(() => {
    queueTableInstance?.destroy();
    memberTableInstance?.destroy();
    agentTableInstance?.destroy();
})
</script>

<style scoped>
.erp-container { font-family: 'Pretendard', sans-serif; letter-spacing: -0.02rem; }
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 12px; }
.mini-move-btn { width: 34px; height: 34px; display: flex; align-items: center; justify-content: center; border-radius: 8px; }
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1050; display: flex; align-items: center; justify-content: center; }
.form-table th { background-color: #f8f9fa; font-size: 0.85rem; text-align: right; padding: 5px 10px; font-weight: bold; }
</style>
