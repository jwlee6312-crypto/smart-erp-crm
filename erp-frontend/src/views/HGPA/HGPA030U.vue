<!--
	=============================================================
	프로그램명  : 다이얼플랜 관리 (Dialplan Management)
    프로그램 ID	: HGPA030U
	작성일자	    : 2025.03.14
	작성자      : AI Assistant
    설명        : 교환기 다이얼플랜 설정 및 업무 모드(수동) 전환 관리
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	
    <div class="erp-container d-flex flex-column h-100 bg-white">
        <!-- 🚀 1. 상단 액션 바 -->
        <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
            <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
                <i class="bi bi- Mortarboard-fill me-2 text-primary" style="font-size: 18px;"></i>
                시스템관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
                통신관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
                <span class="text-primary fw-bolder">다이얼플랜 관리 (HGPA030U)</span>
            </div>
            <div class="btn-group-erp d-flex gap-1 pe-3">
                <button class="btn-erp btn-init" @click="initialize">초기화</button>
                <button class="btn-erp btn-search" @click="search">조회</button>
                <button class="btn-erp btn-save" @click="save">저장</button>
            </div>
        </div>

        <!-- 💡 2. 메인 컨텐츠 영역 -->
        <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

            <!-- [상단] 업무 모드 수동 제어 (추가된 기능) -->
            <div class="card border-0 shadow-sm flex-shrink-0 overflow-hidden mb-1" style="border-left: 5px solid #0d6efd !important;">
                <div class="card-body p-3 bg-white d-flex align-items-center justify-content-between">
                    <div class="d-flex align-items-center gap-3">
                        <span class="fw-bold text-dark"><i class="bi bi-gear-wide-connected me-2 text-primary"></i>현재 ARS 운영 모드:</span>
                        <div class="btn-group" role="group">
                            <button class="btn btn-sm px-3 fw-bold" :class="currentMode === 'OPEN' ? 'btn-success' : 'btn-outline-secondary'" @click="updateMode('OPEN')">업무 중</button>
                            <button class="btn btn-sm px-3 fw-bold" :class="currentMode === 'CLOSE' ? 'btn-danger' : 'btn-outline-secondary'" @click="updateMode('CLOSE')">퇴근/야간</button>
                            <button class="btn btn-sm px-3 fw-bold" :class="currentMode === 'HOLIDAY' ? 'btn-warning' : 'btn-outline-secondary'" @click="updateMode('HOLIDAY')">휴일 모드</button>
                        </div>
                    </div>
                    <div class="text-muted small">
                        <i class="bi bi-info-circle me-1"></i> 버튼 클릭 시 999번 ARS 시나리오가 즉시 전환됩니다.
                    </div>
                </div>
            </div>

            <!-- [중단] 조회 필터 -->
            <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
                <div class="card-body p-2 bg-white">
                    <div class="d-flex align-items-center flex-wrap gap-3 small">
                        <div class="d-flex align-items-center">
                            <span class="erp-label"><i class="bi bi-dot"></i>컨텍스트</span>
                            <input v-model="searchForm.context" class="form-control form-control-sm" style="width: 150px;" placeholder="Context" @keyup.enter="search" />
                        </div>
                        <div class="d-flex align-items-center">
                            <span class="erp-label"><i class="bi bi-dot"></i>번호(Exten)</span>
                            <input v-model="searchForm.exten" class="form-control form-control-sm" style="width: 150px;" placeholder="Extension" @keyup.enter="search" />
                        </div>
                    </div>
                </div>
            </div>

            <!-- [하단] 레이아웃 영역 -->
            <div class="row g-1 flex-grow-1 overflow-hidden">
                <div class="col-md-9 h-100 d-flex flex-column">
                    <div class="card border shadow-sm h-100 d-flex flex-column overflow-hidden">
                        <div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
                            <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> Extensions List</span>
                            <button class="btn btn-xs btn-outline-primary fw-bold py-0" @click="addRow">행추가</button>
                        </div>
                        <div class="card-body p-0 flex-grow-1 bg-white position-relative overflow-hidden d-flex flex-column">
                            <div ref="tableRef" class="tabulator-instance flex-grow-1" />
                        </div>
                    </div>
                </div>

                <div class="col-md-3 h-100 overflow-auto">
                    <div class="card border shadow-sm h-100 overflow-hidden">
                        <div class="card-header bg-white py-1 px-3 border-bottom">
                            <span class="fw-bold small text-dark">Dialplan Guide</span>
                        </div>
                        <div class="card-body p-3 bg-white small text-start">
                            <div class="mb-3">
                                <p class="mb-1 text-primary fw-bold">부서 연결 (Queue)</p>
                                <code class="d-block p-2 bg-light border text-dark rounded">app: Queue, data: 8000,tT</code>
                                <p class="text-muted mt-1" style="font-size: 11px;">지정한 대기열로 연결합니다.</p>
                            </div>
                            <div class="mb-3">
                                <p class="mb-1 text-primary fw-bold">안내 재생 (Background)</p>
                                <code class="d-block p-2 bg-light border text-dark rounded">app: Background, data: custom/01_welcome</code>
                                <p class="text-muted mt-1" style="font-size: 11px;">메뉴 선택이 가능한 안내음을 재생합니다.</p>
                            </div>
                        </div>
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

const searchForm = reactive({ context: '', exten: '' })
const currentMode = ref('OPEN')
const tableRef = ref<HTMLDivElement | null>(null)
let tableInstance: Tabulator | null = null


const initTable = () => {
	if (!tableRef.value) return
    if (tableInstance) tableInstance.destroy();
	tableInstance = new Tabulator(tableRef.value, {
		placeholder: '데이터가 없습니다.', layout: 'fitColumns', selectable: true, height: '100%',
        columnDefaults: { headerSort: false, headerHozAlign: 'center', vertAlign: 'middle' },
		columns: [
			{ formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center", width: 40 },
			{ title: '컨텍스트', field: 'context', editor: 'input', width: 150, cssClass: 'fw-bold text-primary' },
			{ title: '번호 (Exten)', field: 'exten', editor: 'input', width: 120 },
			{ title: '순위', field: 'priority', editor: 'number', width: 80, hozAlign: 'center' },
			{ title: '명령어', field: 'app', editor: 'input', width: 150 },
			{ title: '파라미터', field: 'appdata', editor: 'input', hozAlign: 'left', minWidth: 200 },
		],
	})
}

async function search() {
	try {
		const { data } = await api.get('/crm/asterisk/extension/search', { params: searchForm })
        tableInstance?.setData(data || [])
        vAlert('조회되었습니다.')
        fetchCurrentMode()
	} catch (error) { vAlertError('조회 중 오류가 발생했습니다.') }
}

async function fetchCurrentMode() {
    try {
        const { data } = await api.get('/crm/asterisk/variable/search', { params: { var_name: 'BUSINESS_MODE' } });
        if (data && data.length > 0) currentMode.value = data[0].var_value;
    } catch (e) {}
}

async function updateMode(mode: string) {
    try {
        // 💡 [수정] 백엔드 API 경로 확인 및 데이터 형식 보정
        await api.post('/crm/asterisk/variable/save', [{ var_name: 'BUSINESS_MODE', var_value: mode }]);
        currentMode.value = mode;
        vAlert(`✅ 업무 모드가 [${mode === 'OPEN' ? '업무 중' : mode === 'CLOSE' ? '퇴근' : '휴일'}] 로 변경되었습니다.`);
    } catch (e) {
        console.error('모드 변경 실패:', e);
        vAlertError('모드 변경에 실패했습니다. 백엔드 연결을 확인하세요.');
    }
}

function addRow() {
    tableInstance?.addRow({ context: 'from-internal', exten: '', priority: 1, app: '', appdata: '' }, true)
}

async function save() {
    const data = tableInstance?.getData()
    if (!data || data.length === 0) return
	try {
		await api.post('/crm/asterisk/extension/save', data)
		vAlert('성공적으로 저장되었습니다.');
        search();
	} catch (e) { vAlertError('저장 중 오류가 발생했습니다.') }
}

onMounted(() => { nextTick(() => { initTable(); search(); }) })
onUnmounted(() => { if (tableInstance) tableInstance.destroy(); })
</script>

<style scoped>
.erp-container { font-family: 'Pretendard', sans-serif; letter-spacing: -0.02rem; }
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 12px; }
.erp-label { min-width: 80px; font-weight: 500; font-size: 13px; }
</style>
