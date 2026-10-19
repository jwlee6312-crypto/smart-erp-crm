<!--
	=============================================================
	프로그램명	  : 캠페인 자료생성 (소문자 표준 적용)
    프로그램 ID	: HGOA050U
	작성일자	    : 25.03.06
	작성자	      : AI Assistant
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	
    <div class="hgo050-wrapper bg-light text-start p-2">
        <div class="row g-2 h-100 content-area">
            <!-- [좌측] 캠페인 목록 선택 -->
            <div class="col-md-3 h-100">
                <div class="card shadow-sm border-0 h-100 d-flex flex-column overflow-hidden border-top border-3 border-dark">
                    <div class="card-header bg-white py-1 fw-bold small d-flex justify-content-between align-items-center border-bottom">
                        <span><i class="bi bi-megaphone-fill me-1 text-primary"></i>대상 캠페인 선택</span>
                        <button class="btn btn-xs btn-outline-secondary" @click="load_campaigns"><i class="bi bi-arrow-clockwise"></i></button>
                    </div>
                    <div class="card-body p-0 bg-white flex-grow-1 position-relative">
                        <div ref="camp_list_ref" class="tabulator-custom" />
                    </div>
                </div>
            </div>

            <!-- [우측] 자료 생성 및 확인 -->
            <div class="col-md-9 h-100 d-flex flex-column">
                <ul class="nav nav-tabs custom-tabs bg-white border-bottom-0 ps-2 flex-shrink-0">
                    <li class="nav-item">
                        <button class="nav-link" :class="{active: active_tab === 'CHECK'}" @click="handle_tab_change('CHECK')">업로드 자료 확인</button>
                    </li>
                    <li class="nav-item">
                        <button class="nav-link" :class="{active: active_tab === 'CREATE'}" @click="handle_tab_change('CREATE')">새 자료 만들기(Excel)</button>
                    </li>
                </ul>

                <div class="tab-content flex-grow-1 d-flex flex-column bg-white shadow-sm border rounded-bottom overflow-hidden">
                    <div class="sub-toolbar d-flex justify-content-between align-items-center p-2 bg-light border-bottom">
                        <div class="d-flex gap-1 align-items-center">
                            <button class="btn btn-xs btn-outline-danger px-3 fw-bold" @click="handle_delete" :disabled="!selected_camp">선정삭제</button>
                            <div class="ms-3 d-flex align-items-center gap-2">
                                <span class="small fw-bold text-muted">선택 캠페인:</span>
                                <span class="badge bg-primary px-2" v-if="selected_camp">{{ selected_camp.camp_nm }}</span>
                                <span class="text-muted small" v-else>좌측에서 캠페인을 먼저 선택하세요.</span>
                            </div>
                        </div>
                        <div class="d-flex gap-2 align-items-center" v-if="active_tab === 'CHECK'">
                            <input type="text" v-model="search_keyword" class="form-control form-control-sm" style="width: 180px;" placeholder="고객명/연락처/이메일" @keyup.enter="search_target_list" />
                            <button class="btn btn-xs btn-dark px-3" @click="search_target_list">검색</button>
                        </div>
                    </div>

                    <!-- 메인 그리드 영역 -->
                    <div class="flex-grow-1 position-relative border-bottom bg-white">
                        <div ref="main_grid_ref" class="tabulator-custom" />
                    </div>

                    <!-- 하단 액션바 (업로드 도구) -->
                    <div class="footer-action bg-light p-2 d-flex justify-content-center align-items-center gap-3 flex-shrink-0">
                        <button type="button" class="btn btn-xs btn-outline-dark px-3 fw-bold shadow-sm" @click="download_template">
                            <i class="bi bi-file-earmark-arrow-down me-1"></i>표준 양식 다운로드
                        </button>

                        <div class="input-group input-group-sm w-auto border rounded bg-white shadow-sm overflow-hidden">
                            <input type="file" class="form-control border-0" id="campaignFile" @change="on_file_change" accept=".xlsx, .xls" style="width: 350px;" />
                        </div>
                        
                        <button class="btn btn-xs btn-primary px-4 fw-bold shadow-sm" :disabled="!selected_file || !selected_camp" @click="handle_upload">
                            <i class="bi bi-cloud-upload me-1"></i>선택 캠페인으로 업로드 시작
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
export default {
    name: 'HGOA050U'
}
</script>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import * as XLSX from 'xlsx'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import AppAlert from '@/components/AppAlert.vue'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

const active_tab = ref('CREATE')
const selected_camp = ref<any>(null)
const selected_file = ref<File | null>(null)
const search_keyword = ref('')

const camp_list_ref = ref<HTMLDivElement | null>(null)
const main_grid_ref = ref<HTMLDivElement | null>(null)
let camp_list_instance: Tabulator | null = null
let main_grid_instance: Tabulator | null = null

onMounted(() => {
    init_camp_list_grid();
    init_main_grid([]);
    load_campaigns();
})
onUnmounted(() => {
    camp_list_instance?.destroy();
    main_grid_instance?.destroy();
})

const init_camp_list_grid = () => {
    if (camp_list_instance) camp_list_instance.destroy();
    camp_list_instance = new Tabulator(camp_list_ref.value!, {
        placeholder: '데이터 없음', layout: 'fitColumns', height: '100%', selectable: 1,
        columns: [
            { title: "캠페인명", field: "camp_nm", hozAlign: "left" },
            { title: "번호", field: "camp_no", hozAlign: "center", width: 80 }
        ]
    })
    camp_list_instance.on("rowClick", (e, row) => {
        selected_camp.value = row.getData()
        if (active_tab.value === 'CHECK') search_target_list()
    })
}

const init_main_grid = (data: any[]) => {
    let columns: any[] = [
        { formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center", headerSort: false, width: 40 },
        { title: "고객명", field: "cust_nm", hozAlign: "center", width: 100 },
        { title: "연락처", field: "tel_no", hozAlign: "center", width: 120 },
        { title: "이메일", field: "email", hozAlign: "center", width: 180 },
        { title: "결과", field: "rslt_nm", hozAlign: "center", width: 100 },
        { title: "상태", field: "status", hozAlign: "center", width: 80 }
    ]

    if (data.length > 0 && data[0].ext_data) {
        let extDataSample = data[0].ext_data;
        // search_target_list에서 이미 파싱했지만, 혹시 모를 경우를 대비해 한번 더 체크
        if (typeof extDataSample === 'string') {
            try {
                extDataSample = JSON.parse(extDataSample);
            } catch (e) {
                extDataSample = null;
            }
        }

        if (extDataSample && typeof extDataSample === 'object') {
            Object.keys(extDataSample).forEach(key => {
                columns.push({
                    title: key,
                    field: `ext_data.${key}`,
                    hozAlign: "left",
                    width: 120
                });
            });
        }
    }

    if (main_grid_instance) main_grid_instance.destroy()
    main_grid_instance = new Tabulator(main_grid_ref.value!, {
        data: data,
        layout: "fitColumns",
        height: "100%",
        selectable: true,
        columns: columns,
        placeholder: "데이터가 없습니다."
    })
}

const load_campaigns = async () => {
    try {
        const { data } = await api.get('/crm/outbound/camp-list')
        camp_list_instance?.setData(data)
    } catch (e) {}
}

const handle_tab_change = (tab: string) => {
    active_tab.value = tab
    if (tab === 'CHECK' && selected_camp.value) search_target_list()
}

const search_target_list = async () => {
    if (!selected_camp.value) return
    try {
        const { data } = await api.get('/crm/outbound/call-list', {
            params: { camp_no: selected_camp.value.camp_no, keyword: search_keyword.value }
        })

        // 💡 데이터 정규화: 모든 키를 소문자로 변환 및 ext_data 파싱
        const processedData = data.map((item: any) => {
            const newItem: any = {};
            Object.keys(item).forEach(key => {
                newItem[key.toLowerCase()] = item[key];
            });

            // ext_data가 문자열(JSON)인 경우 객체로 변환
            if (newItem.ext_data && typeof newItem.ext_data === 'string') {
                try {
                    newItem.ext_data = JSON.parse(newItem.ext_data);
                } catch (e) {
                    newItem.ext_data = {};
                }
            }
            return newItem;
        });

        init_main_grid(processedData)
    } catch (e) {
        console.error(e)
        vAlertError('조회 실패')
    }
}

const download_template = () => {
    const headers = ['고객명', '연락처', '이메일', '거래처명', '가입상품', '비고']
    const testData = [['홍길동', '010-1111-2222', 'hong@test.com', '(주)하이온', '인터넷', '테스트']]
    const worksheet = XLSX.utils.aoa_to_sheet([headers, ...testData])
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, "TargetList")
    XLSX.writeFile(workbook, "캠페인_업로드_양식.xlsx")
}

const handle_upload = async () => {
    if (!selected_file.value || !selected_camp.value) return
    const reader = new FileReader()
    reader.onload = async (e: any) => {
        const workbook = XLSX.read(new Uint8Array(e.target.result), { type: 'array' })
        const json: any[] = XLSX.utils.sheet_to_json(workbook.Sheets[workbook.SheetNames[0]])
        const targetList = json.map(row => {
            const item: any = {
                cmpycd: selected_camp.value.cmpycd,
                camp_no: selected_camp.value.camp_no,
                surv_gb: selected_camp.value.surv_gb,
                cust_nm: row['고객명'],
                tel_no: row['연락처'],
                email: row['이메일'] || '',
                status: '010',
                ext_data: {}
            }
            Object.keys(row).forEach(key => {
                if (!['고객명', '연락처', '이메일'].includes(key)) {
                    item.ext_data[key] = row[key];
                }
            })
            return item
        })
        try {
            await api.post('/crm/outbound/call-list/save', targetList)
            vAlert('정상적으로 업로드되었습니다.');
            active_tab.value = 'CHECK'; search_target_list();
        } catch (e) { vAlertError('업로드 실패'); }
    }
    reader.readAsArrayBuffer(selected_file.value)
}

const handle_delete = async () => {
    const selectedData = main_grid_instance?.getSelectedData()
    if (!selectedData?.length) return vAlertError('삭제할 자료를 선택하세요.')
    if (!confirm('선택한 자료를 삭제하시겠습니까?')) return
    try {
        await api.post('/crm/outbound/call-list/delete', {
            camp_no: selected_camp.value.camp_no,
            targets: selectedData
        })
        vAlert('삭제되었습니다.'); search_target_list();
    } catch (e) { vAlertError('삭제 실패'); }
}

const on_file_change = (e: any) => { selected_file.value = e.target.files[0] }
</script>

<style scoped>
.hgo050-wrapper { height: calc(100vh - 110px); display: flex; flex-direction: column; overflow: hidden; }
.content-area { flex-grow: 1; min-height: 0; }
.custom-tabs .nav-link { font-size: 0.85rem; font-weight: bold; padding: 8px 25px; border: 1px solid #dee2e6; background: #f8f9fa; border-radius: 8px 8px 0 0; }
.custom-tabs .nav-link.active { color: #0d6efd; background: #fff; border-top: 3px solid #0d6efd; }
.tabulator-custom { height: 100%; border-top: 1px solid #dee2e6; font-size: 0.85rem; }
.footer-action { border-top: 2px solid #dee2e6; background: #f1f3f5 !important; }
.btn-xs { padding: 2px 10px; font-size: 0.75rem; font-weight: bold; }
</style>
