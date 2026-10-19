<!--
	=============================================================
	프로그램명	: 영업활동 목표관리 (HSAA600U)
	작성일자	: 2025.03.14
	작성자      : AI Assistant
	설명        : 영업사원별 연간/월간 활동 및 매출 목표 설정 (HSOD100U 표준 디자인 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-bullseye me-2 text-primary" style="font-size: 18px;"></i>
        영업활동관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">영업활동 목표관리 (HSAA600U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchGoals">조회</button>
        <button class="btn-erp btn-save" @click="saveGoals">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [상단] 조회 필터 영역 (erp-table-dense 표준) -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 10%;" /><col style="width: 40%;" />
              <col style="width: 10%;" /><col style="width: 40%;" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light">목표년도</th>
                <td>
                  <select v-model="filter.yyyy" class="form-select form-select-sm" style="width: 120px;">
                    <option v-for="y in yyyyOptions" :key="y" :value="y">{{ y }}년</option>
                  </select>
                </td>
                <th class="text-center bg-light border-start">영업사원</th>
                <td>
                  <select v-model="filter.userid" class="form-select form-select-sm" style="width: 180px;">
                    <option value="">전체 (요약)</option>
                    <option v-for="user in salesmanList" :key="user.userid" :value="user.userid">{{ user.usernm }}</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [중앙] 목표 설정 그리드 영역 (전체폭 확장) -->
      <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>연간 월별 목표 리스트</span>
          <span class="x-small text-muted fst-italic">* 각 항목을 클릭하여 목표치를 직접 수정할 수 있습니다.</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="gridRef" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>

      <!-- 정보 안내 가이드 -->
      <div class="p-1 text-center text-muted x-small opacity-75">
        <i class="bi bi-info-circle me-1"></i> 영업상담 및 계약 건수 목표를 월별로 등록하고 관리할 수 있습니다. (금액 단위: 원 / 건수 단위: 건)
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

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

import { api } from '@/utils/axios'

// 📋 상태 데이터
const filter = reactive({
  yyyy: new Date().getFullYear(),
  userid: ''
})

const yyyyOptions = [2026, 2025, 2024, 2023]
const salesmanList = ref<any[]>([])

// 📊 그리드 인스턴스
const gridRef = ref<HTMLDivElement | null>(null)
let grid: Tabulator | null = null

const initialize = () => {
  filter.userid = ''
  grid?.clearData()
  vAlert('초기화되었습니다.')
}

const fetchGoals = async () => {
  try {
    const res = await api.get('/hsaa/targets', {
      params: { yyyy: filter.yyyy, userid: filter.userid }
    })
    grid?.setData(res.data || [])
    vAlert('목표 데이터를 조회합니다.')
  } catch (e) {
    vAlertError('조회 중 오류 발생')
  }
}

const saveGoals = async () => {
  try {
    await api.post('/hsaa/targets/save', {
      userid: filter.userid,
      targets: grid?.getData()
    })
    vAlert('설정된 모든 영업 목표가 저장되었습니다.')
  } catch (e) {
    vAlertError('저장 실패')
  }
}

// 🏆 그리드 초기화
onMounted(async () => {
  try {
    const res = await api.get('/hsaa/users')
    salesmanList.value = res.data || []
  } catch (e) {}
  grid = new Tabulator(gridRef.value!, {
    layout: "fitColumns",
    height: "100%",
    headerSort: false,
    columnDefaults: {
      headerHozAlign: 'center',
      vertAlign: "middle"
    },
    columns: [
      { title: "구분(월)", field: "mm", width: 120, hozAlign: "center", cssClass: "bg-light fw-bold" },
      {
        title: "매출목표(원)", field: "planamt", hozAlign: "right", editor: "number",
        formatter: "money", formatterParams: { precision: 0 },
        bottomCalc: "sum", bottomCalcFormatter: "money", bottomCalcFormatterParams: { precision: 0 },
        cssClass: "text-dark"
      },
      {
        title: "계약목표(건)", field: "contactcnt", hozAlign: "center", editor: "number",
        bottomCalc: "sum",
        cssClass: "text-primary"
      },
      {
        title: "방문목표(건)", field: "consultcnt", hozAlign: "center", editor: "number",
        bottomCalc: "sum",
        cssClass: "text-primary"
      },
      {
        title: "선정목표(건)", field: "salesidcnt", hozAlign: "center", editor: "number",
        bottomCalc: "sum",
        cssClass: "text-primary"
      }
    ]
  })
  fetchGoals()
})
</script>

<style scoped>
.erp-container { font-family: 'Pretendard', sans-serif; letter-spacing: -0.03rem; }
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 13px; }
.x-small { font-size: 11px; }

/* HSOD100U 호환 스타일 */
.erp-header { height: 48px; padding: 0 10px; background-color: #fff; }
.btn-erp { height: 28px; padding: 0 12px; font-size: 12px; font-weight: 600; border-radius: 4px; transition: all 0.2s; border: 1px solid #dee2e6; background-color: #fff; color: #444; }
.btn-erp:hover { background-color: #f8f9fa; border-color: #ced4da; }
.btn-init { color: #6c757d; }
.btn-search { background-color: #0d6efd; border-color: #0d6efd; color: #fff; }
.btn-search:hover { background-color: #0b5ed7; border-color: #0a58ca; color: #fff; }
.btn-save { background-color: #198754; border-color: #198754; color: #fff; }
.btn-save:hover { background-color: #157347; border-color: #146c43; color: #fff; }

.erp-table-dense th { padding: 8px; border: 1px solid #dee2e6; font-size: 12px; font-weight: 600; color: #555; }
.erp-table-dense td { padding: 4px 8px; border: 1px solid #dee2e6; }

/* 합계 행 강조 */
:deep(.tabulator-footer .tabulator-calcs-holder) { background: #fef2f2 !important; font-weight: 800; color: #dc3545 !important; border-top: 2px solid #dc3545 !important; }
:deep(.tabulator-row.tabulator-selectable:hover) { background-color: #f8f9fa !important; }
</style>
