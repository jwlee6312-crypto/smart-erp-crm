<!--
	=============================================================
	프로그램명	: 개인별 상담실적 현황 (HGOA410S)
	작성일자	: 2026.10.26
	작성자      : AI Assistant
	설명        : HGOA100U와 동일한 집계항목(부재, 3자통화, 설문중단, 거절, 결번, 기타, 예약, 통화완료) 적용 개인별 상담실적 통계 화면
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 상단 글로벌 액션 바 (HSOD100U 표준) -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-bar-chart-line-fill me-2 text-primary" style="font-size: 18px;"></i>
        아웃바운드 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        통계관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">개인별 상담실적 현황 (HGOA410S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchStatsMaster">조회</button>
        <button class="btn-erp btn-print" @click="exportExcel"><i class="bi bi-file-earmark-excel-fill me-1"></i>엑셀다운</button>
      </div>
    </div>

    <!-- 💡 검색 필터 영역 -->
    <div class="p-2 bg-light border-bottom flex-shrink-0">
      <div class="card border shadow-sm">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 8%;" /><col style="width: 25%;" />
              <col style="width: 8%;" /><col style="width: 25%;" />
              <col style="width: 8%;" /><col style="width: 26%;" />
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
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 📑 메인 분할 레이아웃 영역 -->
    <div class="flex-grow-1 p-2 bg-light overflow-hidden d-flex flex-column gap-2">
      <!-- (Top 42%) 상단 그리드: 캠페인별/상담원별 실적 집계 통계 (HGOA100U 항목 일치) -->
      <div class="card border shadow-sm overflow-hidden d-flex flex-column" style="flex: 0 0 42%; height: 42%;">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="small fw-bold text-dark"><i class="bi bi-people-fill me-1 text-primary"></i> 캠페인별·상담원별 실적 집계 현황 (각 상태별 건수 클릭 시 하단 좌측에 해당 조건의 개별 상담 건 필터링 조회)</span>
          <span class="badge bg-primary-subtle text-primary">집계 목록</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="masterGridRef" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>

      <!-- (Bottom 56%) 하단 분할 영역: 하단 좌측(개별 상담 건 및 녹취) + 하단 우측(설문 Q&A 문답) -->
      <div class="d-flex flex-row gap-2 overflow-hidden" style="flex: 0 0 56%; height: 56%;">
        <!-- 하단 좌측 (55%): 개별 상담 이력 건 리스트 및 녹취 모니터링 -->
        <div class="card border shadow-sm overflow-hidden d-flex flex-column bg-white" style="width: 55%;">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
            <span class="small fw-bold text-dark"><i class="bi bi-telephone-inbound-fill me-1 text-primary"></i> 개별 상담 건 리스트 (행 클릭 시 우측 Q&A 및 녹취 연동)</span>
            <!-- 간이 녹취 청취 플레이어 위젯 -->
            <div class="d-flex align-items-center gap-1">
              <audio controls class="form-control-sm" style="height: 26px; width: 160px;" :src="currentRecUrl" v-if="currentRecUrl"></audio>
              <span class="x-small text-muted" v-else>녹취선택안됨</span>
            </div>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="callListGridRef" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <!-- 하단 우측 (45%): 상세 설문 문답 내역 -->
        <div class="card border shadow-sm overflow-hidden d-flex flex-column bg-white" style="width: 45%;">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
            <span class="small fw-bold text-primary"><i class="bi bi-chat-left-quote-fill me-2"></i>선택 상담 건 상세 설문 문답 내역</span>
            <span class="badge bg-secondary-subtle text-secondary">Q&A</span>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="detailGridRef" class="tabulator-instance flex-grow-1"></div>
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

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

const filter = reactive({
  frdt: '',
  todt: '',
  campNo: '',
  userid: ''
})

const campaignList = ref<any[]>([])
const userList = ref<any[]>([])
const currentRecUrl = ref('')

const masterGridRef = ref<HTMLDivElement | null>(null)
const callListGridRef = ref<HTMLDivElement | null>(null)
const detailGridRef = ref<HTMLDivElement | null>(null)

let masterTabulator: Tabulator | null = null
let callListTabulator: Tabulator | null = null
let detailTabulator: Tabulator | null = null

onMounted(() => {
  const today = new Date()
  const firstDay = new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0]
  const lastDay = today.toISOString().split('T')[0]
  filter.frdt = firstDay
  filter.todt = lastDay

  initMasterTable()
  initCallListTable()
  initDetailTable()
  loadFilterData()
  fetchStatsMaster()
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
    selectable: 1,
    placeholder: '조회된 상담 실적 통계가 없습니다.',
    columns: [
      { formatter: 'rownum', title: 'No', width: 60, hozAlign: 'center' },
      { title: '캠페인명', field: 'campNm', minWidth: 160, hozAlign: 'left' },
      { title: '상담원명', field: 'userNm', width: 150, hozAlign: 'center' },
      {
        title: '총통화', field: 'totCnt', width: 100, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 },
        cellClick: (e, cell) => { const d = cell.getRow().getData(); fetchCallList(d.userid, d.campNo, ''); }
      },
      {
        title: '부재', field: 'absentCnt', width: 100, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 },
        cellClick: (e, cell) => { const d = cell.getRow().getData(); fetchCallList(d.userid, d.campNo, '부재'); }
      },
      {
        title: '3자통화', field: 'threeWayCnt', width: 100, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 },
        cellClick: (e, cell) => { const d = cell.getRow().getData(); fetchCallList(d.userid, d.campNo, '3자통화'); }
      },
      {
        title: '설문중단', field: 'surveyStopCnt', width: 100, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 },
        cellClick: (e, cell) => { const d = cell.getRow().getData(); fetchCallList(d.userid, d.campNo, '설문중단'); }
      },
      {
        title: '거절', field: 'rejectCnt', width: 100, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 },
        cellClick: (e, cell) => { const d = cell.getRow().getData(); fetchCallList(d.userid, d.campNo, '거절'); }
      },
      {
        title: '결번', field: 'wrongNumCnt', width: 100, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 },
        cellClick: (e, cell) => { const d = cell.getRow().getData(); fetchCallList(d.userid, d.campNo, '결번'); }
      },
      {
        title: '기타', field: 'etcCnt', width: 100, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 },
        cellClick: (e, cell) => { const d = cell.getRow().getData(); fetchCallList(d.userid, d.campNo, '기타'); }
      },
      {
        title: '예약', field: 'reserveCnt', width: 100, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 },
        cellClick: (e, cell) => { const d = cell.getRow().getData(); fetchCallList(d.userid, d.campNo, '예약'); }
      },
      {
        title: '통화완료', field: 'successCnt', width: 100, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 },
        cellClick: (e, cell) => { const d = cell.getRow().getData(); fetchCallList(d.userid, d.campNo, '통화완료'); }
      }
    ]
  })

  masterTabulator.on('rowClick', (e, row) => {
    const data = row.getData()
    fetchCallList(data.userid || filter.userid, data.campNo || filter.campNo, '')
  })
}

const initCallListTable = () => {
  if (!callListGridRef.value) return
  callListTabulator = new Tabulator(callListGridRef.value, {
    layout: 'fitColumns',
    height: '100%',
    selectable: 1,
    placeholder: '상단 실적 집계를 클릭하면 개별 상담 건이 조회됩니다.',
    columns: [
      { formatter: 'rownum', title: 'No', width: 60, hozAlign: 'center' },
      { title: '고객명', field: 'custNm', minWidth: 100, hozAlign: 'center' },
      { title: '연락처', field: 'telNo', width: 150, hozAlign: 'center' },
      { title: '상담결과', field: 'rsltCdNm', width: 120, hozAlign: 'center' },
      { title: '상담일시', field: 'callDtime', width: 170, hozAlign: 'center' },
      { title: '녹취듣기', field: 'recYn', width: 120, hozAlign: 'center', formatter: (cell) => {
          const val = cell.getValue()
          return val === '유' ? '<button class="btn btn-xs btn-danger py-0 px-2" style="font-size:11px;">청취</button>' : '<span class="text-muted">-</span>'
        }
      }
    ]
  })

  callListTabulator.on('rowClick', (e, row) => {
    const data = row.getData()
    currentRecUrl.value = data.recUrl || ''
    fetchDetailQa(data.callSeq || data.id)
  })
}

const initDetailTable = () => {
  if (!detailGridRef.value) return
  detailTabulator = new Tabulator(detailGridRef.value, {
    layout: 'fitColumns',
    height: '100%',
    placeholder: '개별 상담 건을 선택하면 설문 문답 내역이 조회됩니다.',
    columns: [
      { formatter: 'rownum', title: 'No', width: 60, hozAlign: 'center' },
      { title: '설문 질문 항목', field: 'questionText', minWidth: 180, hozAlign: 'left' },
      { title: '고객 답변 내용', field: 'answerText', minWidth: 140, hozAlign: 'left' },
      { title: '응답일시', field: 'addTime', width: 150, hozAlign: 'center' }
    ]
  })
}

const fetchStatsMaster = async () => {
  try {
    const res = await api.get('/crm/outbound/stats/counselor', { params: filter })
    const data = res.data || []
    if (masterTabulator) masterTabulator.setData(data)
    if (callListTabulator) callListTabulator.clearData()
    if (detailTabulator) detailTabulator.clearData()
    currentRecUrl.value = ''
    vAlert('개인별 상담실적 통계를 조회하였습니다.')
  } catch (err: any) {
    vAlertError(err.response?.data?.message || '통계 조회 중 오류가 발생했습니다.')
  }
}

const fetchCallList = async (userid: string, campNo: string, statusFilter: string = '') => {
  try {
    const res = await api.get('/crm/outbound/stats/calls', {
      params: {
        userid: userid || filter.userid,
        campno: campNo || filter.campNo,
        status: statusFilter,
        frdt: filter.frdt,
        todt: filter.todt
      }
    })
    const data = res.data || []
    if (callListTabulator) callListTabulator.setData(data)
    if (detailTabulator) detailTabulator.clearData()
    currentRecUrl.value = ''
  } catch (err: any) {
    vAlertError(err.response?.data?.message || '개별 상담 건 조회 중 오류가 발생했습니다.')
  }
}

const fetchDetailQa = async (callSeq: any) => {
  try {
    const res = await api.get('/crm/outbound/stats/qa', { params: { callseq: callSeq } })
    const data = res.data || []
    if (detailTabulator) detailTabulator.setData(data)
  } catch (err: any) {
    vAlertError(err.response?.data?.message || '설문 문답 내역 조회 중 오류가 발생했습니다.')
  }
}

const initialize = () => {
  filter.campNo = ''
  filter.userid = ''
  currentRecUrl.value = ''
  if (callListTabulator) callListTabulator.clearData()
  if (detailTabulator) detailTabulator.clearData()
  fetchStatsMaster()
  vAlert('초기화되었습니다.')
}

const exportExcel = () => {
  if (masterTabulator) {
    masterTabulator.download('xlsx', 'counselor_performance_stats.xlsx', { sheetName: '개인별상담실적' })
  }
}
</script>
