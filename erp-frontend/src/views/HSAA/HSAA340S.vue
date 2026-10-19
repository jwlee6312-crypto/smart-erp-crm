<!--
	=============================================================
	프로그램명	: 영업단계별 진행현황 (HSAA340S)
	작성일자	: 2025.03.14
	작성자      : AI Assistant
	설명        : 기간별 영업 단계별 진행 건수 매트릭스 및 상세 탭 시스템
                [디자인 표준] 깔끔한 무색 그리드 스타일 적용
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-bar-chart-steps me-2 text-primary" style="font-size: 18px;"></i>
        영업활동관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">영업단계별 진행현황 (HSAA340S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchMatrix">조회</button>
      </div>
    </div>

    <!-- 🔍 2. 검색 필터 -->
    <div class="p-2 bg-light border-bottom flex-shrink-0">
      <div class="card border shadow-sm">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 10%;" /><col style="width: 40%;" />
              <col style="width: 10%;" /><col style="width: 40%;" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light fw-bold">등록기간</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <DateForm v-model:fromdt="filter.sdate" v-model:todt="filter.edate" />
                </td>
                <th class="text-center bg-light border-start fw-bold">담당부서</th>
                <td>
                  <select v-model="filter.deptcd" class="form-select form-select-sm" style="width: 200px;">
                    <option value="00000">전체 부서</option>
                    <option v-for="d in deptList" :key="d.cd" :value="d.cd">{{ d.nm }}</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 📊 3. 영업 단계별 매트릭스 (깔끔한 무색 그리드 디자인) -->
    <div class="flex-grow-1 overflow-auto p-2 bg-light shadow-inner scrollbar-sm" style="max-height: 40%;">
      <div class="card border shadow-sm overflow-hidden">
        <div class="table-responsive overflow-auto scrollbar-sm">
          <table class="table table-bordered erp-matrix-table mb-0 text-center align-middle">
            <colgroup>
              <col style="width: 110px;" />
              <col style="width: 110px;" />
              <col v-for="n in 8" :key="n" style="width: 100px;" />
              <col style="width: 100px;" />
            </colgroup>
            <thead class="text-dark x-small fw-bold">
              <tr>
                <th class="sticky-col first-col">부서명</th>
                <th class="sticky-col second-col">담당자</th>
                <th v-for="stage in stages" :key="stage.code" class="stage-header">
                  {{ stage.name }}
                </th>
                <th class="total-col">합계</th>
              </tr>
            </thead>
            <tbody class="small">
              <template v-for="dept in matrixData" :key="dept.deptcd">
                <tr v-for="(user, uIdx) in dept.users" :key="user.userid">
                  <td v-if="uIdx === 0" :rowspan="dept.users.length + 1" class="sticky-col first-col fw-bold">{{ dept.deptnm }}</td>
                  <td class="sticky-col second-col">{{ user.usernm }}</td>
                  <td v-for="stage in stages" :key="stage.code"
                      class="clickable" @click="drillDown(user.userid, stage.code)">
                    {{ user.counts[stage.code] || '-' }}
                  </td>
                  <td class="total-col">{{ getUserTotal(user) }}</td>
                </tr>
                <tr class="dept-sum-row">
                  <td class="sticky-col second-col fw-bold">부서 합계</td>
                  <td v-for="stage in stages" :key="'ds'+dept.deptcd+stage.code" class="fw-bold">
                    {{ getDeptSum(dept, stage.code) }}
                  </td>
                  <td class="total-col fw-bold">{{ getDeptTotal(dept) }}</td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 📑 4. 하단 상세 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-row gap-2 bg-light main-content-wrapper" style="min-height: 0; max-height: 52%;">
      <div class="card border shadow-sm d-flex flex-column overflow-hidden" style="width: 500px; min-width: 500px; flex-shrink: 0;">
        <div class="card-header bg-white py-1 px-3 border-bottom small fw-bold text-dark">영업 대상 리스트</div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column"><div ref="listGridRef" class="tabulator-instance flex-grow-1"></div></div>
      </div>
      <!-- (Right) 탭 기반 상세 정보 영역 (공통 컴포넌트 적용) -->
      <div class="flex-grow-1 detail-column-expand" style="min-width: 0;">
        <SalesDetailViewer ref="detailViewerRef" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import DateForm from '@/components/DateForm.vue'
import SalesDetailViewer from './components/SalesDetailViewer.vue'
import { getDate } from '@/composables/useDate'

const { firstDay, today } = getDate()
const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

import { api } from '@/utils/axios'

const filter = reactive({ sdate: firstDay, edate: today, deptcd: '00000' })
const deptList = ref<any[]>([])
const matrixData = ref<any[]>([])
const masterListCount = ref(0)
const detailViewerRef = ref<any>(null)
const listGridRef = ref(null)

const stages = [
    { code: '100', name: '고객선정' },
    { code: '200', name: '고객접촉' },
    { code: '300', name: '제안견적' },
    { code: '400', name: '설치' },
    { code: '900', name: '성공' },
    { code: '910', name: '실패' },
    { code: '920', name: '보류' },
    { code: '930', name: '포기' }
]

const detailTabs = [
  { id: 'SALES', no: 1, name: '영업건 상세' },
  { id: 'DIARY', no: 2, name: '상담일지' },
  { id: 'KEYMAN', no: 3, name: 'Key맨' },
  { id: 'STAGES', no: 4, name: '단계변동' }
]

let listGrid: Tabulator | null = null


const getDeptSum = (dept: any, stageCode: string) => {
    return dept.users.reduce((acc: number, user: any) => acc + (user.counts[stageCode] || 0), 0)
}

const getDeptTotal = (dept: any) => {
    return dept.users.reduce((acc: number, user: any) => acc + getUserTotal(user), 0)
}

const getUserTotal = (user: any) => {
    return Object.values(user.counts).reduce((acc: number, val: any) => acc + (Number(val) || 0), 0)
}

const fetchMatrix = async () => {
  try {
    const res = await api.get('/hsaa/stages/progress', {
      params: {
        sdate: filter.sdate.replace(/-/g, ''),
        edate: filter.edate.replace(/-/g, ''),
        deptcd: filter.deptcd === '00000' ? '' : filter.deptcd
      }
    })

    const rawData: any[] = res.data || []
    const grouped: any = {}

    rawData.forEach(item => {
        if (!grouped[item.deptcd]) {
            grouped[item.deptcd] = { deptcd: item.deptcd, deptnm: item.deptnm, users: [] }
        }
        grouped[item.deptcd].users.push({
            userid: item.userid,
            usernm: item.usernm,
            counts: {
                '100': item.s100 || 0,
                '200': item.s200 || 0,
                '300': item.s300 || 0,
                '400': item.s400 || 0,
                '900': item.s900 || 0,
                '910': item.s910 || 0,
                '920': item.s920 || 0,
                '930': item.s930 || 0
            }
        })
    })

    matrixData.value = Object.values(grouped)
    vAlert('조회 완료')
  } catch (e) {
    vAlertError('조회 중 오류 발생')
  }
}

const drillDown = async (userid: string, code: string) => {
  try {
    const res = await api.get('/hsaa/dashboard/list', {
      params: {
        gubun: '340',
        userid: userid,
        code: code,
        yymm: filter.sdate.replace(/-/g, '').substring(0, 6)
      }
    })
    listGrid?.setData(res.data || [])
  } catch (e) {
    vAlertError('상세조회 실패')
  }
}

const refreshAllDetailData = (data: any) => {
  detailViewerRef.value?.loadData(data.salesid, data.custcd)
}

const initialize = () => {
    filter.sdate = firstDay
    filter.edate = today
    filter.deptcd = '00000'
    matrixData.value = []
}

onMounted(async () => {
  try {
    const resUsers = await api.get('/hsaa/users')
    const users = resUsers.data || []
    const depts: any = {}
    users.forEach((u: any) => { if (u.deptcd) depts[u.deptcd] = u.deptnm })
    deptList.value = Object.entries(depts).map(([cd, nm]) => ({ cd, nm }))
  } catch (e) {}
  nextTick(() => {
    listGrid = new Tabulator(listGridRef.value!, {
      layout: "fitColumns", selectable: 1, height: "100%",
      columns: [
        { title: "No", formatter: "rownum", width: 40, hozAlign: "center", headerSort: false },
        { title: "거래처명", field: "custnm", widthGrow: 1, cssClass: "fw-bold small" },
        { title: "영업건명", field: "salestitle", widthGrow: 2, cssClass: "small text-primary", tooltip: true, formatter: (cell) => {
            const val = cell.getValue() || ''; return val.length > 30 ? val.substring(0, 30) + "..." : val;
        }}
      ]
    })
    listGrid.on("rowClick", (e, row) => refreshAllDetailData(row.getData()));
  })
})
</script>

<style scoped>
.erp-container { font-family: 'Pretendard', sans-serif; letter-spacing: -0.03rem; }

/* 💎 깔끔한 무색 그리드 표준 (Collapse 스타일) */
.erp-matrix-table {
    border-collapse: collapse !important;
    font-size: 11px;
    table-layout: fixed;
    width: max-content;
    min-width: 100%;
    border: 1px solid #dee2e6 !important;
}
.erp-matrix-table th {
    background-color: #f8fafc !important;
    border: 1px solid #dee2e6 !important;
    color: #334155 !important;
    padding: 6px 2px !important;
    font-weight: 700;
}
.erp-matrix-table td {
    border: 1px solid #dee2e6 !important;
    padding: 4px 2px !important;
    background-color: #fff !important;
    height: 30px;
}

.sticky-col { position: sticky; left: 0; z-index: 10; border-right: 2px solid #dee2e6 !important; background-color: #f8fafc !important; }
.first-col { width: 110px !important; min-width: 110px !important; }
.second-col { left: 110px !important; width: 110px !important; min-width: 110px !important; z-index: 11; }

.stage-header { width: 100px !important; min-width: 100px !important; }

/* 합계/소계 강조 */
.total-col { background-color: #f1f5f9 !important; }
.dept-sum-row td { background-color: #f8fafc !important; border-top: 2px solid #cbd5e1 !important; }

.clickable { cursor: pointer; text-decoration: underline; color: #0d6efd; }
.clickable:hover { background-color: #f1f5f9 !important; }

.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 12px; border: none; }
.main-content-wrapper { display: flex !important; flex-direction: row !important; align-items: stretch !important; }

.btn-erp { height: 28px; padding: 0 12px; font-size: 12px; font-weight: 600; border-radius: 4px; border: 1px solid #dee2e6; background-color: #fff; color: #444; }
.btn-search { background-color: #0d6efd !important; border-color: #0d6efd !important; color: #fff !important; }

.erp-table-dense th { padding: 6px 8px; border: 1px solid #dee2e6; font-size: 11px; font-weight: 700; color: #555; background-color: #f8f9fa; }
.erp-table-dense td { padding: 3px 8px; border: 1px solid #dee2e6; background-color: #fff; vertical-align: middle; }

.nav-tabs .nav-link { color: #64748b; border-radius: 0; background: transparent !important; padding: 0.6rem 1.4rem !important; }
.nav-tabs .nav-link.active { color: #0d6efd !important; font-weight: 800 !important; border-bottom: 3px solid #0d6efd !important; }

:deep(.tabulator-row.tabulator-selected) { background-color: #eef6ff !important; color: #0d6efd !important; font-weight: bold; }
</style>
