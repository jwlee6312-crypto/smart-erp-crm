<!--
	=============================================================
	프로그램명	: 영업종합현황 (HSAA200S)
	작성일자	: 2025.03.14
	작성자      : AI Assistant
	설명        : 영업 목표/실적 대시보드 및 단계별 현황 매트릭스
                (디자인 표준화: 상단 KPI 압축 및 하단 HSOD100U 표준 레이아웃 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
			<div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-graph-up-arrow me-2 text-primary" style="font-size: 18px;"></i>
				영업활동관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">영업종합현황 (HSAA200S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1 pe-3">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchData">조회</button>
			</div>
		</div>

		<!-- 🔍 2. 검색 필터 (표준 Dense Table 스타일) -->
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
                <th class="text-center bg-light fw-bold">기준년월</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <select v-model="filter.yyyy" class="form-select form-select-sm" style="width: 100px;">
                    <option v-for="y in yyyyOptions" :key="y" :value="y">{{ y }}년</option>
                  </select>
                  <select v-model="filter.mm" class="form-select form-select-sm" style="width: 80px;">
                    <option v-for="m in 12" :key="m" :value="String(m).padStart(2, '0')">{{ m }}월</option>
                  </select>
                </td>
                <th class="text-center bg-light border-start fw-bold">영업담당</th>
                <td>
                  <select v-model="filter.userid" class="form-select form-select-sm" style="width: 150px;">
                    <option value="000">전체 담당자</option>
                    <option v-for="user in userData" :key="user.userid" :value="user.userid">{{ user.usernm }}</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

		<!-- 📊 3. 메인 대시보드 콘텐츠 (영역 압축) -->
		<div class="overflow-auto p-2 pb-0 bg-light scrollbar-sm d-flex flex-column gap-1 flex-shrink-0" style="max-height: 48%;">

			<!-- (A) 상단 KPI 압축 섹션 -->
			<div class="row g-1 flex-shrink-0">
				<div class="col-md-3">
					<div class="kpi-mini-card bg-primary text-white shadow-sm rounded d-flex align-items-center px-3 py-2">
            <div class="me-3"><i class="bi bi-bullseye fs-4 opacity-50"></i></div>
						<div class="flex-grow-1">
              <div class="x-small fw-bold opacity-75">당월 목표 실적</div>
              <div class="h6 mb-0 fw-bolder">{{ formatMoney(summary.monthlygoal) }}</div>
            </div>
            <div class="text-end ms-2">
              <div class="badge bg-white text-primary x-small fw-bolder">{{ summary.monthlyrate }}%</div>
            </div>
					</div>
				</div>
				<div class="col-md-3">
					<div class="kpi-mini-card bg-info text-white shadow-sm rounded d-flex align-items-center px-3 py-2">
            <div class="me-3"><i class="bi bi-check2-circle fs-4 opacity-50"></i></div>
						<div class="flex-grow-1">
              <div class="x-small fw-bold opacity-75">당월 실제 수주</div>
              <div class="h6 mb-0 fw-bolder">{{ formatMoney(summary.monthlyactual) }}</div>
            </div>
					</div>
				</div>
				<div class="col-md-3">
					<div class="kpi-mini-card bg-warning text-dark shadow-sm rounded d-flex align-items-center px-3 py-2 border border-warning">
            <div class="me-3"><i class="bi bi-graph-up fs-4 opacity-50"></i></div>
						<div class="flex-grow-1">
              <div class="x-small fw-bold text-muted">년간 누계 목표</div>
              <div class="h6 mb-0 fw-bolder">{{ formatMoney(summary.yearlygoal) }}</div>
            </div>
					</div>
				</div>
				<div class="col-md-3">
					<div class="kpi-mini-card bg-dark text-white shadow-sm rounded d-flex align-items-center px-3 py-2">
            <div class="me-3"><i class="bi bi-lightning-charge fs-4 opacity-50"></i></div>
						<div class="d-flex gap-3">
              <div><div class="x-small opacity-75">방문</div><div class="fw-bolder">{{ summary.visitcnt }}</div></div>
              <div class="border-start border-secondary opacity-50 mx-1"></div>
              <div><div class="x-small opacity-75">선정</div><div class="fw-bolder">{{ summary.selectioncnt }}</div></div>
            </div>
					</div>
				</div>
			</div>

			<!-- (B) 단계별/중요도별 매트릭스 -->
			<div class="card border shadow-sm overflow-hidden">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
					<span class="fw-bold x-small text-dark"><i class="bi bi-grid-3x3-gap me-1 text-primary"></i> 영업 단계별 현황</span>
					<div class="d-flex gap-3 x-small">
						<span class="text-danger fw-bold"><i class="bi bi-circle-fill me-1 small"></i>상</span>
						<span class="text-warning fw-bold"><i class="bi bi-circle-fill me-1 small"></i>중</span>
						<span class="text-success fw-bold"><i class="bi bi-circle-fill me-1 small"></i>하</span>
					</div>
				</div>
				<div class="card-body p-0">
					<table class="table table-bordered erp-table-compact mb-0 text-center">
						<thead class="bg-light align-middle x-small fw-bold">
							<tr>
								<th rowspan="2" style="width: 120px;">영업단계</th>
								<th rowspan="2" style="width: 80px;">당월발생</th>
								<th rowspan="2" style="width: 80px;">보유건수</th>
								<th colspan="4" class="bg-primary bg-opacity-5">중요도별 건수</th>
								<th colspan="4" class="bg-warning bg-opacity-5">단계변동무 (30일↑)</th>
								<th colspan="4" class="bg-info bg-opacity-5">고객접촉무 (30일↑)</th>
							</tr>
							<tr class="sub-th">
								<th class="text-danger">상</th><th>중</th><th class="text-success">하</th><th class="bg-light">계</th>
								<th>상</th><th>중</th><th>하</th><th class="bg-light">계</th>
								<th>상</th><th>중</th><th>하</th><th class="bg-light">계</th>
							</tr>
						</thead>
						<tbody class="align-middle x-small fw-bold">
							<tr v-for="row in matrix" :key="row.stage">
								<td class="bg-light text-start ps-3">{{ row.stagenm }}</td>
								<td class="text-primary clickable" @click="drillDown(row.stage, 'NEW')">{{ row.monthlynew }}</td>
								<td class="clickable fw-bolder text-dark" @click="drillDown(row.stage, 'TOTAL')">{{ row.totalholding }}</td>
								<!-- 중요도별 -->
								<td class="text-danger">{{ row.impa }}</td><td>{{ row.impb }}</td><td class="text-success">{{ row.impc }}</td><td class="bg-light-subtle">{{ row.impsum }}</td>
								<!-- 단계변동무 -->
								<td>{{ row.nochga }}</td><td>{{ row.nochgb }}</td><td>{{ row.nochgc }}</td><td class="bg-light-subtle">{{ row.nochgsum }}</td>
								<!-- 고객접촉무 -->
								<td>{{ row.nocnta }}</td><td>{{ row.nocntb }}</td><td>{{ row.nocntc }}</td><td class="bg-light-subtle">{{ row.nocntsum }}</td>
							</tr>
						</tbody>
						<tfoot class="bg-secondary bg-opacity-5 x-small fw-bolder border-top-2">
							<tr>
								<td class="bg-white">전체 합계</td>
								<td class="text-primary">{{ total.monthlynew }}</td><td class="text-dark">{{ total.totalholding }}</td>
								<td class="text-danger">{{ total.impa }}</td><td>{{ total.impb }}</td><td class="text-success">{{ total.impc }}</td><td class="bg-secondary bg-opacity-10">{{ total.impsum }}</td>
								<td>{{ total.nochga }}</td><td>{{ total.nochgb }}</td><td>{{ total.nochgc }}</td><td class="bg-secondary bg-opacity-10">{{ total.nochgsum }}</td>
								<td>{{ total.nocnta }}</td><td>{{ total.nocntb }}</td><td>{{ total.nocntc }}</td><td class="bg-secondary bg-opacity-10">{{ total.nocntsum }}</td>
							</tr>
						</tfoot>
					</table>
				</div>
			</div>

			<!-- (C) 기타 결과 현황 섹션 (한 줄 압축) -->
			<div class="bg-white border shadow-sm rounded px-3 py-1 d-flex align-items-center justify-content-center gap-4 x-small fw-bolder flex-shrink-0">
        <div class="clickable" @click="drillDown('900')">성공 <span class="text-primary fs-6">{{ results.success }}</span>건</div>
        <div class="vr opacity-25"></div>
        <div class="clickable" @click="drillDown('910')">실패 <span class="text-danger fs-6">{{ results.fail }}</span>건</div>
        <div class="vr opacity-25"></div>
        <div class="clickable" @click="drillDown('920')">보류 <span class="text-warning fs-6">{{ results.holdover }}/{{ results.holdtotal }}</span>건</div>
        <div class="vr opacity-25"></div>
        <div class="clickable" @click="drillDown('930')">포기 <span class="text-secondary fs-6">{{ results.abandon }}</span>건</div>
      </div>
		</div>

		<!-- 📑 4. 상세 리스트 (HSOD100U 표준 2단 레이아웃) -->
		<div class="flex-grow-1 overflow-hidden p-2 pt-0 d-flex flex-row gap-2 bg-light border-top shadow-sm" style="min-height: 0;">
      <!-- (Left) 영업건 리스트 (500px 고정) -->
      <div class="card border shadow-sm d-flex flex-column overflow-hidden" style="width: 500px; min-width: 500px; flex-shrink: 0;">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="fw-bold small text-dark"><i class="bi bi-list-ul me-1 text-primary"></i> {{ drillTitle }}</span>
          <span class="badge bg-primary-subtle text-primary border x-small">{{ masterListCount }} 건</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="detailGridRef" class="tabulator-instance flex-grow-1"></div>
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
import { ref, reactive, onMounted, nextTick } from 'vue'
import { api } from '@/utils/axios'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import SalesDetailViewer from './components/SalesDetailViewer.vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

// 📋 상태 데이터
const filter = reactive({
	yyyy: new Date().getFullYear(),
	mm: String(new Date().getMonth() + 1).padStart(2, '0'),
	userid: '000'
})

const yyyyOptions = [2026, 2025, 2024]
const drillTitle = ref('데이터를 선택하세요')
const userData = ref<any[]>([])
const masterListCount = ref(0)
const detailViewerRef = ref<any>(null)

// 대시보드 데이터 실시간 바인딩 객체
const summary = reactive({
	monthlygoal: 0, monthlyactual: 0, monthlyrate: 0,
	yearlygoal: 0, yearlyactual: 0,
	visitcnt: 0, selectioncnt: 0
})

const matrix = ref([
	{ stage: '100', stagenm: '고객접촉', monthlynew: 0, totalholding: 0, impa: 0, impb: 0, impc: 0, impsum: 0, nochga: 0, nochgb: 0, nochgc: 0, nochgsum: 0, nocnta: 0, nocntb: 0, nocntc: 0, nocntsum: 0 },
	{ stage: '200', stagenm: '가망고객', monthlynew: 0, totalholding: 0, impa: 0, impb: 0, impc: 0, impsum: 0, nochga: 0, nochgb: 0, nochgc: 0, nochgsum: 0, nocnta: 0, nocntb: 0, nocntc: 0, nocntsum: 0 },
	{ stage: '300', stagenm: '제안견적', monthlynew: 0, totalholding: 0, impa: 0, impb: 0, impc: 0, impsum: 0, nochga: 0, nochgb: 0, nochgc: 0, nochgsum: 0, nocnta: 0, nocntb: 0, nocntc: 0, nocntsum: 0 },
	{ stage: '400', stagenm: '계약단계', monthlynew: 0, totalholding: 0, impa: 0, impb: 0, impc: 0, impsum: 0, nochga: 0, nochgb: 0, nochgc: 0, nochgsum: 0, nocnta: 0, nocntb: 0, nocntc: 0, nocntsum: 0 }
])

const total = reactive({
	monthlynew: 0, totalholding: 0, impa: 0, impb: 0, impc: 0, impsum: 0,
	nochga: 0, nochgb: 0, nochgc: 0, nochgsum: 0,
	nocnta: 0, nocntb: 0, nocntc: 0, nocntsum: 0
})

const results = reactive({ success: 0, fail: 0, holdover: 0, holdtotal: 0, abandon: 0 })

// 📊 그리드 인스턴스
const detailGridRef = ref(null); let detailGrid: Tabulator | null = null

const formatMoney = (val: any) => new Intl.NumberFormat('ko-KR').format(Number(val) || 0)

const initialize = () => {
	filter.userid = '000'
  filter.yyyy = new Date().getFullYear()
  filter.mm = String(new Date().getMonth() + 1).padStart(2, '0')
}

const fetchData = async () => {
	try {
		const res = await api.get('/hsaa/dashboard/stats', {
			params: {
				yymm: `${filter.yyyy}${filter.mm}`,
				userid: filter.userid === '000' ? '' : filter.userid
			}
		})
		const data: any[] = res.data || []

        // 1. 초기화
        Object.assign(summary, { monthlygoal: 0, monthlyactual: 0, monthlyrate: 0, yearlygoal: 0, yearlyactual: 0, visitcnt: 0, selectioncnt: 0 })
        Object.assign(results, { success: 0, fail: 0, holdover: 0, holdtotal: 0, abandon: 0 })
        matrix.value.forEach(row => {
            row.monthlynew = 0; row.totalholding = 0;
            row.impa = 0; row.impb = 0; row.impc = 0; row.impsum = 0;
            row.nochga = 0; row.nochgb = 0; row.nochgc = 0; row.nochgsum = 0;
            row.nocnta = 0; row.nocntb = 0; row.nocntc = 0; row.nocntsum = 0;
        })

		// 2. 백엔드 HsaaStatDto 파싱
        data.forEach(item => {
            const val = Number(item.val) || 0
            if (item.gubun === 'G0') {
                const target = matrix.value.find(r => r.stage === item.code)
                if (target) target.totalholding = val
            } else if (item.gubun === 'G9') {
                const target = matrix.value.find(r => r.stage === item.code)
                if (target) target.monthlynew = val
            } else if (item.gubun === 'G1') {
                const target = matrix.value.find(r => r.stage === item.code)
                if (target) {
                    if (item.code1 === '100') target.impa = val
                    else if (item.code1 === '200') target.impb = val
                    else if (item.code1 === '300') target.impc = val
                    target.impsum = target.impa + target.impb + target.impc
                }
            } else if (item.gubun === 'G2') {
                const target = matrix.value.find(r => r.stage === item.code)
                if (target) {
                    if (item.code1 === '100') target.nochga = val
                    else if (item.code1 === '200') target.nochgb = val
                    else if (item.code1 === '300') target.nochgc = val
                    target.nochgsum = target.nochga + target.nochgb + target.nochgc
                }
            } else if (item.gubun === 'G3') {
                const target = matrix.value.find(r => r.stage === item.code)
                if (target) {
                    if (item.code1 === '100') target.nocnta = val
                    else if (item.code1 === '200') target.nocntb = val
                    else if (item.code1 === '300') target.nocntc = val
                    target.nocntsum = target.nocnta + target.nocntb + target.nocntc
                }
            } else if (item.gubun === 'G4') {
                if (item.code === '900') results.success = val
                else if (item.code === '910') results.fail = val
                else if (item.code === '930') results.abandon = val
                else if (item.code === '920') {
                    results.holdtotal += val
                    if (item.code1 === '100') results.holdover = val
                }
            } else if (item.gubun === 'G5') {
                if (item.code === '200') summary.visitcnt = val
                else if (item.code === '600') summary.selectioncnt = val
            } else if (item.gubun === 'G6') {
                if (item.code === '300') summary.monthlygoal = val
                else if (item.code === '200') summary.monthlyactual = val
            } else if (item.gubun === 'G7') {
                if (item.code === '200') summary.yearlygoal = val
                else if (item.code === '300') summary.yearlyactual = val
            }
        })

        // 3. 달성률 및 합계 계산
        if (summary.monthlygoal > 0) summary.monthlyrate = Math.round((summary.monthlyactual / summary.monthlygoal) * 100)

        Object.assign(total, { monthlynew: 0, totalholding: 0, impa: 0, impb: 0, impc: 0, impsum: 0, nochga: 0, nochgb: 0, nochgc: 0, nochgsum: 0, nocnta: 0, nocntb: 0, nocntc: 0, nocntsum: 0 })
        matrix.value.forEach(row => {
            total.monthlynew += row.monthlynew; total.totalholding += row.totalholding
            total.impa += row.impa; total.impb += row.impb; total.impc += row.impc; total.impsum += row.impsum
            total.nochga += row.nochga; total.nochgb += row.nochgb; total.nochgc += row.nochgc; total.nochgsum += row.nochgsum
            total.nocnta += row.nocnta; total.nocntb += row.nocntb; total.nocntc += row.nocntc; total.nocntsum += row.nocntsum
        })

		drillTitle.value = `영업 종합 리스트 (${filter.yyyy}-${filter.mm})`
	} catch (e) {
		vAlertError('조회 오류가 발생했습니다.')
	}
}

const drillDown = async (stage: string, type?: string) => {
	try {
		const res = await api.get('/hsaa/dashboard/list', {
			params: {
				yymm: `${filter.yyyy}${filter.mm}`,
				gubun: '200',
				code: stage,
				code1: type || '',
				userid: filter.userid === '000' ? '' : filter.userid
			}
		})
    const list = res.data || []
		detailGrid?.setData(list)
    masterListCount.value = list.length
		drillTitle.value = `상세: [단계:${stage}] [구분:${type || '결과'}]`
	} catch (e) {
		vAlertError('상세 조회 오류')
	}
}

const fetchAllDetailData = async (row: any) => {
  detailViewerRef.value?.loadData(row.salesid, row.custcd)
}

onMounted(async () => {
    try {
        const resUsers = await api.get('/hsaa/users')
        userData.value = resUsers.data || []
    } catch (e) {}

	detailGrid = new Tabulator(detailGridRef.value!, {
		layout: "fitColumns", selectable: 1, height: "100%",
		columns: [
			{ title: "거래처명", field: "custnm", widthGrow: 1.2, cssClass: "fw-bold" },
			{ title: "영업건명", field: "salestitle", widthGrow: 1.5, cssClass: "text-primary" }
		]
	})
  detailGrid.on("rowClick", (e, row) => fetchAllDetailData(row.getData()))

	fetchData()
})
</script>

<style scoped>
.erp-container { font-family: 'Pretendard', sans-serif; letter-spacing: -0.03rem; }

/* KPI Mini Card */
.kpi-mini-card { height: 54px; transition: transform 0.2s; cursor: pointer; }
.kpi-mini-card:hover { transform: translateY(-2px); }

/* Table Compact */
.erp-table-compact { border-collapse: collapse !important; font-size: 11px; }
.erp-table-compact th { background-color: #f8fafc !important; border: 1px solid #dee2e6 !important; padding: 4px !important; }
.erp-table-compact td { border: 1px solid #dee2e6 !important; padding: 3px !important; background-color: #fff !important; }
.sub-th th { font-weight: 500; color: #64748b; background-color: #fff !important; }

.clickable { cursor: pointer; text-decoration: underline; color: #0d6efd; }
.clickable:hover { color: #0a58ca; background-color: #f1f5f9 !important; }

.tabulator-instance { font-size: 11px; border: none; width: 100% !important; background-color: #fff; }

.erp-table-dense th { padding: 6px 8px; border: 1px solid #dee2e6; font-size: 11px; font-weight: 700; color: #555; background-color: #f8f9fa; }
.erp-table-dense td { padding: 3px 8px; border: 1px solid #dee2e6; background-color: #fff; vertical-align: middle; font-size: 11px; }

.nav-tabs .nav-link { color: #64748b; border-radius: 0; background: transparent !important; padding: 0.6rem 1.4rem !important; }
.nav-tabs .nav-link.active { color: #0d6efd !important; font-weight: 800 !important; border-bottom: 3px solid #0d6efd !important; }

:deep(.tabulator-row.tabulator-selected) { background-color: #eef6ff !important; color: #0d6efd !important; font-weight: bold; }
</style>
