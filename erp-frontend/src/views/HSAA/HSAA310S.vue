<!--
	=============================================================
	프로그램명	: 신규계약현황 (HSAA310S)
	작성일자	: 2025.03.14
	작성자      : AI Assistant
	설명        : 연간/월간 실적 매트릭스 및 상세 탭 시스템 (HSOD100U 표준 레이아웃 적용)
                [디자인 표준] 색상을 배제한 깔끔한 무색 그리드 스타일 적용
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-check me-2 text-primary" style="font-size: 18px;"></i>
        영업활동관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        <span class="text-primary fw-bolder">신규계약현황 (HSAA310S)</span>
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
                <th class="text-center bg-light fw-bold">기준년도</th>
                <td>
                  <select v-model="filter.yyyy" class="form-select form-select-sm" style="width: 150px;">
                    <option v-for="y in yyyyOptions" :key="y" :value="y">{{ y }}년</option>
                  </select>
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

    <!-- 📊 3. 계약 실적 매트릭스 (깔끔한 무색 그리드) -->
    <div class="flex-grow-1 overflow-auto p-2 bg-light shadow-inner scrollbar-sm" style="max-height: 40%;">
      <div class="card border shadow-sm overflow-hidden">
        <div class="table-responsive overflow-auto scrollbar-sm">
          <table class="table table-bordered erp-matrix-table mb-0 text-center align-middle">
            <colgroup>
              <col style="width: 110px;" />
              <col style="width: 110px;" />
              <col v-for="n in 26" :key="n" style="width: 40px;" />
            </colgroup>
            <thead class="text-dark x-small fw-bold">
              <tr>
                <th rowspan="2" class="sticky-col first-col">부서명</th>
                <th rowspan="2" class="sticky-col second-col">담당자</th>
                <th v-for="m in 12" :key="m" colspan="2" class="month-header">{{ m }}월</th>
                <th colspan="2" class="month-header total-col">연간 합계</th>
              </tr>
              <tr class="sub-header">
                <template v-for="m in 12" :key="'sub'+m">
                  <th class="type-col">기업</th><th class="type-col border-end-2">개인</th>
                </template>
                <th class="type-col total-col">기업</th><th class="type-col total-col">개인</th>
              </tr>
            </thead>
            <tbody class="small">
              <template v-for="dept in matrixData" :key="dept.deptcd">
                <tr v-for="(user, uIdx) in dept.users" :key="user.userid">
                  <td v-if="uIdx === 0" :rowspan="dept.users.length + 1" class="sticky-col first-col fw-bold">{{ dept.deptnm }}</td>
                  <td class="sticky-col second-col">{{ user.usernm }}</td>
                  <template v-for="mIdx in 12" :key="'u'+user.userid+mIdx">
                    <td class="clickable" @click="drillDown(user.userid, mIdx, '010')">{{ user.monthly[mIdx-1].corp || '-' }}</td>
                    <td class="clickable border-end-2" @click="drillDown(user.userid, mIdx, '020')">{{ user.monthly[mIdx-1].indiv || '-' }}</td>
                  </template>
                  <td class="total-col">{{ user.totalcorp }}</td>
                  <td class="total-col">{{ user.totalindiv }}</td>
                </tr>
                <tr class="dept-sum-row">
                  <td class="sticky-col second-col fw-bold">부서 소계</td>
                  <template v-for="mIdx in 12" :key="'dt'+dept.deptcd+mIdx">
                    <td class="fw-bold">{{ getDeptSum(dept, mIdx, 'corp') }}</td>
                    <td class="fw-bold border-end-2">{{ getDeptSum(dept, mIdx, 'indiv') }}</td>
                  </template>
                  <td class="total-col fw-bold">{{ getDeptTotal(dept, 'corp') }}</td>
                  <td class="total-col fw-bold">{{ getDeptTotal(dept, 'indiv') }}</td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 📑 4. 하단 상세 영역 (HSOD100U 2단 구조) -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-row gap-2 bg-light main-content-wrapper" style="min-height: 0; max-height: 52%;">
      <!-- (Left) 영업 계약 리스트 (500px 고정) -->
      <div class="card border shadow-sm d-flex flex-column overflow-hidden" style="width: 500px; min-width: 500px; flex-shrink: 0;">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-list-ul me-1"></i> 영업 계약 리스트</span>
          <span class="badge bg-light text-dark border x-small">{{ masterListCount }} 건</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="listGridRef" class="tabulator-instance flex-grow-1"></div>
        </div>
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

const { today } = getDate()
const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

import { api } from '@/utils/axios'

const filter = reactive({ yyyy: new Date().getFullYear(), deptcd: '00000' })
const salesmanList = ref<any[]>([])
const deptList = ref<any[]>([])
const matrixData = ref<any[]>([])
const masterListCount = ref(0)
const detailViewerRef = ref<any>(null)
const listGridRef = ref(null)

const yyyyOptions = [2026, 2025, 2024, 2023]

// 그리드 인스턴스
let listGrid: Tabulator | null = null

const getDeptSum = (dept: any, month: number, type: 'corp' | 'indiv') => {
    return dept.users.reduce((acc: number, user: any) => acc + (user.monthly[month - 1][type] || 0), 0)
}

const getDeptTotal = (dept: any, type: 'corp' | 'indiv') => {
    return dept.users.reduce((acc: number, user: any) => acc + (type === 'corp' ? user.totalcorp : user.totalindiv), 0)
}

const getUserTotal = (user: any) => user.totalcorp + user.totalindiv

const fetchMatrix = async () => {
  try {
    const res = await api.get('/hsaa/contracts/status', {
      params: { yyyy: filter.yyyy, deptcd: filter.deptcd === '00000' ? '' : filter.deptcd }
    })

    // 백엔드 데이터(Hsaa310sDto)를 매트릭스 구조로 변환
    const rawData: any[] = res.data || []
    const grouped: any = {}

    rawData.forEach(item => {
        if (!grouped[item.deptcd]) {
            grouped[item.deptcd] = { deptcd: item.deptcd, deptnm: item.deptnm, users: [] }
        }
        const monthly = []
        for (let i = 1; i <= 12; i++) {
            const mm = String(i).padStart(2, '0')
            monthly.push({
                corp: item['m' + mm] || 0,
                indiv: item['s' + mm] || 0
            })
        }
        grouped[item.deptcd].users.push({
            userid: item.userid,
            usernm: item.usernm,
            monthly: monthly,
            totalcorp: item.mtotcnt || 0,
            totalindiv: item.stotcnt || 0
        })
    })

    matrixData.value = Object.values(grouped)
    vAlert('조회 완료');
  } catch (e) {
    vAlertError('조회 중 오류가 발생했습니다.')
  }
}

const drillDown = async (userid: string, month: number, custgbn: string) => {
  try {
    const res = await api.get('/hsaa/dashboard/list', {
      params: {
        yymm: `${filter.yyyy}${String(month).padStart(2, '0')}`,
        gubun: '310',
        userid: userid,
        custgbn: custgbn
      }
    })
    listGrid?.setData(res.data || [])
    masterListCount.value = res.data?.length || 0
  } catch (e) {
    vAlertError('상세조회 오류')
  }
}

const refreshAllDetailData = (data: any) => {
  detailViewerRef.value?.loadData(data.salesid, data.custcd)
}

const initialize = () => {
    filter.yyyy = new Date().getFullYear()
    filter.deptcd = '00000'
    matrixData.value = []
}

onMounted(async () => {
  try {
    const resUsers = await api.get('/hsaa/users')
    salesmanList.value = resUsers.data || []

    // 부서 목록 추출
    const depts: any = {}
    salesmanList.value.forEach(u => {
        if (u.deptcd) depts[u.deptcd] = u.deptnm
    })
    deptList.value = Object.entries(depts).map(([cd, nm]) => ({ cd, nm }))
  } catch (e) {}
  nextTick(() => {
    listGrid = new Tabulator(listGridRef.value!, {
      layout: "fitColumns", selectable: 1, height: "100%",
      columns: [
        { title: "No", formatter: "rownum", width: 40, hozAlign: "center", headerSort: false },
        { title: "거래처명", field: "custnm", widthGrow: 1, cssClass: "fw-bold small", tooltip: true },
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

.month-header { width: 80px !important; min-width: 80px !important; }
.type-col { width: 40px !important; min-width: 40px !important; font-weight: 500; color: #64748b; }

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
