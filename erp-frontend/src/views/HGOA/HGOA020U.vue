<!--
	=============================================================
	프로그램명	  : 캠페인 등록 (소문자 표준 적용)
    프로그램 ID	: HGOA020U
	작성일자	    : 25.03.06
	작성자	      : AI Assistant
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hgoa020u-wrapper bg-light text-start p-2">
        <div class="d-flex justify-content-between align-items-center mb-2 bg-white p-2 rounded shadow-sm border border-secondary-subtle">
            <div class="fw-bold text-dark small"><i class="bi bi-gear-wide-connected me-2 text-primary"></i>캠페인 및 설문 구성 관리</div>
            <div class="btn-group shadow-sm">
                <button class="btn btn-xs btn-outline-secondary px-2" @click="initialize">초기화</button>
                <button class="btn btn-xs btn-dark px-3" @click="search">조회</button>
                <button class="btn btn-xs btn-outline-primary px-2" @click="handle_new">신규</button>
                <button class="btn btn-xs btn-primary px-3 fw-bold" @click="save"><i class="bi bi-save me-1"></i>저장</button>
                <button class="btn btn-xs btn-danger px-2" @click="delete_campaign">삭제</button>
            </div>
        </div>

        <div class="row g-2 content-body">
            <div class="col-md-3 h-100">
                <div class="card shadow-sm border-0 h-100 d-flex flex-column border-top border-3 border-dark">
                    <div class="card-header bg-white py-1 fw-bold small d-flex justify-content-between align-items-center border-bottom">
                        <span><i class="bi bi-list-task me-1"></i>캠페인 목록</span>
                        <span class="badge bg-secondary" style="font-size: 0.7rem;">{{ campaigns.length }}건</span>
                    </div>
                    <div class="card-body p-0 bg-white flex-grow-1 position-relative">
                        <div ref="list_ref" class="tabulator-custom-height" />
                    </div>
                </div>
            </div>

            <div class="col-md-9 h-100">
                <div class="d-flex flex-column h-100 gap-2">
                    <div class="card shadow-sm border-0 border-top border-3 border-primary flex-shrink-0">
                        <div class="card-header bg-white py-1 fw-bold small">
                            <i class="bi bi-pencil-square me-1 text-primary"></i>캠페인 상세 등록
                        </div>
                        <div class="card-body p-2 pt-0">
                            <table class="table table-sm table-bordered mb-0 small camp-form-table">
                                <colgroup>
                                    <col style="width: 12%" /><col style="width: 38%" />
                                    <col style="width: 12%" /><col style="width: 38%" />
                                </colgroup>
                                <tbody>
                                    <tr>
                                        <th class="bg-light text-end pe-2 text-primary fw-bold">설문유형</th>
                                        <td>
                                            <select v-model="form.surv_gb" class="form-select form-select-sm border-primary" @change="load_survey_preview">
                                                <option value="">선택하세요</option>
                                                <option v-for="code in code_910" :key="code.codecd" :value="code.codecd">{{ code.codenm }}</option>
                                            </select>
                                        </td>
                                        <th class="bg-light text-end pe-2">캠페인명</th>
                                        <td><input v-model="form.camp_nm" type="text" class="form-control form-control-sm" /></td>
                                    </tr>
                                    <tr>
                                        <th class="bg-light text-end pe-2">실행상태</th>
                                        <td>
                                            <select v-model="form.status" class="form-select form-select-sm">
                                                <option value="">선택</option>
                                                <option v-for="code in code_930" :key="code.codecd" :value="code.codecd">{{ code.codenm }}</option>
                                            </select>
                                        </td>
                                        <th class="bg-light text-end pe-2">실행일자</th>
                                        <td><input v-model="form.actdate" type="text" class="form-control form-control-sm" placeholder="yyyymmDD" /></td>
                                    </tr>
                                    <tr>
                                        <th class="bg-light text-end pe-2">비고</th>
                                        <td colspan="3"><input v-model="form.remark" type="text" class="form-control form-control-sm" /></td>
                                    </tr>
                                    <tr>
                                        <th class="bg-light text-end pe-2">시작인사</th>
                                        <td colspan="3"><textarea v-model="form.start_ment" class="form-control form-control-sm" rows="1"></textarea></td>
                                    </tr>
                                    <tr>
                                        <th class="bg-light text-end pe-2">종료인사</th>
                                        <td colspan="3"><textarea v-model="form.end_ment" class="form-control form-control-sm" rows="1"></textarea></td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div class="card shadow-sm border-0 flex-grow-1 overflow-hidden border-top border-3 border-secondary d-flex flex-column bg-white">
                        <div class="card-header bg-white py-1 fw-bold small d-flex justify-content-between align-items-center border-bottom">
                            <span><i class="bi bi-ui-checks me-1 text-secondary"></i>설문 문항 미리보기</span>
                        </div>
                        <div class="preview-body-scroll flex-grow-1 overflow-auto bg-white">
                            <div v-for="(q, idx) in survey_list" :key="q.surv_no" class="survey-item-row border-bottom d-flex align-items-center p-2 text-start">
                                <div class="col-1 text-center fw-bold text-secondary">{{ idx + 1 }}</div>
                                <div class="col-7 ps-3">
                                    <div class="fw-bold small text-dark">{{ q.question }}</div>
                                    <div class="text-muted" style="font-size: 0.7rem;">ID: {{ q.surv_no }}</div>
                                </div>
                                <div class="col-4 bg-light bg-opacity-25 rounded p-2 text-primary small italic">
                                    <i class="bi bi-reply-all-fill me-1"></i>{{ q.answers || (q.ans_tp === '020' ? '주관식 입력형' : '답변 요약 없음') }}
                                </div>
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
import { useAlerts } from '@/composables/useAlerts'
import { fetchCrmSelectData } from '@/composables/useFetchSelectData'
import { api } from '@/utils/axios'
import AppAlert from '@/components/AppAlert.vue'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

const form = reactive({ cmpycd: '', camp_no: '', camp_nm: '', surv_gb: '', remark: '', actdate: '', status: '010', start_ment: '', end_ment: '' })
const campaigns = ref<any[]>([]); const survey_list = ref<any[]>([]); const code_910 = ref<any[]>([]); const code_930 = ref<any[]>([])
const list_ref = ref<HTMLDivElement | null>(null); let list_instance: Tabulator | null = null

onMounted(async () => {
    try {
        const [res910, res930] = await Promise.all([
            fetchCrmSelectData('910'),
            fetchCrmSelectData('930')
        ])
        code_910.value = res910;
        code_930.value = res930;
    } catch (e) {
        console.error('코드 로드 실패:', e)
    }
    init_main_grid();
    search();
})

const init_main_grid = () => {
    if (!list_ref.value) return
    if (list_instance) list_instance.destroy();
    list_instance = new Tabulator(list_ref.value, {
        placeholder: '데이터 없음', layout: 'fitColumns', height: '100%', selectable: 1,
        columns: [
            { title: "캠페인명", field: "camp_nm", hozAlign: "left" },
            { title: "상태", field: "status", hozAlign: "center", width: 70 }
        ]
    })
    list_instance.on("rowClick", (e, row) => {
        Object.assign(form, row.getData());
        load_survey_preview();
    })
}

const load_survey_preview = async () => {
    if (!form.surv_gb) { survey_list.value = []; return; }
    try {
        const { data } = await api.get('/crm/outbound/camp-surv-mst-list', { params: { surv_gb: form.surv_gb } })
        console.log('load_survey_preview:', data)
        survey_list.value = data
    } catch (e) {
        console.error('미리보기 로드 실패:', e)
        survey_list.value = []
    }
}

async function search() {
    try {
        const { data } = await api.get('/crm/outbound/camp-list')
        campaigns.value = data;
        list_instance?.setData(data);
    } catch (error) {
        console.error('조회 실패:', error)
    }
}

function initialize() {
	Object.assign(form, { cmpycd: '', camp_no: '', camp_nm: '', surv_gb: '', remark: '', actdate: new Date().toISOString().slice(0,10).replace(/-/g,''), status: '010', start_ment: '', end_ment: '' })
	survey_list.value = [];
    list_instance?.deselectRow();
}

const handle_new = () => { initialize(); vAlert('신규 모드'); }

async function save() {
    if (!form.camp_nm || !form.surv_gb) return vAlertError('필수 항목을 입력하세요.');
    const url = form.camp_no ? '/crm/outbound/camp-modify' : '/crm/outbound/camp-save';
	try {
		await api.post(url, form)
		vAlert('저장되었습니다.');
        search();
	} catch (e) { vAlertError('저장 실패') }
}

async function delete_campaign() {
	if (!form.camp_no || !confirm('삭제하시겠습니까?')) return
	try {
		await api.post('/crm/outbound/camp-delete', { camp_no: form.camp_no })
		vAlert('삭제되었습니다.');
        initialize();
        search();
	} catch (e) { vAlertError('삭제 실패') }
}
onUnmounted(() => {
    if (list_instance) {
        list_instance.destroy();
        list_instance = null;
    }
})
</script>

<style scoped>
.hgoa020u-wrapper { height: calc(100vh - 110px); display: flex; flex-direction: column; overflow: hidden; }
.content-body { flex-grow: 1; overflow: hidden; }
.camp-form-table th { background-color: #f8f9fa; font-size: 0.8rem; text-align: right; padding: 4px 8px; font-weight: bold; border: 1px solid #dee2e6; }
.camp-form-table td { padding: 4px 8px; border: 1px solid #dee2e6; }
.tabulator-custom-height { height: 100%; border-top: 1px solid #dee2e6; }
.btn-xs { padding: 2px 8px; font-size: 0.75rem; }
.survey-item-row:hover { background-color: #f8faff; }
.italic { font-style: italic; }
</style>
