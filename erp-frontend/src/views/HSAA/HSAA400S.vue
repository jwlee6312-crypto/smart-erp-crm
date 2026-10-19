<!--
	=============================================================
	프로그램명	: 기간별 영업상담 내역 (HSAA400S)
	작성일자	: 2025.03.14
	작성자      : AI Assistant
	설명        : 특정 기간 동안의 영업 상담 내역 조회 및 영업 코칭 (표준 디자인 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-chat-left-dots-fill me-2 text-primary" style="font-size: 18px;"></i>
        영업활동관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">기간별 영업상담 내역 (HSAA400S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchList">조회</button>
        <button class="btn-erp btn-excel" @click="exportExcel">엑셀 저장</button>
      </div>
    </div>

    <!-- 🔍 2. 검색 필터 (균등 배분 표준) -->
    <div class="p-2 bg-light border-bottom flex-shrink-0">
      <div class="card border shadow-sm">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 10%;" /><col style="width: 23%;" />
              <col style="width: 10%;" /><col style="width: 23%;" />
              <col style="width: 10%;" /><col style="width: 24%;" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light fw-bold">상담일자</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <DateForm v-model:fromdt="filter.sdate" v-model:todt="filter.edate" />
                </td>
                <th class="text-center bg-light border-start fw-bold">거래처명</th>
                <td><input v-model="filter.custnm" class="form-control form-control-sm" placeholder="검색어 입력" @keyup.enter="fetchList" /></td>
                <th class="text-center bg-light border-start fw-bold">영업담당</th>
                <td>
                  <select v-model="filter.userid" class="form-select form-select-sm" style="width: 150px;">
                    <option value="">전체</option>
                    <option v-for="user in salesmanList" :key="user.userid" :value="user.userid">{{ user.usernm }}</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 📊 3. 상담 내역 그리드 영역 -->
    <div class="flex-grow-1 p-2 bg-light overflow-hidden d-flex flex-column">
      <div class="card flex-grow-1 border shadow-sm overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom small fw-bold text-dark"><i class="bi bi-list-ul me-1"></i> 상담 상세 목록</div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="gridRef" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>
    </div>

    <!-- 📑 4. 영업코칭 모달 (표준 스타일 유지) -->
    <Modal v-model:visible="coachingVisible" title="영업코칭 및 피드백 등록" width="650px">
        <div class="p-3">
            <div class="alert alert-info py-2 x-small mb-2 d-flex justify-content-between">
                <span>대상: <strong>{{ currentRecord?.custnm }}</strong></span>
                <span>담당: <strong>{{ currentRecord?.usernm }}</strong></span>
            </div>

            <!-- [추가] 상담 원본 본문 확인 영역 -->
            <div class="mb-3">
              <label class="small fw-bold text-muted mb-1"><i class="bi bi-journal-text me-1"></i>상담 원본 내용</label>
              <div class="p-2 border rounded bg-light bg-opacity-50 small scrollbar-sm overflow-auto" style="max-height: 250px; white-space: pre-wrap; word-break: break-all;">
                {{ currentRecord?.diarycontent || currentRecord?.content || '원본 내용이 없습니다.' }}
              </div>
            </div>

            <table class="erp-table-dense w-100">
                <colgroup><col style="width: 100px;" /><col /><col style="width: 100px;" /><col /></colgroup>
                <tbody>
                    <tr><th class="bg-light">보고분류</th><td><select v-model="coachingForm.reportcd" class="form-select"><option value="10">정기</option></select></td><th class="bg-light border-start">코칭일자</th><td><input type="date" v-model="coachingForm.reportdt" class="form-control" /></td></tr>
                    <tr><th class="bg-light">코칭내용</th><td colspan="3"><textarea v-model="coachingForm.salescoaching" class="form-control" rows="5" placeholder="코칭 및 피드백 내용을 입력하세요."></textarea></td></tr>
                </tbody>
            </table>
            <div class="text-end mt-3"><button class="btn btn-primary btn-sm px-4 fw-bold" @click="saveCoaching">코칭 저장</button></div>
        </div>
    </Modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import DateForm from '@/components/DateForm.vue'
import { getDate } from '@/composables/useDate'

const { firstDay, today } = getDate()
const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

import { api } from '@/utils/axios'

const filter = reactive({ sdate: firstDay, edate: today, custnm: '', userid: '' })
const salesmanList = ref<any[]>([])
const coachingVisible = ref(false)
const currentRecord = ref<any>(null)
const coachingForm = reactive({ reportcd: '10', reportdt: today, salescoaching: '' })

const gridRef = ref(null); let grid: Tabulator | null = null

const initialize = () => {
  filter.sdate = firstDay
  filter.edate = today
  filter.custnm = ''
  filter.userid = ''
  grid?.clearData()
}

const fetchList = async () => {
  try {
    const res = await api.get('/hsaa/consultations/list', {
      params: {
        sdate: filter.sdate.replace(/-/g, ''),
        edate: filter.edate.replace(/-/g, ''),
        schcustnm: filter.custnm,
        userid: filter.userid,
        page: 1, limit: 100 // 페이징 간소화 구현
      }
    })
    grid?.setData(res.data.list || [])
  } catch (e) {
    vAlertError('조회 실패')
  }
}

const saveCoaching = async () => {
  try {
    await api.post('/hsaa/consultations/coaching', {
      salesid: currentRecord.value.salesid,
      ser: currentRecord.value.ser,
      contdt: currentRecord.value.contdt, // 상담일자 추가 (PK)
      ...coachingForm,
      reportdt: coachingForm.reportdt.replace(/-/g, '')
    })
    coachingVisible.value = false;
    vAlert('코칭 저장 완료');
    fetchList()
  } catch (e) {
    vAlertError('저장 실패')
  }
}

const exportExcel = () => {
  grid?.download("xlsx", `영업상담내역_${today}.xlsx`)
}

onMounted(async () => {
  try {
    const res = await api.get('/hsaa/users')
    salesmanList.value = res.data || []
  } catch (e) {}
  grid = new Tabulator(gridRef.value!, {
    layout: "fitColumns", height: "100%",
    columns: [
      { title: "거래처명", field: "custnm", width: 180, cssClass: "fw-bold small text-primary" },
      { title: "상담일시", field: "contdt", width: 110, hozAlign: "center" },
      { title: "채널", field: "channelnm", width: 90, hozAlign: "center" },
      { title: "담당자", field: "usernm", width: 90, hozAlign: "center" },
      { title: "상태", field: "statenm", width: 90, hozAlign: "center" },
      { title: "상담내용", field: "diarycontent", widthGrow: 2, tooltip: true, formatter: (cell: any) => {
          // [개선] 줄바꿈을 공백으로 치환하여 한 줄로 표시
          const val = cell.getValue() || cell.getData().content || '';
          return val.replace(/\r\n|\n/g, ' ').substring(0, 100) + (val.length > 100 ? '...' : '');
      }},
      { title: "코칭", width: 80, hozAlign: "center", headerSort: false,
        formatter: () => '<button class="btn btn-xs btn-outline-dark py-0 px-2 shadow-none">코칭</button>',
        cellClick: (e, cell) => {
          const rowData = cell.getData();
          currentRecord.value = rowData;
          coachingForm.reportcd = rowData.reportcd || '10';
          coachingForm.reportdt = today;
          coachingForm.salescoaching = rowData.salescoaching || ''; // 기존 코칭 내용 로드
          coachingVisible.value = true;
      }}
    ]
  })
  fetchList()
})
</script>

<style scoped>
.erp-container { font-family: 'Pretendard', sans-serif; letter-spacing: -0.02rem; }
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 12px; border: none; }
.btn-erp { height: 28px; padding: 0 12px; font-size: 12px; font-weight: 600; border-radius: 4px; border: 1px solid #dee2e6; background-color: #fff; }
.btn-search { background-color: #0d6efd !important; color: #fff !important; }
.erp-table-dense th { padding: 6px 8px; border: 1px solid #dee2e6; font-size: 11px; font-weight: 700; background-color: #f8f9fa; }
.erp-table-dense td { padding: 3px 8px; border: 1px solid #dee2e6; background-color: #fff; }

/* 스크롤바 스타일 */
.scrollbar-sm::-webkit-scrollbar { width: 4px; height: 4px; }
.scrollbar-sm::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
.scrollbar-sm::-webkit-scrollbar-track { background: #f1f5f9; }
</style>
