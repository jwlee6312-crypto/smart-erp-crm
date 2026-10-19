<!--
	=============================================================
	프로그램명	: 영업담당자 변경 (HSAA300U)
	작성일자	: 2025.03.14
	작성자      : AI Assistant
	설명        : 영업건의 담당자를 일괄 변경/이관 처리 (표준 UI 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom px-2">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-person-gear me-2 text-danger" style="font-size: 18px;"></i>
        영업관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-danger fw-bolder">영업담당자 변경 (HSAA300U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchList">조회</button>
        <button class="btn-erp btn-save" @click="saveChange">일괄 변경 저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [상단] 조회 필터 (erp-table-dense 표준) -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 10%;" /><col style="width: 40%;" />
              <col style="width: 10%;" /><col style="width: 40%;" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light fw-bold">담당자(현)</th>
                <td>
                  <select v-model="filter.fromuserid" class="form-select form-select-sm" style="width: 150px;">
                    <option value="">전체</option>
                    <option v-for="u in salesmanList" :key="u.userid" :value="u.userid">{{ u.usernm }}</option>
                  </select>
                </td>
                <th class="text-center bg-light border-start fw-bold">거래처명</th>
                <td>
                  <input type="text" v-model="filter.custnm" class="form-control form-control-sm" style="width: 200px;" placeholder="검색어 입력" @keyup.enter="fetchList" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 메인 그리드 -->
      <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>영업건 이관 대상 목록</span>
          <div class="d-flex align-items-center gap-2 bg-warning bg-opacity-10 px-2 py-1 rounded">
             <span class="x-small fw-bold text-danger">선택대상 일괄지정(To):</span>
             <select v-model="batchtouserid" class="form-select form-select-sm py-0" style="width: 130px; height: 22px;">
                 <option value="">담당자 선택</option>
                 <option v-for="u in salesmanList" :key="u.userid" :value="u.userid">{{ u.usernm }}</option>
             </select>
             <button class="btn btn-xs btn-danger py-0 px-2 fw-bold" style="font-size: 10px;" @click="applyBatchUser">적용</button>
          </div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="gridRef" class="tabulator-instance flex-grow-1"></div>
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

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

import { api } from '@/utils/axios'

const filter = reactive({ fromuserid: '', custnm: '' })
const batchtouserid = ref('')
const salesmanList = ref<any[]>([])

const gridRef = ref(null); let grid: Tabulator | null = null

const initialize = () => { Object.assign(filter, { fromuserid: '', custnm: '' }); grid?.clearData(); }

const fetchList = async () => {
  try {
    const res = await api.get('/hsaa/transfer-list', {
      params: { userid: filter.fromuserid }
    })
    grid?.setData(res.data || [])
  } catch (e) {
    vAlertError('조회 실패')
  }
}

const applyBatchUser = () => {
    if(!batchtouserid.value) return vAlertError('담당자를 선택하세요.');
    grid?.getSelectedRows().forEach(row => row.update({ touserid: batchtouserid.value }));
}

const saveChange = async () => {
  const selectedData = grid?.getSelectedData() || []
  if (selectedData.length === 0) return vAlertError('변경할 항목을 선택하세요.')
  if (!batchtouserid.value) return vAlertError('변경 대상 담당자를 지정하세요.')

  try {
    await api.post('/hsaa/change-manager', {
      touserid: batchtouserid.value,
      chngreason: '영업담당자 일괄 변경',
      items: selectedData
    })
    vAlert('담당자 변경 처리가 완료되었습니다.')
    fetchList()
  } catch (e) {
    vAlertError('변경 처리 실패')
  }
}

onMounted(async () => {
  try {
    const res = await api.get('/hsaa/users')
    salesmanList.value = res.data || []

    const userMap: Record<string, string> = {}
    salesmanList.value.forEach(u => userMap[u.userid] = u.usernm)

    grid = new Tabulator(gridRef.value!, {
      layout: "fitColumns", selectable: true, height: "100%",
      columns: [
        { formatter: "rowSelection", titleFormatter: "rowSelection", width: 40, hozAlign: "center", headerSort: false },
        { title: "거래처명", field: "custnm", width: 200, cssClass: "fw-bold small" },
        { title: "영업건명", field: "salestitle", widthGrow: 1.5 },
        { title: "등록일", field: "addtime", width: 120, hozAlign: "center" },
        { title: "상태", field: "statenm", width: 100, hozAlign: "center" },
        { title: "현 담당자", field: "usernm", width: 120, hozAlign: "center", cssClass: "bg-light" },
        { title: "변경 담당자(To)", field: "touserid", width: 140, editor: "list", editorParams: { values: userMap }, cssClass: "bg-danger-subtle fw-bold" },
        { title: "비고", field: "remark", editor: "input", widthGrow: 1 }
      ]
    })
  } catch (e) {}
})
</script>

<style scoped>
.erp-container { font-family: 'Pretendard', sans-serif; }
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 12px; border: none; }
.btn-erp { height: 28px; padding: 0 12px; font-size: 12px; font-weight: 600; border-radius: 4px; border: 1px solid #dee2e6; background-color: #fff; }
.btn-search { background-color: #0d6efd !important; color: #fff !important; }
.btn-save { background-color: #198754 !important; color: #fff !important; }
.erp-table-dense th { padding: 6px 8px; border: 1px solid #dee2e6; font-size: 11px; font-weight: 600; background-color: #f8f9fa; }
.erp-table-dense td { padding: 3px 8px; border: 1px solid #dee2e6; }
</style>
