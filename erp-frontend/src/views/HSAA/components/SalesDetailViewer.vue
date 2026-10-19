<!--
	=============================================================
	프로그램명	: 영업활동 상세 조회 공통 컴포넌트 (SalesDetailViewer)
	작성일자	: 2025.03.14
	작성자      : AI Assistant
	설명        : HSAA 대시보드 및 조회 프로그램에서 공통으로 사용하는 상세 내역 뷰어
                (HSAA100U의 상세 영역을 조회 전용으로 표준화 및 캡슐화)
	=============================================================
-->

<template>
  <div class="h-100 d-flex flex-column gap-2 overflow-hidden bg-white shadow-sm rounded">
    <!-- 탭 네비게이션 -->
    <div class="card-header bg-white p-0 border-bottom">
      <ul class="nav nav-tabs border-0 px-3 pt-1">
        <li class="nav-item" v-for="tab in tabs" :key="tab.id">
          <a class="nav-link border-0 fw-bold small py-2 px-3"
             :class="{ 'active text-primary border-bottom border-primary border-3': activeTab === tab.id }"
             href="#" @click.prevent="switchTab(tab.id)">
            <span class="me-1 fw-bolder">{{ tab.no }}.</span>{{ tab.name }}
          </a>
        </li>
      </ul>
    </div>

    <!-- 탭별 상세 컨텐츠 -->
    <div class="flex-grow-1 d-flex flex-column overflow-hidden">

      <!-- 1. 영업건 상세 -->
      <div v-if="activeTab === 'SALES'" class="h-100 d-flex flex-column overflow-hidden">
        <div class="p-0 flex-shrink-0">
          <table class="erp-table-dense w-100 border-bottom shadow-xs">
            <colgroup><col style="width: 100px;" /><col /><col style="width: 100px;" /><col /><col style="width: 100px;" /><col /><col style="width: 100px;" /><col /></colgroup>
            <tbody>
              <tr class="bg-primary bg-opacity-5">
                <th class="bg-light">거래처명</th><td colspan="3" class="fw-bold text-primary">{{ formSales.custnm }}</td>
                <th class="bg-light border-start">대표자</th><td>{{ formSales.bossnm }}</td>
                <th class="bg-light border-start">전화번호</th><td>{{ formSales.telno }}</td>
              </tr>
              <tr>
                <th class="bg-light">회사주소</th>
                <td colspan="5">
                  <span class="badge bg-light text-dark border me-1" v-if="formSales.postno">{{ formSales.postno }}</span>
                  {{ formSales.address }} {{ formSales.d_address }}
                </td>
                <th class="bg-light border-start">팩스번호</th><td>{{ formSales.faxno }}</td>
              </tr>
              <tr>
                <th class="bg-light fw-bold">영업건명</th><td colspan="3" class="fw-bold">{{ formSales.salestitle }}</td>
                <th class="bg-light border-start">영업ID</th><td class="text-center fw-bold text-primary">{{ formSales.salesid }}</td>
                <th class="bg-light border-start">등록일자</th><td>{{ formSales.addtime }}</td>
              </tr>
              <tr>
                <th class="bg-light">영업담당</th><td>{{ formSales.usernm }} ({{ formSales.deptnm }})</td>
                <th class="bg-light border-start">진행상태</th><td class="text-danger fw-bold">{{ formSales.statenm }}</td>
                <th class="bg-light border-start">중요도</th><td>{{ formSales.importranknm }}</td>
                <th class="bg-light border-start text-muted">최종접촉일</th><td>{{ formSales.lastmtdt }}</td>
              </tr>
              <tr>
                <th class="bg-light">예상수주액</th><td class="text-end fw-bold">{{ formatMoney(formSales.foreamt) }}</td>
                <th class="bg-light border-start">실수주액</th><td class="text-end fw-bold text-success">{{ formatMoney(formSales.realamt) }}</td>
                <th class="bg-light border-start">예상수주일</th><td>{{ formSales.foredt }}</td>
                <th class="bg-light border-start">유치경로</th><td>{{ formSales.rtncd }}</td>
              </tr>
              <tr>
                <th class="bg-light">상세내용</th><td colspan="7"><div class="p-1 text-wrap small" style="min-height: 20px; white-space: pre-wrap;">{{ formSales.salesremark }}</div></td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="d-flex flex-column overflow-hidden mt-1" style="height: 30%;">
          <div class="card-header bg-white py-1 px-3 border-bottom border-top small fw-bold text-dark">
            <i class="bi bi-list-check me-1 text-primary"></i> 제안 품목 내역
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white d-flex flex-column shadow-inner">
            <div ref="itemGridRef" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>
      </div>

      <!-- 2. 상담일지 -->
      <div v-show="activeTab === 'DIARY'" class="h-100 flex-column overflow-hidden" :class="{ 'd-flex': activeTab === 'DIARY' }">
        <div class="p-0 flex-shrink-0" v-if="formDiary.ser">
          <table class="erp-table-dense w-100 border-bottom shadow-xs bg-light bg-opacity-10">
            <colgroup><col style="width: 100px;" /><col /><col style="width: 100px;" /><col /><col style="width: 100px;" /><col /></colgroup>
            <tbody>
              <tr>
                <th class="bg-light">상담일시</th><td>{{ formDiary.contdt }} {{ formDiary.conthh }}:{{ formDiary.contmm }}</td>
                <th class="bg-light border-start">상담채널</th><td>{{ formDiary.channelnm }}</td>
                <th class="bg-light border-start">상담고객</th><td>{{ formDiary.custnm }}</td>
              </tr>
              <tr>
                <th class="bg-light">단계변동</th><td class="text-primary fw-bold">{{ formDiary.statenm || '변동없음' }}</td>
                <th class="bg-light border-start">보고여부</th><td>{{ formDiary.reportyn === 'Y' ? '보고' : '미보고' }}</td>
                <th class="bg-light border-start">실수주액</th><td class="text-end">{{ formatMoney(formDiary.realamt) }}</td>
              </tr>
              <tr>
                <th class="bg-light border-top border-bottom">상담본문</th>
                <td colspan="5" class="p-2 border-top border-bottom bg-white">
                  <div class="small scrollbar-sm" style="max-height: 400px; overflow-y: auto; white-space: pre-wrap; word-break: break-all;">{{ formDiary.diarycontent }}</div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="d-flex flex-column overflow-hidden mt-1" style="height: 30%;">
          <div class="card-header bg-white py-1 px-3 border-bottom border-top small fw-bold text-dark">
            <i class="bi bi-chat-dots me-1 text-primary"></i> 상담 이력 리스트 (선택 시 상단에 상세 표시)
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white d-flex flex-column shadow-inner">
            <div ref="diaryGridRef" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>
      </div>

      <!-- 3. Key맨 -->
      <div v-show="activeTab === 'KEYMAN'" class="h-100 flex-column overflow-hidden" :class="{ 'd-flex': activeTab === 'KEYMAN' }">
        <div class="p-0 flex-shrink-0" v-if="formKeyman.custid">
          <table class="erp-table-dense w-100 border-bottom shadow-xs">
            <colgroup><col style="width: 100px;" /><col /><col style="width: 100px;" /><col /><col style="width: 100px;" /><col /></colgroup>
            <tbody>
              <tr>
                <th class="bg-light">고객성명</th><td class="fw-bold">{{ formKeyman.name }}</td>
                <th class="bg-light border-start">부서/직위</th><td>{{ formKeyman.custdept }} {{ formKeyman.jikch }}</td>
                <th class="bg-light border-start text-danger">핸드폰</th><td class="fw-bold">{{ formKeyman.hpno }}</td>
              </tr>
              <tr>
                <th class="bg-light">직통전화</th><td>{{ formKeyman.custtel }} ({{ formKeyman.innumber }})</td>
                <th class="bg-light border-start">개인메일</th><td colspan="3">{{ formKeyman.mail }}</td>
              </tr>
              <tr>
                <th class="bg-light">KEYMAN구분</th><td>{{ formKeyman.keymannm }}</td>
                <th class="bg-light border-start">관리등급</th><td>{{ formKeyman.levelnm }}</td>
                <th class="bg-light border-start">호감도</th><td>{{ formKeyman.favornm }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="d-flex flex-column overflow-hidden mt-1" style="height: 30%;">
          <div class="card-header bg-white py-1 px-3 border-bottom border-top small fw-bold text-dark">
            <i class="bi bi-people me-1 text-primary"></i> Key맨 명단
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white d-flex flex-column shadow-inner">
            <div ref="keymanGridRef" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>
      </div>

      <!-- 4. 단계변동 -->
      <div v-show="activeTab === 'STAGES'" class="h-100 flex-column overflow-hidden" :class="{ 'd-flex': activeTab === 'STAGES' }">
        <div class="flex-grow-1 d-flex flex-column overflow-hidden">
          <div class="card-header bg-white py-1 px-3 border-bottom small fw-bold text-dark">
            <i class="bi bi-arrow-left-right me-1 text-primary"></i> 단계 변동 이력
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white d-flex flex-column shadow-inner">
            <div ref="stageGridRef" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>
      </div>

      <!-- 5. 문서 -->
      <div v-show="activeTab === 'DOCS'" class="h-100 flex-column overflow-hidden" :class="{ 'd-flex': activeTab === 'DOCS' }">
        <div class="flex-grow-1 d-flex flex-column overflow-hidden">
          <div class="card-header bg-white py-1 px-3 border-bottom small fw-bold text-dark">
            <i class="bi bi-file-earmark-text me-1 text-primary"></i> 관련 영업 문서
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white d-flex flex-column shadow-inner">
            <div ref="docsGridRef" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { api } from '@/utils/axios'

const tabs = [
  { id: 'SALES', no: 1, name: '영업건 상세' },
  { id: 'DIARY', no: 2, name: '상담일지' },
  { id: 'KEYMAN', no: 3, name: 'Key맨' },
  { id: 'STAGES', no: 4, name: '단계변동' },
  { id: 'DOCS', no: 5, name: '문서' }
]

const activeTab = ref('SALES')
const formSales = reactive<any>({})
const formDiary = reactive<any>({})
const formKeyman = reactive<any>({})

// 그리드 인스턴스
const itemGridRef = ref(null); let gridItems: Tabulator | null = null
const diaryGridRef = ref(null); let gridDiary: Tabulator | null = null
const keymanGridRef = ref(null); let gridKeyman: Tabulator | null = null
const stageGridRef = ref(null); let gridStages: Tabulator | null = null
const docsGridRef = ref(null); let gridDocs: Tabulator | null = null

const formatMoney = (val: any) => new Intl.NumberFormat('ko-KR').format(Number(val) || 0)

const switchTab = (tabId: string) => {
  activeTab.value = tabId;
  nextTick(() => {
    gridItems?.redraw()
    gridDiary?.redraw()
    gridKeyman?.redraw()
    gridStages?.redraw()
    gridDocs?.redraw()
  })
}

// 🚀 [핵심] 외부에서 호출하는 데이터 조회 통합 메서드
const loadData = async (salesid: string, custcd: string) => {
  if (!salesid) return;

  try {
    // 1. 영업 마스터 및 품목 상세
    const resDetail = await api.get('/hsaa/detail', { params: { salesid } })
    if (resDetail.data) {
      Object.assign(formSales, resDetail.data.master || {})
      gridItems?.setData(resDetail.data.items || [])
    }

    // 2. 상담일지 이력
    const resDiary = await api.get('/hsaa/diary', { params: { salesid } })
    gridDiary?.setData(resDiary.data || [])
    if (resDiary.data?.length > 0) {
      Object.keys(formDiary).forEach(key => delete formDiary[key])
      Object.assign(formDiary, resDiary.data[0])
    } else {
      Object.keys(formDiary).forEach(key => delete formDiary[key])
      Object.assign(formDiary, { ser: '' })
    }

    // 3. Key맨 정보 (거래처 기준)
    if (custcd) {
      const resKeyman = await api.get('/hsaa/keyman', { params: { custcd } })
      gridKeyman?.setData(resKeyman.data || [])
      if (resKeyman.data?.length > 0) Object.assign(formKeyman, resKeyman.data[0])
      else Object.assign(formKeyman, { custid: '' })
    }

    // 4. 단계변동 이력
    const resStages = await api.get('/hsaa/stages', { params: { salesid } })
    gridStages?.setData(resStages.data || [])

    // 5. 문서 내역
    const resDocs = await api.get('/hsaa/docs', { params: { salesid } })
    gridDocs?.setData(resDocs.data || [])

  } catch (e) {
    console.error('상세 데이터 로드 실패', e)
  }
}

// 부모 컴포넌트에서 접근 가능하도록 공개
defineExpose({ loadData, switchTab })

onMounted(() => {
  // 그리드 초기화 (조회 전용 - 11px 적용)
  const commonConfig: any = { layout: "fitColumns", height: "100%", selectable: 1 }

  gridItems = new Tabulator(itemGridRef.value!, { ...commonConfig,
    columns: [
      { title: "No", formatter: "rownum", width: 60, hozAlign: "center" },
      { title: "품목명", field: "itemnm", widthGrow: 2 },
      { title: "수량", field: "qty", width: 100, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "금액", field: "amt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "fw-bold text-primary" }
    ]
  })

  gridDiary = new Tabulator(diaryGridRef.value!, { ...commonConfig,
    columns: [
      { title: "상담일자", field: "contdt", width: 120 },
      { title: "채널", field: "channelnm", width: 120 },
      { title: "내용", field: "diarycontent", widthGrow: 1, tooltip: true, formatter: (cell: any) => {
          const val = cell.getValue() || '';
          return val.length > 50 ? val.substring(0, 50) + '...' : val;
      }},
      { title: "변동상태", field: "statenm", width: 100, cssClass: "text-primary fw-bold" }
    ]
  })
  gridDiary.on("rowClick", (e, row) => {
    // 기존 데이터 초기화 후 새 데이터 할당 (반응성 유지)
    Object.keys(formDiary).forEach(key => delete formDiary[key])
    Object.assign(formDiary, row.getData())
  })

  gridKeyman = new Tabulator(keymanGridRef.value!, { ...commonConfig,
    columns: [
      { title: "성명", field: "name", width: 100, cssClass: "fw-bold" },
      { title: "부서", field: "custdept", width: 180 },
      { title: "직위", field: "jikch", width: 100 },
      { title: "핸드폰", field: "hpno", width: 150 },
      { title: "메일주소", field: "mail", widthGrow: 1.2 },
      { title: "Key맨구분", field: "keymannm", width: 150, hozAlign: "center" },
      { title: "호감도", field: "favornm", width: 120, hozAlign: "center" }
    ]
  })
  gridKeyman.on("rowClick", (e, row) => Object.assign(formKeyman, row.getData()))

  gridStages = new Tabulator(stageGridRef.value!, { ...commonConfig,
    columns: [
      { title: "변동일자", field: "chngdt", width: 100 },
      { title: "변동상태", field: "statenm", width: 100, cssClass: "fw-bold text-danger" },
      { title: "변동사유", field: "remark", widthGrow: 1 }
    ]
  })

  gridDocs = new Tabulator(docsGridRef.value!, { ...commonConfig,
    columns: [
      { title: "유형", field: "docgbnm", width: 80 },
      { title: "문서제목", field: "title", widthGrow: 1.5 },
      { title: "파일명", field: "filename", widthGrow: 1, cssClass: "text-primary clickable" }
    ]
  })
})
</script>

<style scoped>
.tabulator-instance { font-size: 11px !important; border: none; }
.erp-table-dense { font-size: 11px !important; }
.erp-table-dense th { padding: 2px 8px; border: 1px solid #dee2e6; background-color: #f8f9fa; color: #444; font-weight: 700; }
.erp-table-dense td { padding: 2px 8px; border: 1px solid #dee2e6; background-color: #fff; color: #334155; }
.nav-tabs .nav-link { font-size: 12px; color: #64748b; padding: 0.5rem 1rem !important; }
.nav-tabs .nav-link.active { font-weight: 800 !important; }
.shadow-xs { box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05); }
.shadow-inner { box-shadow: inset 0 2px 4px 0 rgba(0, 0, 0, 0.06); }
.x-small { font-size: 10px; }
.vr { min-height: 1em; width: 1px; background-color: currentColor; }

/* 스크롤바 스타일 */
.scrollbar-sm::-webkit-scrollbar { width: 4px; height: 4px; }
.scrollbar-sm::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
.scrollbar-sm::-webkit-scrollbar-track { background: #f1f5f9; }
</style>
