<!--
	=============================================================
	프로그램명	: 영업상담이관 (HSAA200U)
	작성일자	: 2025.03.14
	작성자      : AI Assistant
	설명        : 영업상담 건을 담당자에게 배정하거나 배정 포기 처리 (표준 UI 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom px-2">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-arrow-left-right me-2 text-warning" style="font-size: 18px;"></i>
        영업관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-warning fw-bolder">영업상담이관 (HSAA200U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchList">조회</button>
        <button class="btn-erp btn-save" @click="saveTransfer">일괄 이관/저장</button>
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
                <th class="text-center bg-light fw-bold">상담기간</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <DateForm v-model:fromdt="filter.sdate" v-model:todt="filter.edate" />
                </td>
                <th class="text-center bg-light border-start fw-bold">이관구분</th>
                <td>
                  <select v-model="filter.gubun" class="form-select form-select-sm" style="width: 150px;">
                    <option value="">전체</option>
                    <option value="N">이관 미결건</option>
                    <option value="Y">이관 완료건</option>
                    <option value="X">배정 포기건</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 메인 그리드 -->
      <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden">
        <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark d-flex justify-content-between align-items-center">
            <span><i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>이관 대기 상담 목록</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="gridRef" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>
    </div>

    <!-- 녹취 재생 오버레이 (디자인 스켈레톤) -->
    <div v-if="audioVisible" class="audio-overlay" @click.self="audioVisible = false">
      <div class="card shadow-lg p-3" style="width: 350px;">
        <div class="d-flex justify-content-between align-items-center mb-2">
          <span class="fw-bold small"><i class="bi bi-mic-fill me-1 text-danger"></i>녹취 재생</span>
          <button class="btn-close" @click="audioVisible = false"></button>
        </div>
        <audio controls autoplay class="w-100"></audio>
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
import DateForm from '@/components/DateForm.vue'
import { getDate } from '@/composables/useDate'

const { firstDay, today } = getDate()
const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

import { api } from '@/utils/axios'

const filter = reactive({ sdate: firstDay, edate: today, gubun: 'N', userid: '' })
const salesmanList = ref<any[]>([])
const audioVisible = ref(false)
const gridRef = ref(null); let grid: Tabulator | null = null

const initialize = () => { Object.assign(filter, { sdate: firstDay, edate: today, gubun: 'N', userid: '' }); grid?.clearData(); }

const fetchList = async () => {
  try {
    const res = await api.get('/hsaa/calls', {
      params: {
        sdate: filter.sdate.replace(/-/g, ''),
        edate: filter.edate.replace(/-/g, ''),
        gubun: filter.gubun,
        userid: filter.userid
      }
    })
    grid?.setData(res.data || [])
  } catch (e) {
    vAlertError('조회 실패')
  }
}

const saveTransfer = async () => {
  const selectedData = grid?.getSelectedData() || []
  if (selectedData.length === 0) return vAlertError('이관할 항목을 선택하세요.')

  try {
    await api.post('/hsaa/transfer', selectedData)
    vAlert('이관 처리가 완료되었습니다.')
    fetchList()
  } catch (e) {
    vAlertError('이관 처리 실패')
  }
}

onMounted(async () => {
  try {
    const res = await api.get('/hsaa/users')
    salesmanList.value = res.data || []

    // 그리드 에디터 옵션 업데이트를 위해 salesmanList를 Map으로 변환
    const userMap: Record<string, string> = {}
    salesmanList.value.forEach(u => userMap[u.userid] = u.usernm)

    grid = new Tabulator(gridRef.value!, {
      layout: "fitColumns", selectable: true, height: "100%",
      columns: [
        { formatter: "rowSelection", titleFormatter: "rowSelection", width: 40, hozAlign: "center", headerSort: false },
        { title: "거래처명", field: "custnm", width: 150, cssClass: "fw-bold small" },
        { title: "전화번호", field: "call_telno", width: 120, hozAlign: "center" },
        { title: "상담일자", field: "svcymd", width: 100, hozAlign: "center", cssClass: "x-small" },
        { title: "상담원", field: "consultnm", width: 80, hozAlign: "center" },
        { title: "현담당자", field: "feedback_userNM", width: 100, hozAlign: "center" },
        { title: "문의내용", field: "trb_ment", widthGrow: 2 },
        { title: "녹취", field: "svcno", width: 60, hozAlign: "center", formatter: () => '<i class="bi bi-play-circle text-danger clickable fs-5"></i>', cellClick: () => audioVisible.value = true },
        { title: "담당지정", field: "userid", width: 130, editor: "list", editorParams: { values: userMap } },
        { title: "포기", field: "abandonyn", width: 60, hozAlign: "center", formatter: "tickCross", editor: "tickCross", editorParams: { trueValue: "X", falseValue: "N" } }
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
.audio-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1050; display: flex; align-items: center; justify-content: center; }
</style>
