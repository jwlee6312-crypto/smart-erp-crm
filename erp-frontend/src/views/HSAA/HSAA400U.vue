<!--
	=============================================================
	프로그램명	: 영업활동 코칭등록 (HSAA400U)
	작성일자	: 2025.03.14
	작성자      : AI Assistant
	설명        : 영업 사원의 상담 보고를 확인하고 팀장이 코칭을 등록하는 화면
                (PHP 원본 소스 비즈니스 로직 완벽 반영)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-chat-right-quote-fill me-2 text-primary" style="font-size: 18px;"></i>
        영업활동관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">영업활동 코칭등록 (HSAA400U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchList">조회</button>
        <button class="btn-erp btn-excel" @click="exportExcel">엑셀 저장</button>
      </div>
    </div>

    <!-- 🔍 2. 검색 필터 -->
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

    <!-- 📊 3. 메인 레이아웃 영역 (좌: 목록 / 우: 코칭입력) -->
    <div class="flex-grow-1 p-2 bg-light overflow-hidden d-flex flex-row gap-2">
      <!-- (Left) 상담 내역 그리드 (유동적 확장) -->
      <div class="card border shadow-sm overflow-hidden d-flex flex-column flex-grow-1">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
            <span class="small fw-bold text-dark"><i class="bi bi-list-ul me-1 text-primary"></i> 상담 상세 목록</span>
            <div class="d-flex gap-3 x-small">
                <span class="text-primary fw-bold"><i class="bi bi-file-earmark-check-fill me-1"></i>보고요청</span>
                <span class="text-success fw-bold"><i class="bi bi-check-square-fill me-1"></i>코칭완료</span>
            </div>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="gridRef" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>

      <!-- (Right) 코칭 및 영업요약 입력 폼 (500px 고정) -->
      <div class="card border shadow-sm overflow-hidden d-flex flex-column bg-white" style="flex: 0 0 500px; width: 500px;">
        <div class="card-header bg-white border-bottom py-1 px-3 d-flex justify-content-between align-items-center">
            <span class="small fw-bold text-primary"><i class="bi bi-pencil-square me-2"></i>코칭 등록 및 정보 확인</span>
            <div class="btn-group-erp d-flex gap-1" v-if="currentRecord">
                <button class="btn-erp btn-save" @click="saveCoaching">저장</button>
            </div>
        </div>
        <div class="card-body p-3 overflow-auto scrollbar-sm" v-if="currentRecord">
            <!-- 요약 정보 -->
            <div class="alert alert-secondary py-1 x-small mb-3 d-flex justify-content-between border-0 shadow-xs">
                <span>대상: <strong class="text-primary">{{ currentRecord.custnm }}</strong></span>
                <span>상담일: <strong>{{ currentRecord.contdt }}</strong></span>
            </div>

            <!-- 상담 상세 원본 -->
            <div class="mb-3">
              <label class="small fw-bold text-muted mb-1"><i class="bi bi-journal-text me-1"></i>상담 원본 내용</label>
              <div class="p-2 border rounded bg-light bg-opacity-50 small scrollbar-sm overflow-auto" style="max-height: 300px; white-space: pre-wrap; word-break: break-all;">
                {{ currentRecord.diarycontent || '원본 내용이 없습니다.' }}
              </div>
            </div>

            <!-- 입력 영역 -->
            <table class="erp-table-dense w-100 mb-3">
                <colgroup><col style="width: 80px;" /><col /></colgroup>
                <tbody>
                    <tr>
                        <th class="bg-light text-muted">보고내용</th>
                        <td>
                            <div class="p-2 small fw-bold bg-light border rounded text-muted shadow-xs" style="min-height: 60px; white-space: pre-wrap; overflow-y: auto;">
                                {{ currentRecord.reportcontent || '요청된 보고 내용이 없습니다.' }}
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <th class="bg-light">코칭일자</th>
                        <td><input type="date" v-model="coachingForm.reportdt" class="form-control form-control-sm w-50 shadow-none" /></td>
                    </tr>
                    <tr>
                        <th class="bg-light fw-bold text-primary">상담코칭</th>
                        <td>
                            <textarea v-model="coachingForm.salescoaching" class="form-control border-primary-subtle small shadow-none" style="min-height: 200px; resize: vertical;" placeholder="영업사원에게 전달할 지시 및 코칭 내용을 입력하세요."></textarea>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
        <div class="card-body d-flex align-items-center justify-content-center bg-light bg-opacity-50" v-else>
            <div class="text-center text-muted">
                <i class="bi bi-cursor-fill fs-1 opacity-25 mb-2 d-block"></i>
                <div class="small fw-bold">좌측 목록에서 상담 건을 선택하세요.</div>
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
import Modal from '@/components/Modal.vue'
import DateForm from '@/components/DateForm.vue'
import { getDate } from '@/composables/useDate'
import { api } from '@/utils/axios'

const { firstDay, today } = getDate()
const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

const filter = reactive({ sdate: firstDay, edate: today, custnm: '', userid: '' })
const salesmanList = ref<any[]>([])

const currentRecord = ref<any>(null)
const coachingForm = reactive({
    reportdt: today,
    salescoaching: ''
})

const gridRef = ref(null); let grid: Tabulator | null = null

const initialize = () => {
  filter.sdate = firstDay
  filter.edate = today
  filter.custnm = ''
  filter.userid = ''
  currentRecord.value = null
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
        page: 1, limit: 200
      }
    })
    grid?.setData(res.data.list || [])
  } catch (e) {
    vAlertError('조회 실패')
  }
}

const saveCoaching = async () => {
  if (!currentRecord.value) return;

  try {
    const payload = {
      salesid: currentRecord.value.salesid,
      ser: currentRecord.value.ser,
      contdt: currentRecord.value.contdt?.replace(/-/g, ''), // 상담일자 (PK)
      reportdt: coachingForm.reportdt.replace(/-/g, ''),
      salescoaching: coachingForm.salescoaching
    }

    if (!payload.salesid || !payload.ser || !payload.contdt) {
        return vAlertError('필수 정보(영업ID, 순번 등)가 누락되었습니다.');
    }

    await api.post('/hsaa/consultations/coaching', payload)
    vAlert('✅ 코칭 내용이 저장되었습니다.');
    fetchList()
  } catch (e: any) {
    console.error('코칭 저장 실패:', e);
    vAlertError('저장 중 오류가 발생했습니다.');
  }
}

const exportExcel = () => {
  grid?.download("xlsx", `영업활동코칭_${today}.xlsx`)
}

onMounted(async () => {
  // 기초 데이터 로드
  try {
    const resUsers = await api.get('/hsaa/users')
    salesmanList.value = resUsers.data || []
  } catch (e) {}

  // 그리드 초기화
  grid = new Tabulator(gridRef.value!, {
    layout: "fitColumns", height: "100%",
    placeholder: "조회된 상담 내역이 없습니다.",
    rowFormatter: (row) => {
        const data = row.getData();
        if (data.reportyn === 'Y') {
            row.getElement().style.color = "#0d6efd"; // 확인요청건 파란색
            row.getElement().style.fontWeight = "bold";
        }
    },
    columns: [
      { title: "거래처명", field: "custnm", width: 220, cssClass: "fw-bold small" },
      { title: "상담일시", field: "contdt", width: 110, hozAlign: "center", formatter: (c) => {
          const v = c.getValue();
          return v && v.length === 8 ? `${v.substring(0,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : v;
      }},
      { title: "채널", field: "channelnm", width: 100, hozAlign: "center" },
      { title: "담당자", field: "usernm", width: 100, hozAlign: "center" },
      { title: "상태", field: "statenm", width: 100, hozAlign: "center" },
      { title: "보고", field: "reportyn", width: 100, hozAlign: "center",
        formatter: (c) => c.getValue() === 'Y' ? '<i class="bi bi-file-earmark-check-fill text-primary"></i>' : ''
      },
      { title: "확인", field: "coachingreadyn", width: 100, hozAlign: "center",
        formatter: (c) => c.getValue() === 'Y' ? '<i class="bi bi-check-square-fill text-success"></i>' : ''
      },
      { title: "상담내용", field: "diarycontent", widthGrow: 2, tooltip: true, formatter: (cell: any) => {
          const val = cell.getValue() || cell.getData().content || '';
          return val.replace(/\r\n|\n/g, ' ').substring(0, 100) + (val.length > 100 ? '...' : '');
      }}
    ]
  })

  // 그리드 행 클릭 이벤트 (팝업 대신 우측 폼 연동)
  grid.on("rowClick", (e, row) => {
      const rowData = row.getData();
      currentRecord.value = { ...rowData };

      // 날짜 포맷팅 (YYYYMMDD -> YYYY-MM-DD)
      if (rowData.contdt && rowData.contdt.length === 8) {
          currentRecord.value.contdt = `${rowData.contdt.substring(0,4)}-${rowData.contdt.substring(4,6)}-${rowData.contdt.substring(6,8)}`;
      }

      // 폼 데이터 바인딩
      coachingForm.reportdt = today;
      coachingForm.salescoaching = rowData.salescoaching || '';
  })

  fetchList()
})
</script>

<style scoped>
.erp-container { font-family: 'Pretendard', sans-serif; letter-spacing: -0.02rem; }
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 12px; border: none; }
.btn-erp { height: 28px; padding: 0 12px; font-size: 12px; font-weight: 600; border-radius: 4px; border: 1px solid #dee2e6; background-color: #fff; color: #444; }
.btn-search { background-color: #0d6efd !important; color: #fff !important; border-color: #0d6efd !important; }
.btn-save { background-color: #198754 !important; color: #fff !important; border-color: #198754 !important; }
.erp-table-dense th { padding: 6px 8px; border: 1px solid #dee2e6; font-size: 11px; font-weight: 700; background-color: #f8f9fa; }
.erp-table-dense td { padding: 3px 8px; border: 1px solid #dee2e6; background-color: #fff; }

.shadow-xs { box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05); }

/* 스크롤바 스타일 */
.scrollbar-sm::-webkit-scrollbar { width: 4px; height: 4px; }
.scrollbar-sm::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
.scrollbar-sm::-webkit-scrollbar-track { background: #f1f5f9; }

:deep(.tabulator-row.tabulator-selected) { background-color: #eef6ff !important; }
</style>
