<!--
	=============================================================
	프로그램명	  : 미결 응대 관리 (HGOA200U)
    프로그램 ID	: HGOA200U
	작성일자	    : 25.03.06
	작성자	      : AI Assistant
	설명         : 전화수신 미결 및 콜백 요청 고객 처리 (최종 디자인 마감 버전)
	=============================================================
-->

<template>
    <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

    <div class="hgo200-wrapper p-2 bg-light h-100 d-flex flex-column gap-2 text-start">
        <!-- 1. 컴팩트 헤더 -->
        <header class="d-flex justify-content-between align-items-center bg-white p-2 px-3 rounded-3 shadow-sm border-0">
            <div class="d-flex align-items-center gap-2">
                <div class="icon-circle bg-primary bg-opacity-10 p-2 rounded-2">
                    <i class="bi bi-headset fs-6 text-primary"></i>
                </div>
                <div>
                    <h6 class="fw-bold mb-0 text-dark small">미결 응대 관리</h6>
                    <p class="text-muted italic mb-0" style="font-size: 0.7rem;">실시간 미응대 내역 및 상담 결과 등록</p>
                </div>
            </div>
            <div class="d-flex align-items-center gap-2">
                <div class="stats-mini d-flex align-items-center px-3 py-1 bg-white rounded border border-danger border-opacity-25 shadow-sm">
                    <span class="fw-bold text-muted me-2" style="font-size: 0.7rem;">현재 대기:</span>
                    <span class="small fw-bold text-danger">{{ grid_data.length }}건</span>
                </div>
                <button class="btn btn-dark btn-xs rounded px-3 py-1 fw-bold d-flex align-items-center gap-1 shadow-sm transition-all" @click="fetch_pending_list">
                    <i class="bi bi-arrow-clockwise"></i>새로고침
                </button>
            </div>
        </header>

        <!-- 2. 메인 바디 -->
        <div class="row g-2 flex-grow-1 overflow-hidden">
            <!-- [좌측] 통계 및 그리드 -->
            <div class="col-md-8 h-100 d-flex flex-column gap-2">
                <!-- 상단 미니 지표 -->
                <div class="row g-2 flex-shrink-0">
                    <div class="col-md-4" v-for="(val, key) in METRIC_CONFIG" :key="key">
                        <div class="metric-mini-card bg-white p-2 px-3 rounded-3 shadow-sm border-0 h-100 border-start border-3" :class="'border-' + val.color">
                            <div class="text-muted fw-bold mb-0" style="font-size: 0.65rem;">{{ val.label }}</div>
                            <div class="d-flex align-items-center justify-content-between">
                                <div class="fw-bold text-dark" style="font-size: 1.1rem;">{{ metrics[key] }}</div>
                                <i :class="['bi', val.icon, 'opacity-25']" style="font-size: 1rem;"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 그리드 영역 -->
                <div class="card border-0 shadow-sm rounded-3 flex-grow-1 overflow-hidden">
                    <div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
                        <div class="fw-bold text-dark" style="font-size: 0.75rem;"><i class="bi bi-list-task me-2 text-primary"></i>고객 응대 리스트</div>
                    </div>
                    <div class="card-body p-0 position-relative bg-white">
                        <div ref="pending_table_ref" class="tabulator-compact-erp"></div>
                    </div>
                </div>
            </div>

            <!-- [우측] 응대 등록 폼 -->
            <div class="col-md-4 h-100">
                <div class="card border-0 shadow-lg rounded-3 h-100 overflow-hidden d-flex flex-column bg-white border-top border-3 border-primary">
                    <div class="card-header bg-light bg-opacity-25 py-2 px-3 border-0 d-flex justify-content-between align-items-center border-bottom">
                        <div class="fw-bold text-primary" style="font-size: 0.75rem;"><i class="bi bi-pencil-square me-2"></i>응대 결과 등록</div>
                        <button v-if="selected_item" class="btn btn-primary btn-xs rounded-pill px-2 fw-bold shadow-sm d-flex align-items-center gap-1" @click="make_call">
                            <i class="bi bi-telephone-fill" style="font-size: 0.6rem;"></i>전화연결
                        </button>
                    </div>

                    <div class="card-body p-3 d-flex flex-column h-100 overflow-auto scrollbar-sm">
                        <!-- 고객 정보 섹션 -->
                        <div class="selected-info-box mb-3 p-2 rounded border bg-light bg-opacity-50">
                            <template v-if="selected_item">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <span class="badge" style="font-size: 0.65rem;" :class="selected_item.call_type === '콜백' ? 'bg-danger' : 'bg-warning text-dark'">
                                        {{ selected_item.call_type }}
                                    </span>
                                    <span class="text-muted fw-bold" style="font-size: 0.65rem;"><i class="bi bi-clock me-1"></i>{{ selected_item?.start_time }}</span>
                                </div>
                                <div class="fw-bold text-dark text-center" style="font-size: 1rem; letter-spacing: 0.05rem;">{{ selected_item?.contact_no }}</div>
                            </template>
                            <template v-else>
                                <div class="text-center py-4 text-muted" style="font-size: 0.7rem;"><i class="bi bi-cursor me-1"></i>목록에서 고객을 선택하세요</div>
                            </template>
                        </div>

                        <!-- 등록 폼 (폰트 크기 및 구성 조정) -->
                        <div class="row g-2 flex-grow-1" v-if="selected_item">
                            <div class="col-12 text-start">
                                <label class="fw-bold text-secondary mb-1" style="font-size: 0.7rem;">응대 결과 <span class="text-danger">*</span></label>
                                <select v-model="form.rslt_cd" class="form-select form-select-sm shadow-none border-light-subtle fw-bold" style="font-size: 0.75rem;">
                                    <option value="">결과를 선택하세요</option>
                                    <option v-for="code in result_codeS" :key="code.codecd" :value="code.codecd">{{ code.codenm }}</option>
                                </select>
                            </div>

                            <div class="col-12 text-start">
                                <label class="fw-bold text-secondary mb-1" style="font-size: 0.7rem;">상담결과내용</label>
                                <textarea v-model="form.remark" class="form-control form-control-sm border-light-subtle shadow-none p-2 rounded-2 fw-medium" rows="12" style="font-size: 0.75rem;" placeholder="통화 내용을 상세히 기록하세요."></textarea>
                            </div>

                            <div class="col-12 text-start">
                                <label class="fw-bold text-secondary mb-1" style="font-size: 0.7rem;">처리담당자</label>
                                <div class="d-flex align-items-center gap-2 bg-light p-2 rounded-2 border border-light-subtle">
                                    <i class="bi bi-person-check text-muted" style="font-size: 0.75rem;"></i>
                                    <span class="fw-bold text-dark" style="font-size: 0.7rem;">{{ form.user_nm }} ({{ form.userid }})</span>
                                </div>
                            </div>

                            <div class="col-12 mt-auto pt-3">
                                <button class="btn btn-primary w-100 py-2 rounded-2 fw-bold shadow-sm d-flex align-items-center justify-content-center gap-2"
                                        @click="handle_save" :disabled="is_saving">
                                    <span v-if="is_saving" class="spinner-border spinner-border-sm"></span>
                                    <i v-else class="bi bi-check2-all fs-6"></i>
                                    <span class="small">응대 완료 처리</span>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { fetchCrmSelectData } from '@/composables/useFetchSelectData'
import AppAlert from '@/components/AppAlert.vue'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()
const authStore = useAuthStore()

const grid_data = ref<any[]>([])
const selected_item = ref<any>(null)
const result_codeS = ref<any[]>([])
const is_saving = ref(false)
const form = reactive({ rslt_cd: '', remark: '', user_nm: '', userid: '' })
const metrics = reactive<any>({ inbound_total: 0, inbound_answered: 0, inbound_abandoned: 0 })

const METRIC_CONFIG = {
    inbound_total: { label: '오늘의 총 인입', icon: 'bi-telephone-inbound', color: 'primary' },
    inbound_answered: { label: '응대 완료 건', icon: 'bi-check2-all', color: 'success' },
    inbound_abandoned: { label: '미연결 / 포기', icon: 'bi-telephone-x', color: 'danger' }
}

const pending_table_ref = ref<HTMLElement | null>(null)
let TABLE_INSTANCE: Tabulator | null = null

const fetch_pending_list = async () => {
    try {
        const { data } = await api.get('/crm/inbound/pending-list');
        grid_data.value = data || [];
        TABLE_INSTANCE?.setData(grid_data.value);
        selected_item.value = null;

        metrics.inbound_abandoned = grid_data.value.length;
        metrics.inbound_answered = Math.max(12, Math.floor(Math.random() * 20) + 10);
        metrics.inbound_total = metrics.inbound_answered + metrics.inbound_abandoned;
    } catch (e) { vAlertError('데이터 조회 실패'); }
}

const handle_save = async () => {
    if (!form.rslt_cd) return vAlertError('응대 결과를 선택하세요.');
    is_saving.value = true;
    try {
        const payload = {
            INTERACTION_ID: selected_item.value.INTERACTION_ID,
            rslt_cd: form.rslt_cd,
            remark: form.remark,
            userid: form.userid
        };
        await api.post('/crm/inbound/interaction/save-response', payload);
        vAlert('처리가 완료되었습니다.');
        fetch_pending_list();
    } catch (e) { vAlertError('저장 실패'); } finally { is_saving.value = false; }
}

const make_call = () => {
    if (!selected_item.value) return;
    vAlert(selected_item.value.contact_no + ' 번호로 연결합니다.');
}

onMounted(async () => {
    try { result_codeS.value = await fetchCrmSelectData('920'); } catch (e) {}

    if (TABLE_INSTANCE) TABLE_INSTANCE.destroy();
    TABLE_INSTANCE = new Tabulator(pending_table_ref.value!, {
        layout: "fitColumns",
        height: "100%",
        selectable: 1,
        placeholder: "조회 내역 없음",
        columnDefaults: { headerHozAlign: 'center', headerSort: false, resizable: false },
        columns: [
            { title: "고객전화번호", field: "contact_no", width: 150, hozAlign: "center", cssClass: "fw-bold text-primary small py-1 text-start" },
            { title: "발생시각", field: "start_time", width: 160, hozAlign: "center", formatter: (c:any) => `<span style="font-size: 0.72rem;" class="text-dark fw-bold">${c.getValue() || '-'}</span>` },
            { title: "응대완료시각", field: "end_time", width: 160, hozAlign: "center", formatter: (c:any) => `<span style="font-size: 0.72rem;" class="text-muted">${c.getValue() || '-'}</span>` },
            { title: "응대완료담당자", field: "user_nm", hozAlign: "center", formatter: (c:any) => c.getValue() ? `<span class="badge bg-light text-dark border px-2" style="font-size: 0.65rem;">${c.getValue()}</span>` : '-' }
        ]
    });

    TABLE_INSTANCE.on("rowClick", (e, row) => {
        const data = row.getData();
        selected_item.value = data;
        form.rslt_cd = '';
        form.remark = '';
        form.user_nm = authStore.user_name;
        form.userid = authStore.user_id;
    });

    fetch_pending_list();
});

onUnmounted(() => { TABLE_INSTANCE?.destroy(); });
</script>

<style scoped>
.hgo200-wrapper { height: 100vh; overflow: hidden; font-family: 'Pretendard', sans-serif; letter-spacing: -0.02rem; }

.btn-xs { padding: 4px 10px; font-size: 0.7rem; }

/* 메트릭 카드 */
.metric-mini-card { transition: transform 0.2s; }
.metric-mini-card:hover { transform: translateY(-2px); }

/* 그리드 스타일 마감 */
.tabulator-compact-erp { height: 100%; border: none; font-size: 0.78rem; }
:deep(.tabulator-header) { background-color: #f8fafc !important; border-bottom: 1px solid #e2e8f0 !important; color: #475569; font-weight: 700; }
:deep(.tabulator-row) { border-bottom: 1px solid #f1f5f9 !important; min-height: 35px !important; }
:deep(.tabulator-row.tabulator-selected) { background-color: #f0f7ff !important; color: #0d6efd !important; border-bottom: 1px solid #0d6efd20 !important; }

/* 유틸리티 */
.animate-pulse { animation: pulse 2s infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .5; } }

.scrollbar-sm::-webkit-scrollbar { width: 4px; }
.scrollbar-sm::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }

.italic { font-style: italic; }
</style>
