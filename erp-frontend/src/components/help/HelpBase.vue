<template>
  <div
    v-if="visible"
    class="modal fade show d-block"
    tabindex="-1"
    @keydown.esc="close"
    style="background: rgba(0,0,0,0.5); z-index: 1060; backdrop-filter: blur(2px);"
  >
    <div :class="large ? 'modal-dialog modal-lg modal-dialog-centered' : 'modal-dialog modal-dialog-centered'">
      <div
        class="modal-content border-0 shadow-lg"
        style="border-radius: 8px; overflow: hidden;"
        @keydown="handleAirtightTrap"
      >
        <!-- 모달 헤더 -->
        <div class="modal-header py-2 bg-white border-bottom shadow-sm">
          <h5 class="modal-title fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
            <i class="bi bi-search text-primary me-2"></i> {{ title }}
          </h5>
          <button
            type="button"
            class="btn-close shadow-none"
            style="font-size: 10px;"
            tabindex="-1"
            @click="close"
          ></button>
        </div>

        <!-- 모달 바디 -->
        <div class="modal-body p-3 bg-light">
          <!-- 검색 입력 영역 -->
          <div class="d-flex align-items-center gap-2 mb-3">
            <div class="input-group input-group-sm shadow-sm" style="width: 300px;">
              <span class="input-group-text bg-white border-end-0 text-muted">
                <i class="bi bi-funnel"></i>
              </span>
              <input
                ref="filterInput"
                v-model="filterValue"
                type="text"
                class="form-control border-start-0 ps-0 shadow-none"
                tabindex="0"
                :placeholder="`${title} 검색...`"
                @keydown="handleInputKey"
              />
            </div>
            <button
              class="btn btn-primary btn-sm px-3 fw-bold shadow-sm"
              tabindex="-1"
              @click="onSearch"
              :disabled="loading"
            >
              조회
            </button>
          </div>

          <!-- 그리드 출력 영역 -->
          <div
            ref="gridWrapper"
            class="popup-grid-container border rounded bg-white shadow-sm overflow-hidden"
            style="height: 450px; position: relative; outline: none;"
            tabindex="0"
            @keydown="handleGridKey"
          >
            <div v-if="loading" class="loading-overlay">
              <div class="spinner-border text-primary" role="status"></div>
            </div>
            <div ref="popupRef" style="height: 100%; width: 100%;"></div>
          </div>

          <div class="mt-2 text-muted x-small">
            ※ [Tab]으로 이동, [방향키]로 탐색, [Enter]로 확정하세요.
          </div>
        </div>

        <!-- 모달 푸터 -->
        <div class="modal-footer py-1 bg-white border-top text-end">
          <button
            ref="cancelBtn"
            type="button"
            class="btn btn-outline-secondary btn-sm px-4 fw-bold"
            tabindex="0"
            @click="close"
          >
            취소
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'

const props = defineProps<{
  visible: boolean,
  title: string,
  columns: any[],
  large?: boolean,
  loading?: boolean
}>()

const emit = defineEmits(['update:visible', 'close', 'search', 'confirm'])

const filterValue = ref('')
const filterInput = ref<HTMLElement | null>(null)
const gridWrapper = ref<HTMLElement | null>(null)
const cancelBtn = ref<HTMLElement | null>(null)
const popupRef = ref<HTMLElement | null>(null)
let popupGrid: any = null

const lastSearchedValue = ref('')
const pendingData = ref<any[] | null>(null)
const isTableReady = ref(false)

const close = () => {
  emit('update:visible', false)
  emit('close')
}

const onSearch = () => {
  lastSearchedValue.value = filterValue.value
  emit('search', filterValue.value)
}

const handleInputKey = (e: KeyboardEvent) => {
  if (e.key === 'Enter') {
    const selected = popupGrid?.getSelectedRows()[0]
    if (filterValue.value === lastSearchedValue.value && selected && !props.loading) {
      e.preventDefault()
      emit('confirm', selected.getData())
      close()
    } else {
      onSearch()
    }
  }
  if (e.key === 'ArrowDown') {
    e.preventDefault()
    gridWrapper.value?.focus()
  }
}

const handleAirtightTrap = (e: KeyboardEvent) => {
  if (e.key !== 'Tab') return
  const focusable = [filterInput.value, gridWrapper.value, cancelBtn.value].filter(el => !!el)
  const first = focusable[0] as HTMLElement
  const last = focusable[focusable.length - 1] as HTMLElement
  if (e.shiftKey && document.activeElement === first) {
    e.preventDefault()
    last.focus()
  } else if (!e.shiftKey && document.activeElement === last) {
    e.preventDefault()
    first.focus()
  }
}

const handleGridKey = (e: KeyboardEvent) => {
  if (!popupGrid || !isTableReady.value) return
  const allRows = popupGrid.getRows("active")
  if (allRows.length === 0) return
  const selected = popupGrid.getSelectedRows()[0]
  const idx = selected ? allRows.indexOf(selected) : -1
  if (e.key === 'ArrowDown') {
    e.preventDefault()
    const next = allRows[idx + 1]
    if (next) {
      popupGrid.deselectRow()
      next.select()
      next.getElement().scrollIntoView({ block: 'nearest' })
    }
  } else if (e.key === 'ArrowUp') {
    e.preventDefault()
    const prev = allRows[idx - 1]
    if (prev) {
      popupGrid.deselectRow()
      prev.select()
      prev.getElement().scrollIntoView({ block: 'nearest' })
    }
  } else if (e.key === 'Enter' && selected) {
    e.preventDefault()
    emit('confirm', selected.getData())
    close()
  }
}

const setData = (data: any[]) => {
  // 🛡️ 테이블이 준비된 경우에만 데이터 주입 (verticalFillMode 에러 방어)
  if (popupGrid && isTableReady.value) {
    popupGrid.setData(data || []).then(() => {
        if (data && data.length > 0) {
            popupGrid.selectRow(popupGrid.getRows()[0])
        }
    })
    pendingData.value = null
  } else {
    pendingData.value = data
  }
}

defineExpose({ setData })

watch(() => props.visible, async (isVisible) => {
  if (!isVisible) {
    if (popupGrid) {
      popupGrid.destroy()
      popupGrid = null
    }
    filterValue.value = ''
    pendingData.value = null
    isTableReady.value = false
    return
  }

  await nextTick()
  setTimeout(() => filterInput.value?.focus(), 150)

  setTimeout(() => {
    if (!popupRef.value || !props.columns) return

    popupGrid = new Tabulator(popupRef.value, {
      layout: 'fitColumns',
      height: '100%',
      selectable: 1,
      headerSort: false,
      columns: props.columns,
      columnDefaults: {
        headerHozAlign: 'center',
        vertAlign: 'middle'
      },
      placeholder: "데이터 없음"
    })

    // 🛡️ 테이블 빌드 완료 후 데이터 주입 시퀀스 시작
    popupGrid.on('tableBuilt', () => {
        isTableReady.value = true
        if (pendingData.value) {
            setData(pendingData.value)
        } else {
            onSearch()
        }
    })

    popupGrid.on('rowClick', (_e, row) => {
      emit('confirm', row.getData())
      close()
    })
  }, 100)
})
</script>

<style scoped>
.loading-overlay { position: absolute; inset: 0; background: rgba(255,255,255,0.7); z-index: 100; display: flex; align-items: center; justify-content: center; }
.popup-grid-container :deep(.tabulator-row.tabulator-selected) { background-color: #005a9f !important; color: #ffffff !important; font-weight: bold !important; }
.popup-grid-container :deep(.tabulator-row.tabulator-selected .tabulator-cell) { color: #ffffff !important; }
.popup-grid-container:focus { border: 2px solid #005a9f !important; box-shadow: 0 0 0 4px rgba(0, 90, 159, 0.1) !important; }
.x-small { font-size: 11px; }
</style>
