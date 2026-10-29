<!--
	=============================================================
	프로그램명	: 캠페인 및 상담 모니터링 / 결과 보정 관리 (HGOA400U)
	작성일자	: 2026.10.26
	작성자      : AI Assistant
	설명        : 관리자용 캠페인/상담원별 상담이력 모니터링, 체크박스 선택 및 상담결과/보정답변 인라인 수정, 평가자(evaluator) 및 평가등급(grade) 보정 관리
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 상단 글로벌 액션 바 (HSOD100U 표준) -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-headset me-2 text-primary" style="font-size: 18px;"></i>
        아웃바운드 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        캠페인관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">상담 모니터링 및 결과보정 (HGOA400U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchMasterList">조회</button>
        <button class="btn-erp btn-save" @click="saveCorrection">보정저장</button>
        <button class="btn-erp btn-print" @click="exportExcel"><i class="bi bi-file-earmark-excel-fill me-1"></i>엑셀다운</button>
      </div>
    </div>

    <!-- 💡 검색 필터 영역 -->
    <div class="p-2 bg-light border-bottom flex-shrink-0">
      <div class="card border shadow-sm">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 8%;" /><col style="width: 20%;" />
              <col style="width: 8%;" /><col style="width: 20%;" />
              <col style="width: 8%;" /><col style="width: 18%;" />
              <col style="width: 8%;" /><col style="width: 10%;" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light fw-bold">조회기간</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <input type="date" v-model="filter.frdt" class="form-control form-control-sm" />
                  <span>~</span>
                  <input type="date" v-model="filter.todt" class="form-control form-control-sm" />
                </td>
                <th class="text-center bg-light border-start fw-bold">캠페인선택</th>
                <td>
                  <select v-model="filter.campNo" class="form-select form-select-sm">
                    <option value="">-- 전체 캠페인 --</option>
                    <option v-for="c in campaignList" :key="c.camp_no" :value="c.camp_no">{{ c.camp_nm }}</option>
                  </select>
                </td>
                <th class="text-center bg-light border-start fw-bold">상담원선택</th>
                <td>
                  <select v-model="filter.userid" class="form-select form-select-sm">
                    <option value="">-- 전체 상담원 --</option>
                    <option v-for="u in userList" :key="u.userid" :value="u.userid">{{ u.usernm }} ({{ u.userid }})</option>
                  </select>
                </td>
                <th class="text-center bg-light border-start fw-bold">고객검색</th>
                <td>
                  <input v-model="filter.keyword" class="form-control form-control-sm" placeholder="고객명/연락처" @keyup.enter="fetchMasterList" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 📑 메인 분할 레이아웃 영역 -->
    <div class="flex-grow-1 p-2 bg-light overflow-hidden d-flex flex-column gap-2">
      <!-- (Top 45%) 상단 영역: 좌측 마스터 리스트 + 우측 상담 내용 (전화메모 / 채팅로그) -->
      <div class="d-flex flex-row gap-2 overflow-hidden" style="flex: 0 0 45%; height: 45%;">
        <!-- 좌측: 상담 이력 모니터링 목록 (65%) - 체크박스 및 상담결과 수정 가능 -->
        <div class="card border shadow-sm overflow-hidden d-flex flex-column" style="width: 65%;">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
            <span class="small fw-bold text-dark"><i class="bi bi-list-check me-1 text-primary"></i> 상담 이력 모니터링 목록 (체크박스 선택 및 '상담결과' 직접 수정 가능)</span>
            <span class="badge bg-primary-subtle text-primary">총 {{ masterCount }} 건</span>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="masterGridRef" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <!-- 우측: 상담 내용 (전화메모 / 채팅로그) (35%) -->
        <div class="card border shadow-sm overflow-hidden d-flex flex-column bg-white" style="width: 35%;">
          <div class="card-header bg-light py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
            <span class="small fw-bold text-dark"><i class="bi bi-file-text me-1 text-primary"></i> 상담 내용 (전화메모 / 채팅로그)</span>
            <!-- 간이 녹취 청취 플레이어 위젯 -->
            <div class="d-flex align-items-center gap-1">
              <audio controls class="form-control-sm" style="height: 26px; width: 140px;" :src="currentRecUrl" v-if="currentRecUrl"></audio>
              <span class="x-small text-muted" v-else>녹취없음</span>
            </div>
          </div>
          <div class="card-body p-2 bg-light overflow-auto flex-grow-1" style="font-size: 12px; white-space: pre-wrap; line-height: 1.4;">
            {{ formCorrection.callMemo || '상담 이력 목록에서 항목을 선택하면 상세 상담 내용이 여기에 표시됩니다.' }}
          </div>
        </div>
      </div>

      <!-- (Bottom 55%) 하단 영역: 좌측 Q&A 보정 그리드 + 우측 관리자 보정 및 평가자 폼 -->
      <div class="card border shadow-sm overflow-hidden d-flex flex-column bg-white" style="flex: 0 0 55%; height: 55%;">
        <div class="card-header bg-white border-bottom py-1 px-3 d-flex justify-content-between align-items-center">
          <span class="small fw-bold text-primary"><i class="bi bi-chat-left-text-fill me-2"></i>설문 Q&A 답변 보정 및 관리자 최종 평가</span>
        </div>
        <div class="card-body p-2 overflow-hidden d-flex flex-row gap-2">
          <!-- 1. 설문 Q&A 문답 보정 그리드 (60%) - 체크박스 및 보정답변 수정 가능 -->
          <div class="flex-grow-1 d-flex flex-column overflow-hidden border rounded" style="width: 60%;">
            <div class="bg-light p-1 px-2 border-bottom small fw-bold text-dark">
              <i class="bi bi-question-circle me-1"></i> 설문 응답 내용 및 '보정 답변' 수정
            </div>
            <div ref="qaGridRef" class="tabulator-instance flex-grow-1"></div>
          </div>

          <!-- 2. 관리자 보정 및 평가자 지정 폼 (40%) -->
          <div class="card border shadow-sm d-flex flex-column" style="width: 40%;">
            <div class="card-header bg-light py-1 px-2 small fw-bold text-dark border-bottom">
              <i class="bi bi-person-check me-1"></i> 평가자 지정 및 결과 보정 관리
            </div>
            <div class="card-body p-3 d-flex flex-column gap-2 overflow-auto">
              <div>
                <label class="form-label x-small fw-bold text-muted mb-1">평가자 (Evaluator)</label>
                <select v-model="formCorrection.evaluator" class="form-select form-select-sm">
                  <option value="">-- 평가자 선택 --</option>
                  <option v-for="u in userList" :key="u.userid" :value="u.userid">{{ u.usernm }} ({{ u.userid }})</option>
                </select>
              </div>
              <div>
                <label class="form-label x-small fw-bold text-muted mb-1">평가 등급 (Grade)</label>
                <select v-model="formCorrection.grade" class="form-select form-select-sm">
                  <option value="S">S (우수 - 모범 상담)</option>
                  <option value="A">A (양호)</option>
                  <option value="B">B (보통 - 보완 필요)</option>
                  <option value="C">C (미흡 - 재교육 대상)</option>
                </select>
              </div>
              <div class="flex-grow-1 d-flex flex-column">
                <label class="form-label x-small fw-bold text-muted mb-1">관리자 보정 코멘트 / 피드백</label>
                <textarea v-model="formCorrection.managerComment" class="form-control form-control-sm flex-grow-1" rows="3" placeholder="상담 내용 검토 후 피드백이나 보정 사유 입력..."></textarea>
              </div>
              <div class="d-flex justify-content-end gap-1">
                <button class="btn-erp btn-init" @click="resetCorrection">취소</button>
                <button class="btn-erp btn-save" @click="saveCorrection">보정반영</button>
              </div>
            </div>
          </div>
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
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

const filter = reactive({
  frdt: '',
  todt: '',
  campNo: '',
  userid: '',
  keyword: ''
})

const campaignList = ref<any[]>([])
const userList = ref<any[]>([])
const masterCount = ref(0)
const currentRecUrl = ref('')

const formCorrection = reactive({
  evaluator: authStore.userid || 'admin',
  grade: 'A',
  managerComment: '',
  callMemo: ''
})

const masterGridRef = ref<HTMLDivElement | null>(null)
const qaGridRef = ref<HTMLDivElement | null>(null)
let masterTabulator: Tabulator | null = null
let qaTabulator: Tabulator | null = null

onMounted(() => {
  const today = new Date()
  const firstDay = new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0]
  const lastDay = today.toISOString().split('T')[0]
  filter.frdt = firstDay
  filter.todt = lastDay

  initMasterTable()
  initQaTable()
  loadFilterData()
  fetchMasterList()
})

const loadFilterData = async () => {
  try {
    const resCamp = await api.get('/crm/outbound/camp-list')
    campaignList.value = resCamp.data || []
    const resUser = await api.get('/hsaa/users')
    userList.value = resUser.data?.data || resUser.data || []
  } catch (e) {
    console.warn('필터 데이터 로드 실패')
  }
}

const initMasterTable = () => {
  if (!masterGridRef.value) return
  masterTabulator = new Tabulator(masterGridRef.value, {
    layout: 'fitColumns',
    height: '100%',
    selectable: true,
    placeholder: '조회된 상담 이력이 없습니다.',
    columns: [
      { formatter: 'rowSelection', titleFormatter: 'rowSelection', width: 45, hozAlign: 'center', headerSort: false, frozen: true },
      { title: '캠페인명', field: 'campNm', width: 180, hozAlign: 'left' },
      { title: '상담원', field: 'userNm', width: 100, hozAlign: 'center' },
      { title: '고객명', field: 'custNm', width: 100, hozAlign: 'center' },
      { title: '연락처', field: 'telNo', width: 120, hozAlign: 'center' },
      { title: '상담일시', field: 'callDtime', width: 140, hozAlign: 'center' },
      { title: '상담결과 (수정)', field: 'rsltCdNm', width: 120, hozAlign: 'center', editor: 'input' },
      { title: '평가등급', field: 'grade', width: 80, hozAlign: 'center' },
      { title: '평가자', field: 'evaluator', width: 90, hozAlign: 'center' },
      { title: '녹취유무', field: 'recYn', width: 80, hozAlign: 'center' }
    ]
  })

  masterTabulator.on('rowClick', (e, row) => {
    const data = row.getData()
    currentRecUrl.value = data.recUrl || ''
    formCorrection.evaluator = data.evaluator || authStore.userid || 'admin'
    formCorrection.grade = data.grade || 'A'
    formCorrection.managerComment = data.managerComment || ''
    formCorrection.callMemo = data.callMemo || ''
    fetchQaData(data.callSeq || data.id)
  })
}

const initQaTable = () => {
  if (!qaGridRef.value) return
  qaTabulator = new Tabulator(qaGridRef.value, {
    layout: 'fitColumns',
    height: '100%',
    selectable: true,
    placeholder: '선택된 상담의 Q&A 내역이 없습니다.',
    columns: [
      { formatter: 'rowSelection', titleFormatter: 'rowSelection', width: 45, hozAlign: 'center', headerSort: false, frozen: true },
      { title: '질문 항목', field: 'questionText', minWidth: 180, hozAlign: 'left' },
      { title: '고객 응답', field: 'answerText', width: 120, hozAlign: 'left' },
      { title: '보정 답변 (수정)', field: 'correctedAnswer', width: 150, hozAlign: 'left', editor: 'input' }
    ]
  })
}

const fetchMasterList = async () => {
  try {
    const res = await api.get('/crm/outbound/monitoring/master', { params: filter })
    const data = res.data || []
    masterCount.value = data.length
    if (masterTabulator) masterTabulator.setData(data)
  } catch (err: any) {
    vAlertError(err.response?.data?.message || '상담 모니터링 데이터 조회 중 오류가 발생했습니다.')
  }
}

const fetchQaData = async (callSeq: any) => {
  try {
    const res = await api.get('/crm/outbound/monitoring/qa', { params: { callSeq } })
    if (qaTabulator) qaTabulator.setData(res.data || [])
  } catch (err: any) {
    console.warn('Q&A 데이터 조회 실패')
  }
}

const initialize = () => {
  filter.campNo = ''
  filter.userid = ''
  filter.keyword = ''
  currentRecUrl.value = ''
  formCorrection.evaluator = authStore.userid || 'admin'
  formCorrection.grade = 'A'
  formCorrection.managerComment = ''
  formCorrection.callMemo = ''
  if (qaTabulator) qaTabulator.clearData()
  fetchMasterList()
  vAlert('초기화되었습니다.')
}

const resetCorrection = () => {
  formCorrection.managerComment = ''
  vAlert('보정 폼이 초기화되었습니다.')
}

const saveCorrection = async () => {
  try {
    const selectedMaster = masterTabulator ? masterTabulator.getSelectedData() : []
    const selectedQa = qaTabulator ? qaTabulator.getSelectedData() : []

    if (selectedMaster.length === 0) {
      vAlert('저장할 상담 이력 항목을 체크박스로 선택해주세요.')
      return
    }

    const payload = {
      correctionForm: formCorrection,
      selectedMaster: selectedMaster,
      selectedQa: selectedQa
    }

    await api.post('/crm/outbound/monitoring/save', payload)
    vAlert('선택된 상담건의 결과 보정 및 평가가 성공적으로 저장되었습니다.')
    fetchMasterList()
  } catch (err: any) {
    vAlertError(err.response?.data?.message || '보정 저장 중 오류가 발생했습니다.')
  }
}

const exportExcel = () => {
  if (masterTabulator) {
    masterTabulator.download('xlsx', 'consultation_monitoring.xlsx', { sheetName: '상담모니터링' })
  }
}
</script>
