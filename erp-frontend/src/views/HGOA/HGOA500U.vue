<!--
	=============================================================
	프로그램명	: 맞춤형 설문 동적 검색 및 쿼리 관리 (HGOA500U)
	작성일자	: 2026.10.26
	작성자      : AI Assistant
	설명        : 저장된 쿼리 콤보박스 2배 확장 및 우측 설명(query_desc) 표시, 최상단 쿼리삭제 버튼, 조건 행 높이 25% 축소 적용 화면
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 상단 글로벌 액션 바 (HSOD100U 표준) -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-search-heart-fill me-2 text-primary" style="font-size: 18px;"></i>
        아웃바운드 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        캠페인관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">맞춤형 설문 동적 검색 및 쿼리 관리 (HGOA500U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="executeQuery">조회</button>
        <button class="btn-erp btn-save" @click="openSaveQueryModal">쿼리저장</button>
        <button class="btn-erp btn-print" @click="exportExcel"><i class="bi bi-file-earmark-excel-fill me-1"></i>엑셀다운</button>
      </div>
    </div>

    <!-- 💡 상단 쿼리 템플릿 로드 바 (콤보박스 2배 확장, 우측 설명 표시, 최상단 쿼리삭제 버튼) -->
    <div class="p-2 bg-light border-bottom flex-shrink-0">
      <div class="d-flex align-items-center justify-content-between bg-white p-2 border rounded shadow-sm">
        <div class="d-flex align-items-center gap-3 flex-grow-1">
          <div class="d-flex align-items-center gap-2">
            <span class="small fw-bold text-secondary"><i class="bi bi-bookmark-star-fill text-warning me-1"></i>저장된 맞춤 쿼리:</span>
            <select v-model="selectedQueryid" @change="loadSelectedQuery" class="form-select form-select-sm shadow-none" style="width: 500px;">
              <option value="">-- 맞춤 쿼리 선택 --</option>
              <option v-for="q in savedQueryList" :key="q.queryid" :value="q.queryid">{{ q.querynm }}</option>
            </select>
          </div>
          <!-- 선택된 쿼리의 설명(querydesc)을 우측에 표시 -->
          <div class="flex-grow-1 text-muted small fst-italic text-truncate" v-if="selectedQuerydesc">
            <i class="bi bi-info-circle me-1 text-primary"></i>설명: {{ selectedQuerydesc }}
          </div>
        </div>
        <button class="btn btn-sm btn-outline-danger py-1 px-3 fw-bold shadow-xs flex-shrink-0" @click="deleteSelectedQuery" v-if="selectedQueryid" style="font-size: 11px;">
          <i class="bi bi-trash me-1"></i>쿼리삭제
        </button>
      </div>
    </div>

    <!-- 💡 좌우 분할 검색 조건 설정 영역 (좌측: 캠페인 선택 / 우측: 복수 조건 필터 및 연산자) -->
    <div class="p-2 bg-light border-bottom flex-shrink-0">
      <div class="d-flex flex-row gap-2">
        <!-- 좌측: 캠페인 선택 카드 -->
        <div class="card border shadow-sm" style="width: 35%;">
          <div class="card-header bg-white py-2 px-3 border-bottom">
            <span class="small fw-bold text-primary"><i class="bi bi-bullseye me-1"></i> 캠페인 선택</span>
          </div>
          <div class="card-body p-3 bg-white d-flex flex-column gap-2">
            <label class="form-label small fw-bold text-secondary mb-1">대상 캠페인</label>
            <select v-model="filter.campno" @change="onCampChange" class="form-select form-select-sm shadow-none border-primary-subtle fw-bold">
              <option value="">-- 전체 캠페인 --</option>
              <option v-for="c in campaignList" :key="c.camp_no" :value="c.camp_no">{{ c.camp_nm }}</option>
            </select>
            <div class="text-muted x-small mt-2">
              <i class="bi bi-info-circle me-1"></i>캠페인을 선택하면 해당 캠페인의 설문 문항만 자동 연동됩니다.
            </div>
          </div>
        </div>

        <!-- 우측: 복수 조건 필터 및 AND/OR 연산자 설정 카드 (조건 행 높이 25% 축소) -->
        <div class="card border shadow-sm flex-grow-1" style="width: 65%;">
          <div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
            <span class="small fw-bold text-primary"><i class="bi bi-funnel-fill me-1"></i> 복수 조건 필터 및 AND/OR 연산자 설정</span>
            <button class="btn btn-sm btn-primary py-1 px-3 fw-bold rounded-pill shadow-xs" @click="addConditionRow" style="font-size: 11px;"><i class="bi bi-plus-lg me-1"></i>조건 추가</button>
          </div>
          <div class="card-body p-2 bg-white d-flex flex-column gap-1 overflow-auto" style="max-height: 190px;">
            <div v-for="(cond, index) in filter.conditions" :key="index" class="border rounded px-2 py-1 bg-light bg-opacity-25 d-flex align-items-center gap-2 shadow-xs">
              <div v-if="index > 0" style="width: 70px;">
                <select v-model="cond.operator" class="form-select form-select-sm fw-bold text-primary shadow-none bg-white py-0" style="height: 28px; font-size: 11px;">
                  <option value="AND">AND</option>
                  <option value="OR">OR</option>
                </select>
              </div>
              <div v-else class="text-center small fw-bold text-muted bg-white border rounded px-2 py-0 shadow-xs" style="width: 100px; font-size: 11px; height: 28px; display: flex; align-items: center; justify-content: center;">
                <i class="bi bi-pin-angle-fill me-1 text-primary"></i>조건 #1
              </div>

              <div style="flex: 0 0 60%;">
                <select v-model="cond.survno" @change="onQuestionChange(cond, false)" class="form-select form-select-sm shadow-none bg-white py-0" style="height: 28px; font-size: 11px;">
                  <option value="">-- 설문 질문 선택 --</option>
                  <option v-for="s in currentSurveyList" :key="s.surv_no" :value="s.surv_no">[{{ s.surv_no }}] {{ s.question }}</option>
                </select>
              </div>

              <div class="flex-grow-1">
                <select v-if="cond.options && cond.options.length > 0" v-model="cond.ansno" class="form-select form-select-sm shadow-none bg-white py-0" style="height: 28px; font-size: 11px;">
                  <option value="">-- 답변 항목 선택 (객관식) --</option>
                  <option v-for="opt in cond.options" :key="opt.ans_no" :value="opt.ans_no">{{ opt.ans_no }}. {{ opt.ans_cont }} ({{ opt.point }}점)</option>
                </select>
                <input v-else v-model="cond.keyword" class="form-control form-control-sm shadow-none bg-white py-0" style="height: 28px; font-size: 11px;" placeholder="주관식 키워드 입력..." @keyup.enter="executeQuery" />
              </div>

              <button class="btn btn-sm btn-outline-danger py-0 px-2 flex-shrink-0" @click="removeConditionRow(index)" v-if="filter.conditions.length > 1" title="조건 삭제" style="height: 26px; font-size: 11px;">
                <i class="bi bi-trash"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 📑 메인 동적 결과 그리드 영역 -->
    <div class="flex-grow-1 p-2 bg-light overflow-hidden d-flex flex-column">
      <div class="card border shadow-sm overflow-hidden d-flex flex-column h-100">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="small fw-bold text-dark"><i class="bi bi-table me-1 text-primary"></i> 맞춤형 동적 조회 결과 리스트</span>
          <span class="badge bg-primary-subtle text-primary">총 {{ resultCount }} 건</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="resultGridRef" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>
    </div>
  </div>

  <!-- 쿼리 저장 모달 -->
  <div class="modal fade" id="querySaveModal" tabindex="-1" ref="querySaveModalRef">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header py-2 bg-light">
          <h6 class="modal-title fw-bold text-primary"><i class="bi bi-bookmark-plus me-1"></i>맞춤 쿼리 조건 저장</h6>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body p-3">
          <div class="mb-2">
            <label class="form-label small fw-bold text-dark mb-1">쿼리 명칭 (필수)</label>
            <input v-model="queryForm.querynm" class="form-control form-control-sm shadow-none" placeholder="예: 복수 조건 만족도 조사 쿼리" />
          </div>
          <div>
            <label class="form-label small fw-bold text-dark mb-1">쿼리 설명 / 메모</label>
            <textarea v-model="queryForm.querydesc" class="form-control form-control-sm shadow-none" rows="3" placeholder="쿼리 조건에 대한 설명을 입력하세요..."></textarea>
          </div>
        </div>
        <div class="modal-footer py-2 bg-light">
          <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">취소</button>
          <button type="button" class="btn btn-sm btn-primary fw-bold" @click="saveCustomQuery">저장하기</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { Modal } from 'bootstrap'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

const filter = reactive({
  campno: '',
  conditions: [
    { survno: '', ansno: '', keyword: '', operator: 'AND', options: [] as any[] }
  ]
})

const queryForm = reactive({
  queryid: '',
  querynm: '',
  querydesc: ''
})

const campaignList = ref<any[]>([])
const allSurveyList = ref<any[]>([])
const currentSurveyList = ref<any[]>([])
const savedQueryList = ref<any[]>([])
const selectedQueryid = ref('')
const selectedQuerydesc = ref('')
const resultCount = ref(0)

const resultGridRef = ref<HTMLDivElement | null>(null)
const querySaveModalRef = ref<HTMLElement | null>(null)
let resultTabulator: Tabulator | null = null
let queryModal: Modal | null = null

onMounted(() => {
  initResultTable()
  loadFilters()
  loadSavedQueries()
  if (querySaveModalRef.value) {
    queryModal = new Modal(querySaveModalRef.value)
  }
})

const loadFilters = async () => {
  try {
    const resCamp = await api.get('/crm/outbound/camp-list')
    campaignList.value = resCamp.data || []
    const resSurv = await api.get('/crm/outbound/surv/mst/search')
    allSurveyList.value = resSurv.data || []
    currentSurveyList.value = allSurveyList.value
  } catch (e) {
    console.warn('필터 데이터 로드 실패')
  }
}

const onCampChange = async () => {
  filter.conditions.forEach(c => { c.survno = ''; c.ansno = ''; c.keyword = ''; c.options = []; })
  if (!filter.campno) {
    currentSurveyList.value = allSurveyList.value
    return
  }
  const selectedCamp = campaignList.value.find(c => c.camp_no === filter.campno)
  if (selectedCamp && selectedCamp.surv_gb) {
    try {
      const res = await api.get('/crm/outbound/surv-form', { params: { surv_gb: selectedCamp.surv_gb } })
      currentSurveyList.value = res.data || []
    } catch (e) {
      currentSurveyList.value = allSurveyList.value
    }
  } else {
    currentSurveyList.value = allSurveyList.value
  }
}

const onQuestionChange = async (cond: any, keepAns: boolean = false) => {
  const savedAnsNo = keepAns ? cond.ansno : ''
  const savedKeyword = keepAns ? cond.keyword : ''
  cond.ansno = savedAnsNo
  cond.keyword = savedKeyword
  cond.options = []
  if (!cond.survno) return
  try {
    const res = await api.get('/crm/outbound/surv/dtl/search', { params: { surv_no: cond.survno } })
    const details = res.data || []
    if (details.length > 0) {
      cond.options = details
    }
  } catch (e) {
    console.warn('설문 답변 옵션 로드 실패')
  }
}

const loadSavedQueries = async () => {
  try {
    const res = await api.get('/crm/outbound/custom-query/list')
    savedQueryList.value = res.data || []
  } catch (e) {
    console.warn('저장된 쿼리 목록 로드 실패')
  }
}

const initResultTable = () => {
  if (!resultGridRef.value) return
  resultTabulator = new Tabulator(resultGridRef.value, {
    layout: 'fitColumns',
    height: '100%',
    placeholder: '조회된 동적 검색 결과가 없습니다. 복수 조건을 입력하고 조회하세요.',
    columns: [
      { formatter: 'rownum', title: 'No', width: 60, hozAlign: 'center' },
      { title: '캠페인번호', field: 'campno', width: 100, hozAlign: 'center' },
      { title: '캠페인명', field: 'campnm', minWidth: 160, widthGrow: 2, hozAlign: 'left' },
      { title: '고객명', field: 'custnm', width: 100, hozAlign: 'center' },
      { title: '연락처', field: 'telno', width: 150, hozAlign: 'center' },
      { title: '상담일시', field: 'calldtime', width: 150, hozAlign: 'center' },
      { title: '질문번호', field: 'survno', width: 100, hozAlign: 'center' },
      { title: '설문 질문 항목', field: 'questiontext', minWidth: 180, widthGrow: 3, hozAlign: 'left' },
      { title: '답변번호', field: 'ansno', width: 100, hozAlign: 'center' },
      { title: '답변 내용', field: 'answertext', minWidth: 150, widthGrow: 2, hozAlign: 'left' }
    ]
  })
}

const addConditionRow = () => {
  filter.conditions.push({ survno: '', ansno: '', keyword: '', operator: 'AND', options: [] })
}

const removeConditionRow = (index: number) => {
  if (filter.conditions.length > 1) {
    filter.conditions.splice(index, 1)
  }
}

const executeQuery = async () => {
  try {
    const res = await api.post('/crm/outbound/custom-query/execute', filter)
    const data = res.data || []
    resultCount.value = data.length
    if (resultTabulator) resultTabulator.setData(data)
    vAlert('복수 조건 맞춤형 쿼리가 성공적으로 조회되었습니다.')
  } catch (err: any) {
    vAlertError(err.response?.data?.message || '쿼리 실행 중 오류가 발생했습니다.')
  }
}

const openSaveQueryModal = () => {
  if (!filter.campno && filter.conditions.every(c => !c.survno && !c.keyword && !c.ansno)) {
    vAlert('저장할 검색 조건이 최소한 하나 이상 입력되어야 합니다.')
    return
  }
  queryForm.queryid = selectedQueryid.value || ''
  queryForm.querynm = ''
  queryForm.querydesc = ''
  queryModal?.show()
}

const saveCustomQuery = async () => {
  if (!queryForm.querynm.trim()) {
    vAlert('쿼리 명칭을 입력해주세요.')
    return
  }
  try {
    const payload = {
      query_id: queryForm.queryid,
      query_nm: queryForm.querynm,
      query_desc: queryForm.querydesc,
      query_json: JSON.stringify(filter)
    }
    await api.post('/crm/outbound/custom-query/save', payload)
    vAlert('맞춤 쿼리가 성공적으로 저장되었습니다.')
    queryModal?.hide()
    loadSavedQueries()
  } catch (err: any) {
    vAlertError(err.response?.data?.message || '쿼리 저장 중 오류가 발생했습니다.')
  }
}

const loadSelectedQuery = async () => {
  if (!selectedQueryid.value) {
    selectedQuerydesc.value = ''
    return
  }
  const found = savedQueryList.value.find(q => q.queryid === selectedQueryid.value)
  if (found) {
    selectedQuerydesc.value = found.querydesc || ''
    if (found.queryjson) {
      try {
        const parsed = JSON.parse(found.queryjson)
        if (parsed.campno !== undefined) {
          filter.campno = parsed.campno
          await onCampChange()
        }
        if (parsed.conditions && Array.isArray(parsed.conditions)) {
          filter.conditions = parsed.conditions
          for (const cond of filter.conditions) {
            if (cond.survno) {
              await onQuestionChange(cond, true)
            }
          }
        }
        vAlert(`[${found.querynm}] 쿼리가 불러와졌습니다.`)
        executeQuery()
      } catch (e) {
        console.error('쿼리 JSON 파싱 실패')
      }
    }
  }
}

const deleteSelectedQuery = async () => {
  if (!selectedQueryid.value) return
  if (!confirm('선택한 맞춤 쿼리를 삭제하시겠습니까?')) return
  try {
    await api.post('/crm/outbound/custom-query/delete', { query_id: selectedQueryid.value })
    vAlert('삭제되었습니다.')
    selectedQueryid.value = ''
    selectedQuerydesc.value = ''
    loadSavedQueries()
  } catch (err: any) {
    vAlertError(err.response?.data?.message || '삭제 중 오류가 발생했습니다.')
  }
}

const initialize = () => {
  filter.campno = ''
  filter.conditions = [{ survno: '', ansno: '', keyword: '', operator: 'AND', options: [] }]
  currentSurveyList.value = allSurveyList.value
  selectedQueryid.value = ''
  selectedQuerydesc.value = ''
  resultCount.value = 0
  if (resultTabulator) resultTabulator.clearData()
  vAlert('초기화되었습니다.')
}

const exportExcel = () => {
  if (resultTabulator) {
    resultTabulator.download('xlsx', 'custom_survey_query.xlsx', { sheetName: '맞춤설문조회' })
  }
}
</script>
