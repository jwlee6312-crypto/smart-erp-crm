<!--
	=============================================================
	컴포넌트명	: 품목 선택 도움창 (Premium Compact Design)
	작성일자	: 25.02.24
	설명		: 고밀도 인터페이스와 15건 페이징 기능을 갖춘 탄탄한 품목 팝업
	=============================================================
-->

<template>
  <div v-if="visible" class="erp-modal-overlay">
    <div class="erp-modal-container shadow-2xl border-0 animate-pop-in">
      <!-- 🚀 탄탄한 헤더 -->
      <div class="erp-modal-header d-flex justify-content-between align-items-center px-3 py-1">
        <div class="d-flex align-items-center">
          <div class="header-icon-box-sm me-2">
            <i class="bi bi-box-seam text-white" style="font-size: 13px;"></i>
          </div>
          <span class="fw-bold text-white" style="font-size: 12.5px; letter-spacing: -0.3px;">품목 선택 도움창</span>
        </div>
        <button type="button" class="btn-close btn-close-white" style="font-size: 9px; opacity: 0.7;" @click="$emit('close')"></button>
      </div>

      <!-- 🔍 슬림 검색 영역 -->
      <div class="p-2 bg-white border-bottom shadow-sm">
        <div class="d-flex align-items-center gap-2">
          <div class="input-group input-group-sm" style="width: 350px;">
            <span class="input-group-text bg-light border-end-0 px-2 text-secondary" style="font-size: 11px;"><i class="bi bi-search"></i></span>
            <input
              v-model="searchWord"
              type="text"
              class="form-control border-start-0 ps-1 compact-font"
              placeholder="품목명 또는 코드를 입력..."
              @keyup.enter="fetchItems"
            />
            <button class="btn btn-primary fw-bold px-3 compact-btn" @click="fetchItems">조회</button>
          </div>
          <div class="ms-auto">
             <span class="badge erp-badge-info-slim">
              <i class="bi bi-mouse2 me-1"></i>더블클릭 시 선택
            </span>
          </div>
        </div>
      </div>

      <!-- 📊 알찬 그리드 영역 (15건 페이징) -->
      <div class="p-1 bg-light">
        <div class="grid-card-border overflow-hidden bg-white shadow-inner">
          <div ref="itemGridRef" style="height: 420px; width: 100%;"></div>
        </div>
      </div>

      <!-- 💡 슬림 푸터 -->
      <div class="px-3 py-1 bg-white border-top text-end d-flex justify-content-between align-items-center" style="min-height: 36px;">
        <span class="text-muted smaller-text">※ 15건 단위로 페이지가 구분됩니다.</span>
        <button class="btn btn-xs btn-dark px-4 py-1 fw-bold rounded-1 compact-font" @click="$emit('close')">닫기</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { api } from '@/utils/axios'

const props = defineProps<{ visible: boolean, cmpycd: string, astKind?: string }>()
const emit = defineEmits(['close', 'confirm'])

const searchWord = ref('')
const itemGridRef = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)

const fetchItems = async () => {
  try {
    const res = await api.post('/hs00/HS00_000S_STR', {
      gubun: 'I1',
      cmpycd: String(props.cmpycd),
      gbncd: String(props.astKind || '2'),
      code: '',
      codenm: String(searchWord.value).trim(),
      ETCVAL: ''
    })
    if (grid.value) {
      await grid.value.setData(res.data || [])
    }
  } catch (e) { console.error('조회 실패') }
}

watch(() => props.visible, async (val) => {
  if (!val) {
    if (grid.value) { grid.value.destroy(); grid.value = null; }
    searchWord.value = '';
    return
  }

  await nextTick()
  setTimeout(() => {
    if (!itemGridRef.value) return

    grid.value = new Tabulator(itemGridRef.value, {
      layout: 'fitColumns',
      height: '100%',
      placeholder: '데이터를 불러오는 중...',
      pagination: "local",      // 💡 로컬 페이징 활성화
      paginationSize: 15,       // 💡 지시하신 15건 단위
      columnDefaults: { headerHozAlign: 'center', headerSort: true },
      columns: [
        { title: '코드', field: 'itemcd', width: 100, cssClass: 'compact-cell fw-bold text-primary border-end' },
        { title: '품목명칭', field: 'itemnm', minWidth: 200, widthGrow: 1, cssClass: 'compact-cell fw-bold text-dark' },
        { title: '규격', field: 'itsize', width: 130, cssClass: 'compact-cell text-muted' },
        { title: '단위', field: 'unitnm', width: 60, hozAlign: 'center', cssClass: 'compact-cell' },
        {
          title: '재고',
          field: 'qty',
          width: 80,
          hozAlign: 'right',
          cssClass: 'compact-cell fw-bold text-dark',
          formatter: (c:any) => new Intl.NumberFormat().format(Number(c.getValue()) || 0)
        },
        { title: '구분', field: 'astkindnm', width: 85, hozAlign: 'center', cssClass: 'compact-cell smaller-text' }
      ]
    })

    grid.value.on('rowDblClick', (e, row) => {
      emit('confirm', row.getData())
      emit('close')
    })

    fetchItems()
  }, 50)
})
</script>

<style scoped>
.erp-modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(15, 23, 42, 0.4); backdrop-filter: blur(1px); z-index: 10000;
  display: flex; align-items: center; justify-content: center;
}
.erp-modal-container {
  width: 850px; max-width: 95%; background: #fff; border-radius: 4px; overflow: hidden;
  box-shadow: 0 15px 40px rgba(0,0,0,0.3);
}
.erp-modal-header {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  border-bottom: 2.5px solid #005a9f;
}
.header-icon-box-sm {
  background: rgba(255,255,255,0.1); width: 24px; height: 24px;
  border-radius: 3px; display: flex; align-items: center; justify-content: center;
}
.compact-font { font-size: 11.5px !important; }
.erp-badge-info-slim { background: #f1f5f9; color: #475569; padding: 2px 8px; border-radius: 3px; font-size: 10.5px; border: 1px solid #e2e8f0; font-weight: 600; }
.grid-card-border { background: #fff; border: 1px solid #dee2e6; }
.smaller-text { font-size: 10.5px; color: #64748b; }

:deep(.tabulator) { border: none !important; font-size: 11.5px !important; }
:deep(.tabulator-header) {
  background-color: #f8fafc !important; border-bottom: 1.5px solid #cbd5e1 !important;
}
:deep(.tabulator-col-title) { font-weight: 800 !important; color: #1e293b !important; }
:deep(.tabulator-row) { border-bottom: 1px solid #f1f5f9 !important; min-height: 28px !important; }
:deep(.tabulator-cell) { padding: 3px 6px !important; vertical-align: middle !important; }
:deep(.tabulator-row:hover) { background-color: #f0f7ff !important; cursor: pointer; }

/* 💡 페이징 컨트롤 컴팩트화 */
:deep(.tabulator-footer) { background: #fff !important; padding: 2px !important; border-top: 1px solid #dee2e6 !important; }
:deep(.tabulator-page) { padding: 2px 8px !important; margin: 0 2px !important; font-size: 11px !important; border-radius: 2px !important; }

.animate-pop-in { animation: popIn 0.15s ease-out; }
@keyframes popIn { from { opacity: 0; transform: scale(0.98); } to { opacity: 1; transform: scale(1); } }
</style>
