<!--
	=============================================================
	컴포넌트명	: 부서 선택 도움창 (Ultra-Compact ERP Design)
	작성일자	: 25.02.24
	설명		: 슬림한 폰트와 최소화된 여백으로 가독성을 극대화한 부서 팝업 (소문자 표준화 적용)
	=============================================================
-->

<template>
  <div v-if="visible" class="erp-modal-overlay">
    <div class="erp-modal-container shadow-2xl border-0 animate-scale-in">
      <!-- 🚀 초슬림 헤더 -->
      <div class="erp-modal-header d-flex justify-content-between align-items-center px-2 py-1">
        <div class="d-flex align-items-center">
          <div class="header-icon-box-xs me-2">
            <i class="bi bi-diagram-3 text-white" style="font-size: 12px;"></i>
          </div>
          <span class="fw-bold text-white" style="font-size: 12px; letter-spacing: -0.5px;">부서 선택 도움창</span>
        </div>
        <button type="button" class="btn-close btn-close-white" style="font-size: 9px; opacity: 0.7;" @click="$emit('close')"></button>
      </div>

      <!-- 🔍 고밀도 안내 영역 -->
      <div class="p-2 bg-white border-bottom shadow-sm d-flex justify-content-center">
        <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle" style="font-size: 10.5px; font-weight: 500; padding: 3px 10px;">
          <i class="bi bi-mouse2 me-1"></i>부서를 더블클릭하면 즉시 선택됩니다.
        </span>
      </div>

      <!-- 📊 알찬 그리드 영역 -->
      <div class="p-1 bg-light">
        <div class="grid-card-solid border rounded-1 overflow-hidden bg-white shadow-inner">
          <div ref="deptGridRef" style="height: 380px; width: 100%;"></div>
        </div>
      </div>

      <!-- 💡 초슬림 푸터 -->
      <div class="px-3 py-1 bg-white border-top text-end" style="min-height: 32px;">
        <button class="btn btn-xs btn-dark px-4 py-0 fw-bold rounded-1" @click="$emit('close')" style="font-size: 11px; height: 24px;">닫기</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { api } from '@/utils/axios'

const props = defineProps<{ visible: boolean, cmpycd: string }>()
const emit = defineEmits(['close', 'confirm'])

const deptGridRef = ref<HTMLElement | null>(null)
const grid = ref<Tabulator | null>(null)

const fetchDepts = async () => {
  try {
    // 💡 [소문자 표준화] API 파라미터 키를 소문자로 변경
    const res = await api.post('/ha00/HA00_00P_STR', {
      gubun: 'd0',
      cmpycd: String(props.cmpycd)
    })
    if (grid.value) {
      // 💡 데이터 셋업 (인터셉터에서 추출된 res.data 사용)
      const list = res.data || (Array.isArray(res.data) ? res.data : []);
      await grid.value.setData(list)
      setTimeout(() => grid.value?.redraw(true), 10)
    }
  } catch (e) { console.error('부서 조회 실패') }
}

watch(() => props.visible, async (val) => {
  if (!val) {
    if (grid.value) { grid.value.destroy(); grid.value = null; }
    return
  }

  await nextTick()
  setTimeout(() => {
    if (!deptGridRef.value) return

    grid.value = new Tabulator(deptGridRef.value, {
      layout: 'fitColumns',
      height: '100%',
      placeholder: '자료를 불러오는 중...',
      columnDefaults: { headerHozAlign: 'center', headerSort: false },
      columns: [
        // 💡 필드명 소문자 유지
        { title: '부서코드', field: 'deptcd', width: 100, cssClass: 'compact-cell-bold text-primary border-end' },
        { title: '부서명칭', field: 'deptnm', minWidth: 200, widthGrow: 1, cssClass: 'compact-cell-bold text-dark' }
      ]
    })

    grid.value.on('rowDblClick', (e, row) => {
      emit('confirm', row.getData())
      emit('close')
    })

    fetchDepts()
  }, 50)
})
</script>

<style scoped>
.erp-modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.4); backdrop-filter: blur(1px); z-index: 10000;
  display: flex; align-items: center; justify-content: center;
}
.erp-modal-container {
  width: 550px; max-width: 95%; background: #fff; border-radius: 4px; overflow: hidden;
  box-shadow: 0 15px 40px rgba(0,0,0,0.3);
}
.erp-modal-header {
  background: linear-gradient(135deg, #1e293b 0%, #005a9f 100%);
  border-bottom: 2px solid #005a9f;
}
.header-icon-box-xs {
  background: rgba(255,255,255,0.1); width: 22px; height: 22px;
  border-radius: 3px; display: flex; align-items: center; justify-content: center;
}
.grid-card-solid { background: #fff; border: 1px solid #dee2e6; }
.shadow-inner { box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.03); }

:deep(.tabulator) { border: none !important; font-size: 11.5px !important; }
:deep(.tabulator-header) {
  background-color: #f8fafc !important; border-bottom: 1.5px solid #cbd5e1 !important;
}
:deep(.tabulator-col-title) { font-weight: 800 !important; color: #1e293b !important; }
:deep(.tabulator-row) { border-bottom: 1px solid #f1f5f9 !important; min-height: 28px !important; }
:deep(.compact-cell-bold) { padding: 3px 6px !important; font-weight: 700; }
:deep(.tabulator-row:hover) { background-color: #f0f7ff !important; cursor: pointer; }

.animate-scale-in { animation: scaleIn 0.15s ease-out; }
@keyframes scaleIn { from { opacity: 0; transform: scale(0.98); } to { opacity: 1; transform: scale(1); } }
</style>
