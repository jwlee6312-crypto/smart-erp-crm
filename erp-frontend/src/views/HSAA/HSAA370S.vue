<!--
	=============================================================
	프로그램명	: 성공실패원인분석 (HSAA370S)
	작성일자	: 2025.03.14
	작성자      : AI Assistant
	설명        : 영업 결과 분석 및 상세 탭 시스템 (HSOD100U 표준 레이아웃 적용)
                [디자인 표준] 깔끔한 무색 그리드 스타일 적용
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-pie-chart-fill me-2 text-primary" style="font-size: 18px;"></i>
        영업활동관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">성공실패원인분석 (HSAA370S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchAnalysis">조회</button>
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
                <th class="text-center bg-light fw-bold">등록일자</th>
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

    <!-- 📊 3. 성과 분석 매트릭스 (깔끔한 그리드 디자인) -->
    <div class="flex-grow-1 overflow-auto p-2 bg-light shadow-inner scrollbar-sm" style="max-height: 40%;">
      <div class="card border shadow-sm overflow-hidden">
        <div class="table-responsive overflow-auto scrollbar-sm">
          <table class="table table-bordered erp-matrix-table mb-0 text-center align-middle">
            <colgroup>
              <col style="width: 110px;" />
              <col style="width: 110px;" />
              <col style="width: 80px;" />
              <col v-for="n in 6" :key="n" style="width: 90px;" />
            </colgroup>
            <thead class="text-dark x-small fw-bold">
              <tr>
                <th rowspan="2" class="sticky-col first-col">부서</th>
                <th rowspan="2" class="sticky-col second-col">담당자</th>
                <th rowspan="2" class="total-col">전체건수</th>
                <th colspan="2">성공 분석</th>
                <th colspan="2">실패 분석</th>
                <th colspan="2">보류 분석</th>
              </tr>
              <tr class="sub-header">
                <th>건수</th><th>비율(%)</th>
                <th>건수</th><th>비율(%)</th>
                <th>건수</th><th>비율(%)</th>
              </tr>
            </thead>
            <tbody class="small">
              <template v-for="dept in matrixData" :key="dept.deptcd">
                <tr v-for="(user, uIdx) in dept.users" :key="user.userid">
                  <td v-if="uIdx === 0" :rowspan="dept.users.length" class="sticky-col first-col fw-bold">{{ dept.deptnm }}</td>
                  <td class="sticky-col second-col">{{ user.usernm }}</td>
                  <td class="total-col fw-bold">{{ user.totcnt }}</td>
                  <td class="clickable" @click="drillDown(user.userid, '900')">{{ user.success }}</td>
                  <td>{{ calculateRate(user.success, user.totcnt) }}%</td>
                  <td class="clickable" @click="drillDown(user.userid, '910')">{{ user.fail }}</td>
                  <td>{{ calculateRate(user.fail, user.totcnt) }}%</td>
                  <td class="clickable" @click="drillDown(user.userid, '920')">{{ user.hold }}</td>
                  <td>{{ calculateRate(user.hold, user.totcnt) }}%</td>
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
        <div class="card-header bg-white py-1 px-3 border-bottom small fw-bold text-dark">분석 대상 리스트</div>
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
import { ref, reactive, onMounted, nextTick } from 'vue'
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

let listGrid: Tabulator | null = null


const calculateRate = (part: number, total: number) => {
    if (!total || total === 0) return 0
    return Math.round((part / total) * 100)
}

const fetchAnalysis = async () => {
  try {
    const res = await api.get('/hsaa/cause/analysis', {
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
            totcnt: item.totcnt || 0,
            success: item.wincdcnt || 0,
            fail: item.failcdcnt || 0,
            hold: item.holdcnt || 0
        })
    })

    matrixData.value = Object.values(grouped)
    vAlert('조회 완료')
  } catch (e) {
    vAlertError('조회 실패')
  }
}

const drillDown = async (userid: string, code: string) => {
  try {
    const res = await api.get('/hsaa/dashboard/list', {
      params: {
        gubun: '370',
        userid: userid,
        code: code,
        sdate: filter.sdate.replace(/-/g, ''),
        edate: filter.edate.replace(/-/g, ''),
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
    listGrid = new Tabulator(listGridRef.value!, { layout: "fitColumns", selectable: 1, height: "100%", columns: [ { title: "No", formatter: "rownum", width: 40 }, { title: "거래처명", field: "custnm", widthGrow: 1, cssClass: "fw-bold small" }, { title: "영업건명", field: "salestitle", widthGrow: 1.5, cssClass: "small text-primary" } ] });
    listGrid.on("rowClick", (e, row) => refreshAllDetailData(row.getData()));
  })
})
</script>

<style scoped>
.erp-container { font-family: 'Pretendard', sans-serif; letter-spacing: -0.03rem; }

/* 💎 깔끔한 무색 그리드 스타일 */
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

.sticky-col { position: sticky; left: 0; z-index: 10; border-right: 2px solid #dee2e6 !important; }
.first-col { width: 110px !important; min-width: 110px !important; }
.second-col { left: 110px !important; width: 110px !important; min-width: 110px !important; z-index: 11; }

/* 합계/소계 강조 */
.total-col { background-color: #f1f5f9 !important; }
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
</style>
