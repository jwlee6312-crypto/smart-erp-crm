<template>
	<div class="modal-root-wrapper">
		<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

		<div v-if="visible" class="modal fade show d-block" tabindex="-1" style="background: rgba(0, 0, 0, 0.5); z-index: 1060; backdrop-filter: blur(2px);">
			<div :class="props.modalProps.large ? 'modal-dialog modal-lg modal-dialog-centered' : 'modal-dialog modal-dialog-centered'" style="max-height: 85vh;">
				<div class="modal-content border-0 shadow-lg" style="border-radius: 12px; overflow: hidden;">
					<div class="modal-header py-2 bg-white border-bottom shadow-sm">
						<h5 class="modal-title fw-bolder text-dark d-flex align-items-center" style="font-size: 15px;">
							<span class="bg-primary p-1 rounded me-2 d-flex align-items-center justify-content-center" style="width: 22px; height: 22px;">
								<i class="bi bi-search text-white" style="font-size: 12px;"></i>
							</span>
							{{ props.modalProps.title || '데이터 조회' }}
						</h5>
						<button type="button" class="btn-close shadow-none" style="font-size: 12px;" @click="close"></button>
					</div>

					<div class="modal-body p-3 bg-light">
						<div class="d-flex flex-wrap gap-2 justify-content-between align-items-center mb-3">
							<div class="d-flex align-items-center gap-2">
								<div class="input-group input-group-sm shadow-sm" style="width: 250px;">
									<span class="input-group-text bg-white border-end-0 text-muted">
										<i class="bi bi-funnel"></i>
									</span>
									<input
										v-model="filterValue"
										type="text"
										class="form-control border-start-0 ps-0 shadow-none"
										:placeholder="`${props.modalProps.title?.replace(' 선택', '')} 검색...`"
										@keyup.enter="search"
									/>
								</div>

								<!-- 💡 날짜 검색 필터 추가 -->
								<div v-if="props.modalProps.searchDate" class="d-flex align-items-center gap-1 shadow-sm rounded overflow-hidden">
									<input v-model="startDate" type="date" class="form-control form-control-sm border-0" style="width: 130px; font-size: 12px;" />
									<span class="px-1 text-muted" style="font-size: 11px;">~</span>
									<input v-model="endDate" type="date" class="form-control form-control-sm border-0" style="width: 130px; font-size: 12px;" />
								</div>

								<button class="btn btn-primary btn-sm px-3 fw-bold shadow-sm" @click="search" :disabled="loading">조회</button>
							</div>

							<div v-if="totalCount > 0" class="badge bg-white text-dark border px-3 py-2 fw-normal rounded-pill shadow-sm">
								조회: <span class="text-primary fw-bold">{{ totalCount.toLocaleString() }}</span> 건
							</div>
						</div>

						<div class="popup-grid-container border rounded-3 bg-white shadow-sm overflow-hidden" style="height: 480px; position: relative;">
							<div v-if="loading" class="loading-overlay">
								<div class="spinner-grow text-primary mb-2" role="status" style="width: 2rem; height: 2rem;"></div>
								<div class="fw-bold text-primary small">검색 중...</div>
							</div>
							<div ref="popupRef" style="height: 100%; width: 100%;"></div>
						</div>

						<div class="mt-2 text-muted" style="font-size: 11px;">
							<i class="bi bi-info-circle me-1 text-primary"></i> 항목을 클릭하여 선택하십시오.
						</div>
					</div>

					<div class="modal-footer py-2 bg-white border-top border-0 text-end">
						<button type="button" class="btn btn-outline-secondary btn-sm px-4 rounded-pill fw-bold" @click="close">취소</button>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { api } from '@/utils/axios'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import type { ModalProps } from '@/types/modal'

const props = defineProps<{ visible: boolean, modalProps: ModalProps }>()
const emit = defineEmits(['update:visible', 'confirm', 'close'])
const { showAlert, showError, alertMessage } = useAlerts()

const popupRef = ref<HTMLElement | null>(null)
const popupGrid = ref<Tabulator | null>(null)
const filterValue = ref<string>('')
const loading = ref(false)
const totalCount = ref(0)
const startDate = ref('')
const endDate = ref('')

watch(() => props.visible, async (isVisible) => {
	if (!isVisible) {
		if (popupGrid.value) { popupGrid.value.destroy(); popupGrid.value = null; }
		filterValue.value = '';
		totalCount.value = 0;
		return
	}

	await nextTick()

	setTimeout(() => {
		if (!popupRef.value) return

		popupGrid.value = new Tabulator(popupRef.value, {
			layout: 'fitColumns',
			height: '100%',
			pagination: "local",
			paginationSize: 15,
			paginationSizeSelector: [15, 30, 50],
			data: [],
			columns: props.modalProps.columns || [],
			columnDefaults: {
				headerHozAlign: 'center',
				headerSort: true,
				minWidth: 80,
				vertAlign: 'middle',
			},
			placeholder: "데이터가 없습니다."
		})

		search()

		popupGrid.value.on('rowClick', (_e, row) => {
			props.modalProps.onConfirm?.(row.getData())
			close()
		})
	}, 100)
})

async function search() {
	if (!popupGrid.value) return
	loading.value = true;
	try {
		const body = {
			...props.modalProps.data,
			gubun: props.modalProps.data.gubun || '',
			codenm: filterValue.value || props.modalProps.data.codenm || '',
			custnm: filterValue.value || props.modalProps.data.custnm || '',
			etcval: props.modalProps.data.etcval || ''
		}

		if (!props.modalProps.data.code) {
			body.code = filterValue.value || '';
		} else {
			body.code = props.modalProps.data.code;
		}

		const res = await api.post(props.modalProps.path, body)
		const resData = res.data || (Array.isArray(res.data) ? res.data : [])
		totalCount.value = resData.length

		if (popupGrid.value) {
			await popupGrid.value.setData(resData)
			popupGrid.value.redraw(true)
		}
	} catch (error) {
		console.error('팝업 조회 실패:', error)
	} finally {
		loading.value = false;
	}
}

function close() { emit('update:visible', false) }
</script>

<style scoped>
.loading-overlay {
	position: absolute;
	top: 0; left: 0; right: 0; bottom: 0;
	background: rgba(255, 255, 255, 0.7);
	z-index: 100;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	backdrop-filter: blur(1px);
}
.popup-grid-container :deep(.tabulator) { border: none !important; font-size: 13px !important; }
.popup-grid-container :deep(.tabulator-header) { background-color: #f8fafc !important; border-bottom: 1px solid #e2e8f0 !important; color: #475569 !important; }
.popup-grid-container :deep(.tabulator-row:hover) { background-color: #f0f9ff !important; cursor: pointer; }
.popup-grid-container :deep(.tabulator-footer) { background-color: #fff !important; border-top: 1px solid #e2e8f0 !important; padding: 5px !important; }
.popup-grid-container :deep(.tabulator-page) { border: 1px solid #e2e8f0 !important; margin: 0 2px !important; padding: 2px 8px !important; border-radius: 4px !important; background: #fff !important; color: #64748b !important; font-size: 12px; }
.popup-grid-container :deep(.tabulator-page.active) { background-color: #0d6efd !important; color: white !important; border-color: #0d6efd !important; }
</style>
