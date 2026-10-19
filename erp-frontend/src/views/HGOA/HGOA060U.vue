<!--
	=============================================================
	프로그램명	  : 캠페인 속성(JSON) 매핑 관리 (소문자 표준 적용)
    프로그램 ID	: HGOA060U
	작성일자	    : 25.03.06
	작성자	      : AI Assistant
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hgo060-wrapper bg-light text-start p-2">
        <!-- 1. 상단 툴바 -->
        <div class="d-flex justify-content-between align-items-center mb-2 bg-white p-2 rounded shadow-sm border border-secondary-subtle">
            <div class="fw-bold text-dark small"><i class="bi bi-diagram-3-fill text-primary me-2"></i>캠페인 속성(JSON) 매핑 관리</div>
            <div class="btn-group shadow-sm">
                <button class="btn btn-xs btn-dark px-3" @click="search">조회</button>
                <button class="btn btn-xs btn-primary px-3 fw-bold" @click="save"><i class="bi bi-save me-1"></i>저장</button>
            </div>
        </div>

        <div class="row g-2 content-body">
            <!-- 2. 좌측: 설문유형 목록 -->
            <div class="col-md-3 h-100">
                <div class="card shadow-sm border-0 h-100 d-flex flex-column overflow-hidden border-top border-3 border-dark">
                    <div class="card-header bg-white py-1 fw-bold small d-flex justify-content-between align-items-center border-bottom">
                        <span><i class="bi bi-tags-fill me-1"></i>설문유형 (910)</span>
                    </div>
                    <div class="card-body p-0 bg-white flex-grow-1 position-relative">
                        <div ref="type_grid_ref" class="tabulator-full-height" />
                    </div>
                </div>
            </div>

            <!-- 3. 우측: 매핑 상세 설정 -->
            <div class="col-md-9 h-100">
                <div class="card shadow-sm border-0 h-100 d-flex flex-column overflow-hidden border-top border-3 border-primary">
                    <div class="card-header bg-white py-1 fw-bold small d-flex justify-content-between align-items-center border-bottom">
                        <span>
                            <i class="bi bi-list-check me-1"></i>
                            <span v-if="selected_surv_gb_nm" class="text-primary fw-bold">[{{ selected_surv_gb_nm }}] </span>
                            매핑 리스트 (엑셀 헤더 ↔ DB 표준키)
                        </span>
                        <button class="btn btn-xs btn-outline-primary" @click="add_row">+ 행추가</button>
                    </div>
                    <div class="card-body p-0 bg-white flex-grow-1 position-relative">
                        <div ref="mapping_grid_ref" class="tabulator-full-height" />
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
import { useAuthStore } from '@/stores/authStore'
import { fetchCrmSelectData } from '@/composables/useFetchSelectData'
import { api } from '@/utils/axios'
import AppAlert from '@/components/AppAlert.vue'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()
const authStore = useAuthStore()

const type_grid_ref = ref<HTMLDivElement | null>(null)
const mapping_grid_ref = ref<HTMLDivElement | null>(null)
let type_grid_instance: Tabulator | null = null
let mapping_grid_instance: Tabulator | null = null

const selected_surv_gb = ref('')
const selected_surv_gb_nm = ref('')

onMounted(async () => {
    init_grids()
    try {
        const codes = await fetchCrmSelectData('910')
        type_grid_instance?.setData(codes)
    } catch (e) { console.error('코드 로드 실패') }
})
onUnmounted(() => {
    type_grid_instance?.destroy();
    mapping_grid_instance?.destroy();
})

const init_grids = () => {
    if (type_grid_instance) type_grid_instance.destroy();
    type_grid_instance = new Tabulator(type_grid_ref.value!, {
        layout: "fitColumns",
        height: "100%",
        selectable: 1,
        placeholder: "데이터 없음",
        columns: [
            { title: "코드", field: "codecd", width: 80, hozAlign: "center" },
            { title: "유형명", field: "codenm", hozAlign: "left" }
        ],
    })

    type_grid_instance.on("rowClick", (e, row) => {
        const data = row.getData()
        selected_surv_gb.value = data.codecd
        selected_surv_gb_nm.value = data.codenm
        search()
    })

    if (mapping_grid_instance) mapping_grid_instance.destroy();
    mapping_grid_instance = new Tabulator(mapping_grid_ref.value!, {
        layout: "fitColumns",
        height: "100%",
        placeholder: "유형을 선택하세요",
        columns: [
            { title: "엑셀 헤더명 (한글)", field: "attr_nm", editor: "input", headerSort: false },
            { title: "DB 저장 키 (영문 표준)", field: "attr_key", editor: "input", headerSort: false },
            {
                title: "데이터 타입", field: "data_type", editor: "list", width: 200,
                editorParams: { values: ["STRING", "NUMBER", "DATE"] }
            }
            /* 💡 통계 및 삭제 기능은 필요 시 활성화 (현재는 데이터 수집만 진행)
            , {
                title: "통계활용", field: "stats_yn", width: 100, hozAlign: "center", headerSort: false,
                formatter: (cell) => cell.getValue() === 'Y' ? '<i class="bi bi-check-square-fill text-primary fs-5"></i>' : '<i class="bi bi-square text-muted fs-5"></i>',
                cellClick: (e, cell) => {
                    const newVal = cell.getValue() === 'Y' ? 'N' : 'Y';
                    cell.setValue(newVal);
                }
            },
            {
                title: "삭제", width: 60, hozAlign: "center", headerSort: false,
                formatter: () => '<i class="bi bi-trash3 text-danger cursor-pointer fs-6"></i>',
                cellClick: (e, cell) => {
                    if(confirm('이 매핑 항목을 삭제하시겠습니까?')) cell.getRow().delete()
                }
            }
            */
        ]
    })
}

const search = async () => {
    if (!selected_surv_gb.value) return
    try {
        const { data } = await api.get('/crm/outbound/attr-mapper/list', {
            params: { surv_gb: selected_surv_gb.value }
        })
        mapping_grid_instance?.setData(data)
    } catch (e) { vAlertError('조회 실패') }
}

const add_row = () => {
    if (!selected_surv_gb.value) return vAlertError('먼저 설문유형을 선택하세요.')
    mapping_grid_instance?.addRow({
        surv_gb: selected_surv_gb.value,
        data_type: 'STRING',
        stats_yn: 'Y'
    })
}

const save = async () => {
    if (!selected_surv_gb.value) return vAlertError('유형이 선택되지 않았습니다.')
    const data = mapping_grid_instance?.getData()

    const invalid = data?.some(item => !item.attr_nm || !item.attr_key)
    if (invalid) return vAlertError('헤더명과 DB 키는 필수 입력입니다.')

    try {
        await api.post('/crm/outbound/attr-mapper/save', {
            surv_gb: selected_surv_gb.value,
            list: data
        })
        vAlert('정상적으로 저장되었습니다.')
        search()
    } catch (e) { vAlertError('저장 중 오류가 발생했습니다.') }
}

function initialize() {
    selected_surv_gb.value = '';
    selected_surv_gb_nm.value = '';
    type_grid_instance?.deselectRow();
    mapping_grid_instance?.clearData();
}
</script>

<style scoped>
.hgo060-wrapper { height: calc(100vh - 110px); display: flex; flex-direction: column; overflow: hidden; }
.content-body { flex-grow: 1; overflow: hidden; min-height: 0; }
.tabulator-full-height { height: 100%; border-top: 1px solid #dee2e6; font-size: 0.8rem; }
.btn-xs { padding: 1px 8px; font-size: 0.75rem; font-weight: bold; }
</style>
