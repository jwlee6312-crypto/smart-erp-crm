<!--
	=============================================================
	프로그램명	  : 공통 게시판 관리 (HBAA100U)
    프로그램 ID	: HBAA100U
	작성일자	    : 2026.09.10
	Description	: 게시판 목록 조회 및 등록 (ASP 연동 무결성 유지)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hbaa-workspace bg-light d-flex flex-column h-100 overflow-hidden text-start">
		<!-- 1. 상단 타이틀 및 검색 바 -->
		<header class="p-3 bg-white border-bottom shadow-sm flex-shrink-0">
			<div class="d-flex justify-content-between align-items-center mb-3">
				<h5 class="fw-bold m-0 text-dark">
					<i class="bi bi-clipboard-data text-primary me-2"></i>게시판 관리
				</h5>
				<div class="d-flex gap-2">
					<button class="btn btn-sm btn-primary px-4 fw-bold shadow-sm" @click="openWriteModal()">
						<i class="bi bi-pencil-square me-1"></i>글쓰기
					</button>
				</div>
			</div>

			<div class="search-box bg-light p-2 rounded-3 d-flex gap-2 align-items-center">
				<select v-model="searchParam.sfi" class="form-select form-select-sm border-0 bg-white" style="width: 120px;">
					<option value="ALL">전체</option>
					<option value="SUBJECT">제목</option>
					<option value="MEMO">내용</option>
					<option value="USERID">작성자</option>
				</select>
				<div class="input-group input-group-sm flex-grow-1">
					<input type="text" v-model="searchParam.stx" class="form-control border-0 bg-white shadow-none" placeholder="검색어를 입력하세요..." @keyup.enter="loadList">
					<button class="btn btn-dark px-3 fw-bold" @click="loadList"><i class="bi bi-search"></i></button>
				</div>
			</div>
		</header>

		<!-- 2. 메인 목록 영역 -->
		<main class="flex-grow-1 p-3 overflow-hidden position-relative">
			<div class="card border-0 shadow-sm h-100 rounded-3 overflow-hidden d-flex flex-column">
				<div class="flex-grow-1">
					<div ref="gridRef" class="tabulator-custom"></div>
				</div>
				<!-- 페이징 -->
				<div class="card-footer bg-white py-2 d-flex justify-content-center border-top">
					<nav>
						<ul class="pagination pagination-sm m-0 gap-1">
							<li class="page-item" :class="{disabled: searchParam.page === 1}">
								<a class="page-link border-0" @click="changePage(searchParam.page - 1)">이전</a>
							</li>
							<li v-for="p in totalPages" :key="p" class="page-item" :class="{active: searchParam.page === p}">
								<a class="page-link border-0 rounded-circle" @click="changePage(p)">{{ p }}</a>
							</li>
							<li class="page-item" :class="{disabled: searchParam.page === totalPages}">
								<a class="page-link border-0" @click="changePage(searchParam.page + 1)">다음</a>
							</li>
						</ul>
					</nav>
				</div>
			</div>
		</main>

		<!-- 3. 글쓰기/상세보기 모달 -->
		<div v-if="modalVisible" class="modal-backdrop-custom d-flex align-items-center justify-content-center">
			<div class="modal-content-custom bg-white shadow-lg rounded-4 overflow-hidden" style="width: 800px; max-height: 90vh;">
				<div class="modal-header-custom p-3 border-bottom d-flex justify-content-between align-items-center">
					<h6 class="fw-bold m-0"><i class="bi bi-file-earmark-text me-2"></i>{{ isEditMode ? '게시글 수정' : '게시글 작성' }}</h6>
					<button class="btn-close" @click="modalVisible = false"></button>
				</div>
				<div class="modal-body-custom p-4 overflow-auto">
					<div class="mb-3">
						<label class="form-label extra-small fw-bold text-muted">제목</label>
						<input type="text" v-model="bbsForm.subject" class="form-control form-control-sm border-0 bg-light-blue fw-bold" placeholder="제목을 입력하세요">
					</div>
					<div class="mb-3">
						<label class="form-label extra-small fw-bold text-muted">내용</label>
						<textarea v-model="bbsForm.memo" class="form-control border-0 bg-light-blue" rows="12" style="resize: none;" placeholder="내용을 입력하세요"></textarea>
					</div>
				</div>
				<div class="modal-footer-custom p-3 bg-light border-top d-flex justify-content-end gap-2">
					<button v-if="isEditMode" class="btn btn-sm btn-outline-danger px-3 fw-bold" @click="handleDelete">삭제</button>
					<button class="btn btn-sm btn-secondary px-3 fw-bold" @click="modalVisible = false">닫기</button>
					<button class="btn btn-sm btn-primary px-4 fw-bold" @click="handleSave">저장하기</button>
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
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

const gridRef = ref<HTMLElement | null>(null)
let gridInstance: Tabulator | null = null

const searchParam = reactive({
	fancode: '010', // 기본 게시판 코드 (공지사항)
	sfi: 'ALL',
	stx: '',
	page: 1,
	limit: 15
})

const totalPages = ref(1)
const modalVisible = ref(false)
const isEditMode = ref(false)
const bbsForm = reactive({
	boardid: '',
	subject: '',
	memo: '',
	filename: '',
	filesize: 0
})

const loadList = async () => {
	try {
		const res = await api.get('/bbs/list', { params: searchParam })
		if (res.data) {
			gridInstance?.setData(res.data.list || [])
			totalPages.value = Math.ceil((res.data.total || 0) / searchParam.limit)
		}
	} catch (e) {
		vAlertError('목록 로드 실패')
	}
}

const changePage = (p: number) => {
	if (p < 1 || p > totalPages.value) return
	searchParam.page = p
	loadList()
}

const openWriteModal = () => {
	isEditMode.value = false
	Object.assign(bbsForm, { boardid: '', subject: '', memo: '', filename: '', filesize: 0 })
	modalVisible.value = true
}

const openDetail = async (id: string) => {
	try {
		const res = await api.get('/bbs/detail', { params: { boardid: id } })
		if (res.data) {
			isEditMode.value = true
			Object.assign(bbsForm, res.data)
			modalVisible.value = true
		}
	} catch (e) {
		vAlertError('상세 정보 로드 실패')
	}
}

const handleSave = async () => {
	if (!bbsForm.subject) return vAlertError('제목을 입력하세요')
	try {
		await api.post('/bbs/save', { ...bbsForm, fancode: searchParam.fancode })
		vAlert('저장되었습니다.')
		modalVisible.value = false
		loadList()
	} catch (e) {
		vAlertError('저장 실패')
	}
}

const handleDelete = async () => {
	if (!confirm('정말 삭제하시겠습니까?')) return
	try {
		await api.post('/bbs/delete', { boardid: bbsForm.boardid })
		vAlert('삭제되었습니다.')
		modalVisible.value = false
		loadList()
	} catch (e) {
		vAlertError('삭제 실패')
	}
}

const initGrid = () => {
	if (!gridRef.value) return
	gridInstance = new Tabulator(gridRef.value, {
		layout: "fitColumns",
		height: "100%",
		columnDefaults: { headerHozAlign: 'center', vertAlign: 'middle', headerSort: false },
		columns: [
			{ title: "NO", field: "boardid", width: 80, hozAlign: "center" },
			{ title: "제목", field: "subject", widthGrow: 1, hozAlign: "left", formatter: (cell) => {
				return `<div class="fw-bold text-dark cursor-pointer">${cell.getValue()}</div>`
			}},
			{ title: "작성자", field: "usernm", width: 120, hozAlign: "center" },
			{ title: "날짜", field: "addtime", width: 120, hozAlign: "center" },
			{ title: "조회", field: "readcnt", width: 80, hozAlign: "center" }
		],
		placeholder: "데이터가 없습니다."
	})

	gridInstance.on("rowClick", (e, row) => {
		openDetail(row.getData().boardid)
	})
}

onMounted(() => {
	initGrid()
	loadList()
})
</script>

<style scoped>
.hbaa-workspace { height: calc(100vh - 65px); font-family: 'Pretendard', sans-serif; }
.tabulator-custom { border: none !important; }
.tabulator-custom :deep(.tabulator-header) { background-color: #f8fafc !important; border-bottom: 1px solid #e2e8f0 !important; font-size: 13px; font-weight: 800; color: #475569; }
.tabulator-custom :deep(.tabulator-row) { border-bottom: 1px solid #f1f5f9 !important; font-size: 13px; }
.tabulator-custom :deep(.tabulator-row:hover) { background-color: #f0f9ff !important; cursor: pointer; }

.modal-backdrop-custom { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0,0,0,0.5); z-index: 1050; backdrop-filter: blur(4px); }
.bg-light-blue { background-color: #f1f5f9; }
.extra-small { font-size: 0.75rem; }

.page-link { cursor: pointer; color: #64748b; }
.page-item.active .page-link { background-color: #0d6efd !important; border-color: #0d6efd !important; color: white !important; }
</style>
