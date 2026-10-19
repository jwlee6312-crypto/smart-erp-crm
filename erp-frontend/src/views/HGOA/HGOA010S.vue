<!--
	=============================================================
	프로그램명	  : 캠페인 상담현황 (HGOA010S - 소문자 표준 적용)
    프로그램 ID	: HGOA010S
	작성일자	    : 2026.05.19
	작성자	      : AI Assistant
	Description	: 캠페인별 통계 및 상세 상담 이력 조회 (최종 안정화 마감 버전)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="hgoa-status-wrapper d-flex flex-column h-100 bg-light overflow-hidden text-start">
    <!-- 1. 상단 조회 바 -->
    <header class="search-header bg-white border-bottom p-3 shadow-sm flex-shrink-0">
      <div class="row g-3 align-items-center">
        <div class="col-auto d-flex align-items-center gap-2">
          <i class="bi bi-calendar3 text-primary"></i>
          <label class="fw-bold small mb-0">실행일자</label>
          <DateForm v-model:fromdt="searchForm.fromdt" v-model:todt="searchForm.todt" />
        </div>
        <div class="col-auto">
          <div class="input-group input-group-sm">
            <span class="input-group-text bg-light border-end-0"><i class="bi bi-search"></i></span>
            <input type="text" v-model="searchForm.campnm" class="form-control border-start-0" placeholder="캠페인명 검색" @keyup.enter="handleSearch" />
          </div>
        </div>
        <div class="col-auto ms-auto">
          <button class="btn btn-dark btn-sm fw-bold px-4 shadow-sm" @click="handleSearch">
            <i class="bi bi-search me-1"></i>조회
          </button>
          <button class="btn btn-outline-success btn-sm fw-bold px-3 shadow-sm ms-1" @click="exportExcel">
            <i class="bi bi-file-earmark-excel me-1"></i>Excel
          </button>
        </div>
      </div>
    </header>

    <div class="flex-grow-1 d-flex overflow-hidden p-2 gap-2">
      <!-- [좌측] 캠페인 목록 -->
      <aside class="col-md-2 d-flex flex-column bg-white border rounded shadow-sm overflow-hidden">
        <div class="card-header bg-light py-2 px-3 fw-bold small border-bottom text-start">캠페인 목록</div>
        <div class="flex-grow-1 overflow-hidden position-relative">
          <div ref="campTableRef" class="h-100"></div>
        </div>
      </aside>

      <!-- [우측] 요약 및 상세/설문 -->
      <div class="col-md-10 d-flex flex-column gap-2 overflow-hidden">
        <!-- 상단: 배지형 요약 통계 -->
        <section class="card border-0 shadow-sm flex-shrink-0 overflow-hidden" style="height: 60px;">
          <div class="card-body p-0 px-3 d-flex align-items-center gap-3 h-100 bg-white">
            <div class="fw-bold text-primary small border-end pe-3 flex-shrink-0">캠페인 실시간 통계</div>
            <div v-if="selectedCamp" class="stats-group d-flex gap-2 align-items-center flex-grow-1 overflow-auto">
              <div class="stat-badge-mini pointer" :class="{active: currentFilter === ''}" @click="applyFilter('')">
                전체 {{ stats.tot_cnt || 0 }}
              </div>
              <div v-for="item in detail_stats" :key="item.rslt_cd"
                   class="stat-badge-mini pointer" :class="{active: currentFilter === item.rslt_cd}" @click="applyFilter(item.rslt_cd)">
                {{ item.rslt_nm }} {{ item.cnt }}
              </div>
            </div>
            <div v-else class="text-muted small italic">캠페인을 선택하세요.</div>
          </div>
        </section>

        <!-- 하단: 상세 내역 & 설문 결과 -->
        <div class="flex-grow-1 d-flex gap-2 overflow-hidden">
          <!-- 상세 상담 내역 -->
          <section class="card border-0 shadow-sm overflow-hidden flex-grow-1 d-flex flex-column border-top border-4 border-dark" style="flex-basis: 65%;">
            <div class="card-header bg-white py-2 px-3 border-bottom fw-bold small text-success text-start">상세 상담 내역</div>
            <div class="card-body p-0 flex-grow-1 position-relative bg-white">
              <div ref="detailTableRef" class="h-100"></div>
            </div>
          </section>

          <!-- 설문 결과 상세 -->
          <section class="card border-0 shadow-sm overflow-hidden d-flex flex-column border-top border-4 border-info" style="flex-basis: 35%;">
            <div class="card-header bg-white py-2 px-3 border-bottom fw-bold small text-info text-start">설문 상세 결과</div>
            <div class="card-body p-0 flex-grow-1 position-relative bg-white">
              <div ref="surveyTableRef" class="h-100"></div>
            </div>
          </section>
        </div>
      </div>
    </div>

    <!-- 채팅 상세 팝업 -->
    <div v-if="showChatModal" class="modal-overlay d-flex justify-content-center align-items-center" @click.self="showChatModal = false">
      <div class="modal-content card shadow-lg" style="width: 550px; max-height: 80vh;">
        <div class="card-header bg-dark text-white d-flex justify-content-between py-2 px-3 border-0">
          <span class="small fw-bold">채팅 상담 이력 상세</span>
          <button type="button" class="btn-close btn-close-white" @click="showChatModal = false"></button>
        </div>
        <div class="card-body overflow-auto bg-light small p-3" style="white-space: pre-wrap; line-height: 1.6;">{{ selectedChatLog }}</div>
        <div class="card-footer bg-white text-end border-0 py-2">
            <button class="btn btn-sm btn-secondary fw-bold px-3" @click="showChatModal = false">닫기</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator, type CellComponent } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { getDate } from '@/composables/useDate'
import AppAlert from '@/components/AppAlert.vue'
import DateForm from '@/components/DateForm.vue'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()
const { firstDay, today } = getDate()

// 상태 관리
const searchForm = reactive({ fromdt: firstDay, todt: today, campnm: '' })
const selectedCamp = ref<any>(null)
const stats = reactive<any>({ tot_cnt: 0, ready_cnt: 0 });
const detail_stats = ref<any[]>([]);
const currentFilter = ref('');
const showChatModal = ref(false);
const selectedChatLog = ref('');

// 테이블 인스턴스
const campTableRef = ref<HTMLElement | null>(null);
const detailTableRef = ref<HTMLElement | null>(null);
const surveyTableRef = ref<HTMLElement | null>(null);

let campTable: Tabulator | null = null;
let detailTable: Tabulator | null = null;
let surveyTable: Tabulator | null = null;

const handleSearch = async () => {
  try {
    const res = await api.get('/crm/outbound/camp-list', { params: { camp_nm: searchForm.campnm } });
    campTable?.setData(res.data || []);
  } catch (e) { vAlertError('조회 실패'); }
}

const fetchSummary = async (campNo: string) => {
  try {
    const res = await api.get('/crm/outbound/camp-stats', { params: { camp_no: campNo } });
    Object.assign(stats, res.data);
    detail_stats.value = res.data.details || [];
    applyFilter('');
  } catch (e) {}
}

const applyFilter = (filter: string) => {
  currentFilter.value = filter;
  if (selectedCamp.value) fetchDetails(selectedCamp.value.camp_no, filter);
}

const fetchDetails = async (campNo: string, filter: string = '') => {
  try {
    const res = await api.get('/crm/outbound/call-list', { params: { camp_no: campNo, filter: filter } });
    detailTable?.setData(res.data || []);
    surveyTable?.clearData();
  } catch (e) {}
}

const fetchSurveyResults = async (rsltNo: number) => {
  try {
    const res = await api.get('/crm/outbound/rslt-dtl', { params: { rslt_no: rsltNo } });
    surveyTable?.setData(res.data || []);
  } catch (e) {}
}

const playRecording = (file: string) => {
  const url = `/crm/cti/play-recording?file=${file}`;
  window.open(url, '_blank');
}

const initTables = () => {
  if (!campTableRef.value || !detailTableRef.value || !surveyTableRef.value) return;

  // 1. 캠페인 목록
  if (campTable) campTable.destroy();
  campTable = new Tabulator(campTableRef.value, {
    layout: "fitColumns", height: "100%", selectable: 1, headerVisible: false,
    columns: [{ title: "캠페인", field: "camp_nm", formatter: (cell:any) => `<div class="py-1 small fw-bold text-truncate">${cell.getValue()}</div>` }]
  });
  campTable.on("rowClick", (e, row) => {
    selectedCamp.value = row.getData();
    fetchSummary(selectedCamp.value.camp_no);
  });

  // 2. 상세 상담 내역
  if (detailTable) detailTable.destroy();
  detailTable = new Tabulator(detailTableRef.value, {
    layout: "fitColumns", height: "100%", selectable: 1,
    columnDefaults: { headerHozAlign: 'center', headerSort: false },
    columns: [
      { title: "구분", field: "media_type", width: 50, hozAlign: 'center', formatter: (cell: any) => cell.getValue() === 'chat' ? '💬' : '📞' },
      { title: "상담원", field: "consultnm", width: 80 },
      { title: "일시", field: "start_dtime", width: 140, formatter: (c:any) => c.getValue()?.substring(0, 16) || '-' },
      { title: "고객명", field: "cust_nm", width: 90, cssClass: 'fw-bold' },
      { title: "결과", field: "rslt_nm", width: 80, formatter: (c:any) => {
          const val = c.getValue();
          const color = val === '완료' ? 'bg-primary' : (val === '미처리' ? 'bg-secondary' : 'bg-danger');
          return `<span class="badge ${color}">${val}</span>`;
      }},
      { title: "이력", field: "rec_file", width: 80, formatter: (cell:any) => {
          const d = cell.getRow().getData();
          let h = '';
          if (d.rec_file) h += '<i class="bi bi-play-circle-fill text-danger fs-6 me-2 cursor-pointer"></i>';
          if (d.chat_log) h += '<i class="bi bi-file-earmark-text text-success fs-6 cursor-pointer"></i>';
          return h;
      }, cellClick: (e, cell) => {
          const d = cell.getRow().getData();
          const t = (e.target as HTMLElement);
          if (t.classList.contains('bi-play-circle-fill')) playRecording(d.rec_file);
          if (t.classList.contains('bi-file-earmark-text')) { selectedChatLog.value = d.chat_log; showChatModal.value = true; }
      }}
    ]
  });
  detailTable.on("rowClick", (e, row) => {
    const d = row.getData();
    if (d.rslt_no) fetchSurveyResults(d.rslt_no);
    else surveyTable?.clearData();
  });

  // 3. 설문 결과 상세
  if (surveyTable) surveyTable.destroy();
  surveyTable = new Tabulator(surveyTableRef.value, {
    layout: "fitColumns", height: "100%",
    columns: [
      { title: "질문내용", field: "question", widthGrow: 2, hozAlign: 'left' },
      { title: "답변", field: "ans_cont", widthGrow: 1, hozAlign: 'center' },
      { title: "점수", field: "point", width: 50, hozAlign: 'right', cssClass: 'fw-bold text-primary' }
    ]
  });
}

const exportExcel = () => {
  detailTable?.download("xlsx", `캠페인상담현황_${today}.xlsx`);
}

onMounted(() => { nextTick(() => { initTables(); handleSearch(); }); });
onUnmounted(() => {
  if (campTable) campTable.destroy();
  if (detailTable) detailTable.destroy();
  if (surveyTable) surveyTable.destroy();
});
</script>

<style scoped>
.hgoa-status-wrapper { height: 100vh; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.stat-badge-mini { padding: 4px 12px; border-radius: 4px; border: 1px solid #dee2e6; background: #fff; font-size: 11px; cursor: pointer; transition: all 0.2s; white-space: nowrap; }
.stat-badge-mini.active { background: #005a9f; color: #fff; }
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1050; }
:deep(.tabulator) { font-size: 12px; border: none; }
:deep(.tabulator-header) { background-color: #f8fafc !important; }
</style>
