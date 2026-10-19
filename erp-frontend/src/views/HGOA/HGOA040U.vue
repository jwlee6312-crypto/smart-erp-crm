<!--
	=============================================================
	프로그램명  : 설문유형별 질문 매핑 (설문조사 기능 활용)
    프로그램 ID	: HGOA040U
	작성일자	    : 25.03.06
	작성자      : AI Assistant
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

    <div class="hgo040-wrapper bg-light text-start p-2">
        <div class="d-flex justify-content-between align-items-center mb-1 bg-white p-1 px-3 rounded shadow-sm border border-secondary-subtle">
            <div class="fw-bold text-dark small">
                <i class="bi bi-diagram-3-fill text-primary me-2"></i>설문유형별 질문 구성 관리
            </div>
            <div class="btn-group shadow-sm">
                <button class="btn btn-xs btn-outline-secondary px-2" @click="initialize">초기화</button>
                <button class="btn btn-xs btn-dark px-3" @click="search_types">조회</button>
                <button class="btn btn-xs btn-primary px-3 fw-bold" @click="save"><i class="bi bi-save me-1"></i>저장</button>
                <button class="btn btn-xs btn-outline-danger px-2" @click="delete_mapping">전체삭제</button>
            </div>
        </div>

        <div class="row g-1 flex-grow-1 content-body overflow-hidden">
            <!-- [좌측] 설문유형 목록 -->
            <div class="col-md-3 h-100">
                <div class="card shadow-sm border-0 h-100 d-flex flex-column border-top border-3 border-dark">
                    <div class="card-header bg-white py-1 fw-bold small border-bottom">
                        <i class="bi bi-list-task me-1"></i>설문유형 (910)
                    </div>
                    <div class="card-body p-0 bg-white flex-grow-1 position-relative">
                        <div ref="type_list_ref" class="tabulator-custom" />
                    </div>
                </div>
            </div>

            <!-- [중앙] 설정된 질문 -->
            <div class="col-md-4 h-100">
                <div class="card shadow-sm border-0 h-100 d-flex flex-column border-top border-3 border-primary">
                    <div class="card-header bg-white py-1 fw-bold small border-bottom d-flex justify-content-between align-items-center">
                        <span class="text-truncate">
                            <i class="bi bi-check-circle-fill text-primary me-1"></i>
                            <span v-if="selected_surv_gb_nm" class="text-primary fw-bold">[{{ selected_surv_gb_nm }}] </span>설정 질문
                        </span>
                        <span class="badge bg-light text-dark border rounded-pill">{{ selected_count }}</span>
                    </div>
                    <div class="card-body p-0 bg-white flex-grow-1 position-relative">
                        <div ref="selected_table_ref" class="tabulator-custom" />
                        <div class="mapping-control-buttons shadow-sm border rounded bg-white">
                            <button class="btn btn-sm btn-outline-secondary mb-1" @click="remove_selected" title="선정 취소"><i class="bi bi-chevron-double-right"></i></button>
                            <button class="btn btn-sm btn-primary" @click="add_from_master" title="질문 추가"><i class="bi bi-chevron-double-left"></i></button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- [우측] 전체 질문 마스터 -->
            <div class="col-md-5 h-100">
                <div class="card shadow-sm border-0 h-100 d-flex flex-column border-top border-3 border-secondary text-start">
                    <div class="card-header bg-white py-1 fw-bold small border-bottom">
                        <i class="bi bi-database-fill-gear me-1"></i>전체 질문 마스터
                    </div>
                    <div class="card-body p-0 bg-white flex-grow-1 position-relative">
                        <div ref="total_table_ref" class="tabulator-custom" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { fetchCrmSelectData } from '@/composables/useFetchSelectData'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

const selected_surv_gb = ref('')
const selected_surv_gb_nm = ref('')
const selected_count = ref(0)

const type_list_ref = ref<HTMLDivElement | null>(null)
const selected_table_ref = ref<HTMLDivElement | null>(null)
const total_table_ref = ref<HTMLDivElement | null>(null)

let type_list_instance: Tabulator | null = null
let selected_table_instance: Tabulator | null = null
let total_table_instance: Tabulator | null = null

onMounted(() => {
	init_tables()
    search_types()
    search_all_questions()
})
onUnmounted(() => {
    type_list_instance?.destroy();
    selected_table_instance?.destroy();
    total_table_instance?.destroy();
})

function init_tables() {
    if (type_list_instance) type_list_instance.destroy();
	type_list_instance = new Tabulator(type_list_ref.value!, {
		layout: 'fitColumns', selectable: 1, height: '100%',
		columns: [
            { title: '코드', field: 'codecd', width: 80, hozAlign: 'center' },
            { title: '유형명', field: 'codenm', hozAlign: 'left' }
        ]
	})
	type_list_instance.on("rowClick", (e, row) => {
        const data = row.getData()
		selected_surv_gb.value = data.codecd
        selected_surv_gb_nm.value = data.codenm
		search_selected_questions(data.codecd)
	})

    const q_cols = [
        { formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center", headerSort: false, width: 40 },
        { title: "번호", field: "surv_no", hozAlign: "center", width: 80 },
        { title: "질문내용", field: "question", hozAlign: "left", formatter: "textarea" },
        { title: "유형", field: "ans_tp", hozAlign: "center", width: 70, formatter: (c) => c.getValue()==='010'?'객관식':'주관식' }
    ]

    if (selected_table_instance) selected_table_instance.destroy();
    selected_table_instance = new Tabulator(selected_table_ref.value!, {
        layout: 'fitColumns', height: '100%', selectable: true, movableRows: true,
        columns: [ { rowHandle:true, formatter:"handle", headerSort:false, frozen:true, width:30, minWidth:30 }, ...q_cols ]
    })
    selected_table_instance.on("dataChanged", () => { selected_count.value = selected_table_instance?.getData().length || 0 })

    if (total_table_instance) total_table_instance.destroy();
    total_table_instance = new Tabulator(total_table_ref.value!, {
        layout: 'fitColumns', height: '100%', selectable: true, columns: q_cols
    })
}

async function search_types() {
    try {
        const data = await fetchCrmSelectData('910')
        type_list_instance?.setData(data)
    } catch (e) { console.error('설문유형 조회 실패') }
}

async function search_all_questions() {
    try {
        const { data } = await api.get('/crm/outbound/surv/mst/search')
        total_table_instance?.setData(data)
    } catch (e) { console.error('질문마스터 조회 실패') }
}

async function search_selected_questions(surv_gb: string) {
    try {
        const { data } = await api.get('/crm/outbound/mapping/list', { params: { surv_gb: surv_gb } })
        selected_table_instance?.setData(data)
        selected_count.value = data.length
    } catch (e) { console.error('매핑목록 조회 실패') }
}

function add_from_master() {
    if (!selected_surv_gb.value) return vAlertError('설문유형을 먼저 선택하세요.')
    const selected = total_table_instance?.getSelectedData()
    if (!selected || selected.length === 0) return
    const current_data = selected_table_instance?.getData() || []
    selected.forEach(item => {
        if (!current_data.find(c => c.surv_no === item.surv_no)) {
            selected_table_instance?.addRow({ ...item, sortcd: String(current_data.length + 1).padStart(3, '0') })
        }
    })
    total_table_instance?.deselectRow()
}

function remove_selected() {
    const selected_rows = selected_table_instance?.getSelectedRows()
    if (!selected_rows || selected_rows.length === 0) return
    selected_rows.forEach(row => row.delete())
}

async function save() {
    if (!selected_surv_gb.value) return vAlertError('설문유형을 선택해 주세요.')
    try {
        const payload = {
            surv_gb: selected_surv_gb.value,
            questions: selected_table_instance?.getData()
        }
        await api.post('/crm/outbound/mapping/save', payload)
        vAlert('정상적으로 저장되었습니다.')
        search_selected_questions(selected_surv_gb.value)
    } catch (e) { vAlertError('저장 실패') }
}

async function delete_mapping() {
    if (!selected_surv_gb.value) return vAlertError('유형을 선택하세요.')
    if (!confirm('정말 삭제하시겠습니까?')) return
    try {
        await api.post('/crm/outbound/mapping/delete', { surv_gb: selected_surv_gb.value })
        vAlert('삭제되었습니다.')
        initialize()
        search_types()
    } catch (e) { vAlertError('삭제 실패') }
}

function initialize() {
    selected_surv_gb.value = '';
    selected_surv_gb_nm.value = '';
    selected_count.value = 0;
    type_list_instance?.deselectRow();
    selected_table_instance?.clearData();
}
</script>

<style scoped>
.hgo040-wrapper { height: calc(100vh - 110px); display: flex; flex-direction: column; overflow: hidden; }
.content-body { flex-grow: 1; overflow: hidden; }
.tabulator-custom { height: 100%; border: 1px solid #dee2e6; font-size: 0.8rem; border-radius: 4px; }
.mapping-control-buttons { position: absolute; right: 10px; top: 50%; transform: translateY(-50%); z-index: 100; display: flex; flex-direction: column; padding: 6px; gap: 8px; }
.btn-xs { padding: 2px 8px; font-size: 0.7rem; font-weight: bold; }
</style>
