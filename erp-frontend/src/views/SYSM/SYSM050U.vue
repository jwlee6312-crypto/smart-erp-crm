<!--
	=============================================================
	프로그램명	: 매뉴얼 관리 (SYSM_050U)
	작성일자	: 2025.02.24
	설명        : 시스템 프로그램 매뉴얼 마스터 관리 (표준화된 regyn 필드 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center">
				<i class="bi bi-journal-check me-2 text-primary" style="font-size: 18px;"></i>
				시스템관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">매뉴얼 마스터 (SYSM_050U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1 pe-2">
				<button class="btn-erp btn-search" @click="search">조회</button>
				<button class="btn-erp btn-save" @click="save">저장하기</button>
			</div>
		</div>

		<div class="flex-grow-1 overflow-hidden p-3 d-flex gap-3 bg-light">
			<!-- 좌측: 프로그램 목록 -->
			<div class="card border shadow-sm d-flex flex-column" style="width: 420px;">
				<div class="card-header bg-white py-2 fw-bold small">대상 프로그램 리스트</div>
				<div class="p-2 border-bottom bg-white">
					<div class="input-group input-group-sm">
						<span class="input-group-text bg-light border-end-0"><i class="bi bi-search small"></i></span>
						<input v-model="searchPgmId" type="text" class="form-control border-start-0 ps-0" placeholder="ID 또는 프로그램명 검색" @keyup.enter="search" />
					</div>
				</div>
				<div class="card-body p-0 overflow-hidden d-flex flex-column">
					<div ref="gridRef" class="tabulator-instance flex-grow-1"></div>
				</div>
			</div>

			<!-- 우측: 편집 및 미리보기 -->
			<div class="flex-grow-1 d-flex flex-row gap-2 overflow-hidden">
				<!-- 편집창 -->
				<div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden bg-white" style="flex: 1;">
					<div class="card-header bg-white py-2 d-flex justify-content-between align-items-center border-bottom">
						<div class="fw-bold text-dark small">
							<i class="bi bi-pencil-fill me-2 text-primary"></i>
							<span v-if="selectedProg.id">[{{ selectedProg.id }}] {{ selectedProg.nm }}</span>
							<span v-else>프로그램을 선택하세요</span>
						</div>
						<span v-if="lastSavedTime" class="badge bg-success" style="font-size: 10px;">저장완료 {{ lastSavedTime }}</span>
					</div>
					<div class="card-body p-0 flex-grow-1 d-flex flex-column overflow-hidden">
						<textarea v-model="textContent" class="form-control p-3 flex-grow-1 border-0 custom-textarea" placeholder="내용을 입력하세요." style="resize: none;"></textarea>
					</div>
				</div>

				<!-- 미리보기 -->
				<div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden bg-light" style="flex: 1.2;">
					<div class="card-header bg-white py-2 fw-bold text-primary small border-bottom">실시간 미리보기</div>
					<div class="card-body p-4 flex-grow-1 overflow-auto bg-white manual-preview-container">
						<div class="manual-content-view">
							<h3 class="manual-title" v-if="selectedProg.id">{{ selectedProg.nm }}</h3>
							<hr v-if="selectedProg.id" />
							<div v-html="formattedContent"></div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import AppAlert from '@/components/AppAlert.vue'
import { useAuthStore } from '@/stores/authStore'

const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const authStore = useAuthStore()

const gridRef = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null

const selectedProg = reactive({ id: '', nm: '' })
const searchPgmId = ref('')
const textContent = ref('')
const lastSavedTime = ref('')

const formattedContent = computed(() => {
	if (!textContent.value) return '<p class="text-muted">내용이 없습니다.</p>'
	return textContent.value.split('\n').map(line => {
		let t = line.trim(); if (!t) return '<br/>';
		if (t.startsWith('■')) return `<h5 class="mt-4 mb-2 fw-bold text-primary border-bottom pb-2">${t.substring(1)}</h5>`
		if (t.startsWith('●')) return `<h6 class="mt-3 mb-2 fw-bold text-dark">${t.substring(1)}</h6>`
		return `<p class="mb-1 ps-1">${line}</p>`
	}).join('')
})

onMounted(async () => {
	await nextTick()
	grid = new Tabulator(gridRef.value!, {
		layout: 'fitColumns', height: '100%', selectable: 1,
		placeholder: '데이터 로딩 중...',
		columns: [
			{ title: "ID", field: "pgmid", width: 110, headerHozAlign: 'center',
				formatter: (cell) => {
					const d = cell.getData();
					return String(d.regyn || '').toUpperCase() === 'Y' ? `<span class="text-primary fw-bold">${cell.getValue()}</span>` : cell.getValue();
				}
			},
			{ title: "프로그램명", field: "pgmnm", hozAlign: "left", headerHozAlign: 'center', minWidth: 150,
				formatter: (cell) => {
					const d = cell.getData();
					return String(d.regyn || '').toUpperCase() === 'Y' ? `<span class="text-primary fw-bold">${cell.getValue()}</span>` : cell.getValue();
				}
			},
			{ title: "등록", field: "regyn", width: 80, hozAlign: "center", headerHozAlign: 'center',
				formatter: (cell) => {
					return String(cell.getValue() || '').toUpperCase() === 'Y' ? '<span class="badge bg-primary px-2">등록됨</span>' : '<span class="text-muted small">미등록</span>';
				}
			}
		]
	})

	grid.on("rowClick", (e, row) => {
		const d = row.getData();
		selectedProg.id = d.pgmid; selectedProg.nm = d.pgmnm
		loadManual(d.pgmid)
	})

	search()
})

const search = async () => {
	try {
		const res = await api.post('/comm/getProgramList', { pgmnm: '' })
		const query = searchPgmId.value.toUpperCase().trim();
		const filtered = (res.data || []).filter((i: any) => !query || i.pgmid.toUpperCase().includes(query) || i.pgmnm.toUpperCase().includes(query))

		if (grid) {
			await grid.setData(filtered);
            grid.redraw(true);
		}
	} catch (e) { console.error('Data Load Error:', e) }
}

const loadManual = async (progid: string) => {
	try {
		const cleanId = progid.replace(/_/g, '')
		const res = await api.get(`/manual/db/${cleanId}`)
		textContent.value = res.data?.content || ''
		lastSavedTime.value = textContent.value ? '조회완료' : ''
	} catch (e) { textContent.value = ''; lastSavedTime.value = '' }
}

const save = async () => {
	if (!selectedProg.id) return vAlertError('프로그램을 선택하세요.')
	try {
		const cleanId = selectedProg.id.replace(/_/g, '')
		await api.post('/manual/db/save', {
			progid: cleanId, prognm: selectedProg.nm, content: textContent.value, upd_id: authStore.userid
		})
		vAlert('성공적으로 저장되었습니다.')
		lastSavedTime.value = new Date().toLocaleTimeString()
		await search()
	} catch (e) { vAlertError('저장 오류') }
}
</script>

<style scoped>
.custom-textarea { background-color: #fdfdfd; font-family: 'Pretendard', sans-serif; font-size: 15px; line-height: 1.6; }
.manual-preview-container { font-family: 'Pretendard', sans-serif; line-height: 1.7; color: #333; }
.manual-title { color: #005a9f; font-weight: 800; margin-bottom: 15px; }
.tabulator-instance { border: 1px solid #dee2e6; }
</style>
