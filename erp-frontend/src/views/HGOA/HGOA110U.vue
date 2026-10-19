<!--
	=============================================================
	프로그램명	  : 콜백 통합 관리 (HGOA110U)
    프로그램 ID	: HGOA110U
	작성일자	    : 25.03.14
	작성자	      : AI Assistant
	설명         : 전수 녹취 기반 콜백 이력 조회 및 응대 처리 (PC용)
	=============================================================
-->

<template>
    <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

    <div class="erp-container d-flex flex-column h-100 bg-white">
        <!-- 🚀 1. 상단 액션 바 -->
        <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
            <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
                <i class="bi bi-telephone-outbound-fill me-2 text-primary" style="font-size: 18px;"></i>
                통신관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
                미결관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
                <span class="text-primary fw-bolder">콜백 통합 관리 (HGOA110U)</span>
            </div>
            <div class="btn-group-erp d-flex gap-1 pe-3">
                <button class="btn-erp btn-init" @click="initialize">초기화</button>
                <button class="btn-erp btn-search" @click="search">조회</button>
            </div>
        </div>

        <!-- 💡 2. 메인 컨텐츠 영역 -->
        <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">
            <!-- [상단] 조회 필터 -->
            <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
                <div class="card-body p-2 bg-white">
                    <div class="d-flex align-items-center flex-wrap gap-3 small">
                        <div class="d-flex align-items-center">
                            <span class="erp-label">조회기간</span>
                            <div class="d-flex align-items-center gap-1">
                                <input type="date" v-model="searchForm.fromdt" class="form-control form-control-sm" style="width: 130px;" />
                                <span class="px-1">~</span>
                                <input type="date" v-model="searchForm.todt" class="form-control form-control-sm" style="width: 130px;" />
                            </div>
                        </div>
                        <div class="d-flex align-items-center">
                            <span class="erp-label">전화번호</span>
                            <input v-model="searchForm.src_no" class="form-control form-control-sm" style="width: 150px;" placeholder="번호 입력" @keyup.enter="search" />
                        </div>
                    </div>
                </div>
            </div>

            <!-- [하단] 데이터 그리드 -->
            <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
                <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
                    <span class="fw-bold small text-dark"><i class="bi bi-list-stars me-1"></i> 콜백 요청 및 처리 내역</span>
                    <span class="badge bg-danger bg-opacity-10 text-danger border-0 rounded-pill px-2" style="font-size: 10px;">미결: {{ pendingCount }}건</span>
                </div>
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                    <div ref="tableRef" class="tabulator-instance flex-grow-1" />
                </div>
            </div>
        </div>

        <!-- 🎙️ 녹취 재생 모달 (숨김) -->
        <audio ref="audioPlayer" hidden></audio>

        <!-- ✅ 결과 등록 모달 추가 -->
        <teleport to="body">
            <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
                <div class="modal-content card shadow-lg border-0" style="width: 450px;">
                    <div class="card-header bg-success text-white fw-bold py-2 d-flex justify-content-between">
                        <span><i class="bi bi-pencil-square me-2"></i>콜백 응대 결과 등록</span>
                        <button type="button" class="btn-close btn-close-white" @click="showModal = false"></button>
                    </div>
                    <div class="card-body p-3">
                        <div class="mb-3">
                            <label class="form-label small fw-bold">응대 결과</label>
                            <select v-model="regForm.rslt_cd" class="form-select form-select-sm">
                                <option value="">-- 선택 --</option>
                                <option v-for="c in resultCodes" :key="c.codecd" :value="c.codecd">{{ c.codenm }}</option>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label class="form-label small fw-bold">상담 메모 (비고)</label>
                            <textarea v-model="regForm.remark" class="form-control form-control-sm" rows="5" placeholder="상담 내용을 입력하세요."></textarea>
                        </div>
                        <div class="d-flex justify-content-end gap-2 mt-3">
                            <button class="btn btn-sm btn-secondary px-3" @click="showModal = false">취소</button>
                            <button class="btn btn-sm btn-primary px-4 fw-bold" @click="saveResult">저장</button>
                        </div>
                    </div>
                </div>
            </div>
        </teleport>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()
const authStore = useAuthStore()

const searchForm = reactive({
    fromdt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    todt: new Date().toISOString().split('T')[0],
    src_no: ''
})

const tableRef = ref<HTMLDivElement | null>(null)
const audioPlayer = ref<HTMLAudioElement | null>(null)
let tableInstance: Tabulator | null = null
const gridData = ref<any[]>([])

const pendingCount = computed(() => gridData.value.filter(d => d.callback_status !== '030').length)

const initTable = () => {
    if (tableInstance) tableInstance.destroy();
    tableInstance = new Tabulator(tableRef.value!, {
        data: [],
        layout: "fitColumns",
        height: "100%",
        placeholder: "조회된 콜백 내역이 없습니다.",
        columnDefaults: { headerSort: false, headerHozAlign: 'center', vertAlign: 'middle' },
        columns: [
            { title: "통화일시", field: "start_time", width: 150, hozAlign: "center",
              formatter: (cell) => `<span class="small">${cell.getValue()?.replace('T', ' ')}</span>` },
            { title: "전화번호", field: "src_no", width: 120, cssClass: "fw-bold text-primary" },
            { title: "발신자명(회사)", field: "custnm", minWidth: 150, hozAlign: "left" },
            { title: "콜백번호", field: "callback_no", width: 120, cssClass: "text-danger fw-bold" },
            { title: "녹취(원본)", field: "rec_file", width: 100,
              formatter: (cell) => cell.getValue() ? `<button class="btn btn-xs btn-outline-dark py-0 px-2"><i class="bi bi-play-fill"></i></button>` : '-',
              cellClick: (e, cell) => { if(cell.getValue()) playAudio(cell.getValue()) }
            },
            { title: "담당자", field: "callback_agent_id", width: 100 },
            { title: "통화결과", field: "result_cd", width: 100,
              formatter: (cell) => {
                  const val = cell.getValue();
                  if (val === '200') return '<span class="badge bg-success">성공</span>';
                  if (val === '100') return '<span class="badge bg-warning text-dark">대기</span>';
                  return '<span class="badge bg-secondary">미결</span>';
              }
            },
            { title: "콜백녹취", field: "callback_rec_file", width: 100,
              formatter: (cell) => cell.getValue() ? `<button class="btn btn-xs btn-outline-primary py-0 px-2"><i class="bi bi-play-circle"></i></button>` : '-',
              cellClick: (e, cell) => { if(cell.getValue()) playAudio(cell.getValue()) }
            },
            { title: "비고", field: "call_memo", hozAlign: "left", editor: "input" },
            { title: "처리", width: 100,
              formatter: (cell) => `<div class="d-flex gap-1">
                                      <button class="btn btn-xs btn-primary py-0 px-2 btn-call">발신</button>
                                      <button class="btn btn-xs btn-success py-0 px-2 btn-reg">등록</button>
                                    </div>`,
              cellClick: (e, cell) => {
                  const target = e.target as HTMLElement;
                  if(target.classList.contains('btn-call')) makeCallback(cell.getData());
                  else if(target.classList.contains('btn-reg')) openResultModal(cell.getData());
              }
            }
        ],
    });
}

// --- 결과 등록 모달 제어 ---
const showModal = ref(false)
const regForm = reactive({ interaction_id: '', rslt_cd: '', remark: '' })
const resultCodes = ref<any[]>([])

async function openResultModal(data: any) {
    regForm.interaction_id = data.interaction_id;
    regForm.rslt_cd = data.result_cd || '';
    regForm.remark = data.call_memo || '';
    if (resultCodes.value.length === 0) {
        // 💡 920: 상담결과 코드
        const res = await api.post('/hs00/HS00_000S_STR', { gubun: 'GB', cmpycd: authStore.cmpycd, gbncd: '920' })
        resultCodes.value = res.data || []
    }
    showModal.value = true;
}

async function saveResult() {
    if (!regForm.rslt_cd) return vAlertError('결과 코드를 선택하세요.');
    try {
        await api.post('/crm/inbound/interaction/save-response', {
            INTERACTION_ID: regForm.interaction_id,
            rslt_cd: regForm.rslt_cd,
            remark: regForm.remark
        });
        vAlert('등록되었습니다.');
        showModal.value = false;
        search();
    } catch (e) { vAlertError('저장 실패'); }
}
// -----------------------

async function search() {
    try {
        const { data } = await api.get('/crm/inbound/callback-list', { params: searchForm });
        gridData.value = data || [];
        tableInstance?.setData(gridData.value);
        vAlert('조회되었습니다.');
    } catch (e) { vAlertError('조회 실패'); }
}

function playAudio(filename: string) {
    if (!audioPlayer.value) return;
    const url = `/crm/inbound/play-recording?file=${filename}`;
    audioPlayer.value.src = url;
    audioPlayer.value.play().catch(() => vAlertError('파일을 재생할 수 없습니다.'));
}

function makeCallback(data: any) {
    if (!confirm(`${data.callback_no} 번호로 콜백 전화를 거시겠습니까?`)) return;
    vAlert(`${data.callback_no} 번호로 연결을 시도합니다...`);
    // 💡 Asterisk 발신 API 연동 예정
}

function initialize() {
    Object.assign(searchForm, {
        fromdt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        todt: new Date().toISOString().split('T')[0],
        src_no: ''
    });
    gridData.value = [];
    tableInstance?.clearData();
}

onMounted(() => { nextTick(() => { initTable(); search(); }) })
onUnmounted(() => { tableInstance?.destroy(); })
</script>

<style scoped>
.erp-container { font-family: 'Pretendard', sans-serif; }
.erp-label { min-width: 70px; font-weight: 600; font-size: 13px; color: #444; }
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 12px; }
.btn-xs { font-size: 11px; padding: 2px 8px; }
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1050; display: flex; align-items: center; justify-content: center; }
</style>
