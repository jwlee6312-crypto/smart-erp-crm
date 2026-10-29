<!--
	=============================================================
	프로그램명	: 영업활동 방문계획 자동수립 (HSAA700U)
	작성일자	: 2026.10.28
	설명        : 영업건 중요도(IMPORT), 확도(SUCCRATE), 최종접촉일(LASTMTDT), 방문주기, 공휴일(PD_HOLIDAY), 위·경도 동선 최적화 기반 자동 방문 계획 수립 및 캘린더 확정
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-map me-2 text-primary" style="font-size: 18px;"></i>
        영업활동관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">영업활동 방문계획 자동수립 (HSAA700U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchPlan">계획조회</button>
        <button class="btn-erp btn-auto" @click="generateAutoPlan">자동생성(A)</button>
        <button class="btn-erp btn-save" @click="confirmAndSync">일정확정(S)</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [상단] 조건 설정 영역 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 10%;" /><col style="width: 20%;" />
              <col style="width: 10%;" /><col style="width: 20%;" />
              <col style="width: 10%;" /><col style="width: 30%;" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light">기준년월</th>
                <td>
                  <input v-model="filter.ym" type="text" class="form-control form-control-sm" placeholder="YYYYMM" maxlength="6" />
                </td>
                <th class="text-center bg-light border-start">영업사원</th>
                <td>
                  <select v-model="filter.userid" class="form-select form-select-sm">
                    <option v-for="user in salesmanList" :key="user.userid" :value="user.userid">{{ user.usernm }}</option>
                  </select>
                </td>
                <th class="text-center bg-light border-start">일일 방문 CAPA</th>
                <td>
                  <div class="d-flex align-items-center gap-2">
                    <input v-model.number="filter.dailyCapa" type="number" class="form-control form-control-sm" style="width: 80px;" min="1" max="10" />
                    <span class="text-muted x-small">(일일 최대 방문 건수)</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [중앙] 자동 수립된 방문 계획 그리드 -->
      <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark"><i class="bi bi-calendar-check me-2 text-primary"></i>스마트 방문 계획 스케줄</span>
          <span class="x-small text-muted fst-italic">* 영업건 중요도(IMPORT), 확도(SUCCRATE), 최종접촉일(LASTMTDT), 공휴일(PD_HOLIDAY), 위·경도 동선 기반 자동 산출 결과입니다.</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="gridRef" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>

      <!-- 안내 가이드 -->
      <div class="p-1 text-center text-muted x-small opacity-75">
        <i class="bi bi-info-circle me-1"></i> [자동생성] 버튼을 누르면 조건에 맞는 미팅 일정이 최적의 동선과 주기로 자동 배치되며, [일정확정] 시 캘린더에 반영됩니다.
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

const authstore = useAuthStore()
const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

const filter = reactive({
  ym: new Date().toISOString().slice(0, 7).replace('-', ''),
  userid: authstore.userid || '',
  dailyCapa: 4
})

const salesmanList = ref<any[]>([])
const gridRef = ref<HTMLDivElement | null>(null)
let grid: Tabulator | null = null

const initialize = () => {
  grid?.clearData()
  vAlert('초기화되었습니다.')
}

const fetchPlan = async () => {
  try {
    const res = await api.get('/hsaa/visit-plan', {
      params: { ym: filter.ym, userid: filter.userid }
    })
    grid?.setData(res.data || [])
    vAlert('방문 계획을 조회했습니다.')
  } catch (e) {
    vAlertError('조회 중 오류 발생')
  }
}

const generateAutoPlan = async () => {
  if (!confirm('설정된 조건(중요도, 확도, 최종접촉일, 공휴일, 위경도 동선)을 기반으로 방문 계획을 자동 수립하시겠습니까?')) return
  try {
    const res = await api.post('/hsaa/visit-plan/generate', {
      ym: filter.ym,
      userid: filter.userid,
      dailyCapa: filter.dailyCapa
    })
    grid?.setData(res.data || [])
    vAlert('스마트 방문 계획이 성공적으로 자동 수립되었습니다.')
  } catch (e) {
    vAlertError('자동 계획 수립 실패')
  }
}

const confirmAndSync = async () => {
  if (!confirm('현재 수립된 방문 계획을 개인 일정 및 캘린더에 최종 확정 반영하시겠습니까?')) return
  try {
    await api.post('/hsaa/visit-plan/confirm', {
      userid: filter.userid,
      plans: grid?.getData()
    })
    vAlert('방문 계획이 성공적으로 확정 및 캘린더에 연동되었습니다.')
  } catch (e) {
    vAlertError('일정 확정 실패')
  }
}

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
      { title: "방문예정일", field: "plan_dt", width: 110, hozAlign: "center", cssClass: "fw-bold text-primary" },
      { title: "거래처명", field: "custnm", width: 160, hozAlign: "left" },
      { title: "영업건명", field: "deal_title", minWidth: 200, hozAlign: "left" },
      { title: "중요도(등급)", field: "import_nm", width: 100, hozAlign: "center" },
      { title: "확도(%)", field: "succrate", width: 80, hozAlign: "center" },
      { title: "최종접촉일", field: "lastmtdt", width: 110, hozAlign: "center" },
      { title: "주소", field: "address", minWidth: 250, hozAlign: "left" }
    ]
  })
  fetchPlan()
})
</script>

<style scoped>
.erp-container { font-family: 'Pretendard', sans-serif; letter-spacing: -0.03rem; }
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 13px; }
.x-small { font-size: 11px; }

.erp-header { height: 48px; padding: 0 10px; background-color: #fff; }
.btn-erp { height: 28px; padding: 0 12px; font-size: 12px; font-weight: 600; border-radius: 4px; transition: all 0.2s; border: 1px solid #dee2e6; background-color: #fff; color: #444; }
.btn-erp:hover { background-color: #f8f9fa; border-color: #ced4da; }
.btn-init { color: #6c757d; }
.btn-search { background-color: #0d6efd; border-color: #0d6efd; color: #fff; }
.btn-search:hover { background-color: #0b5ed7; border-color: #0a58ca; color: #fff; }
.btn-auto { background-color: #fd7e14; border-color: #fd7e14; color: #fff; }
.btn-auto:hover { background-color: #e8590c; border-color: #d9480f; color: #fff; }
.btn-save { background-color: #198754; border-color: #198754; color: #fff; }
.btn-save:hover { background-color: #157347; border-color: #146c43; color: #fff; }

.erp-table-dense th { padding: 8px; border: 1px solid #dee2e6; font-size: 12px; font-weight: 600; color: #555; }
.erp-table-dense td { padding: 4px 8px; border: 1px solid #dee2e6; }
:deep(.tabulator-row.tabulator-selectable:hover) { background-color: #f8f9fa !important; }
</style>
