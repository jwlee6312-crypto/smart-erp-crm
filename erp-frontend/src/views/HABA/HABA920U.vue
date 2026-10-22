<!--
	=============================================================
	프로그램명	: 사용자 마스터 관리 (HABA920U)
	작성일자	: 2025.03.14
	설명        : HSOD100U 표준 준수 및 사진 업로드(파일 보관 방식) 적용
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	<Modal v-model:visible="modalVisible" :modalProps="modalProps" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
			<div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-person-badge-fill me-2 text-primary" style="font-size: 18px;"></i>
				시스템관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				기초관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">사용자 마스터 관리 (HABA920U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1 pe-3">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchList">조회</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
				<button v-if="formData.actkind === 'U0'" class="btn-erp btn-delete" @click="deleteData">삭제</button>
			</div>
		</div>

		<!-- 💡 2. 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

			<!-- [상단] 조회 필터 영역 -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-body p-0 bg-white">
					<table class="erp-table-dense" width="100%">
						<colgroup>
							<col style="width: 120px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="text-center bg-light">사용자 통합검색</th>
								<td>
									<div class="d-flex align-items-center gap-2 px-2" style="height: 32px;">
										<input v-model="searchForm.word" class="form-control form-control-sm" style="max-width: 300px;" placeholder="아이디 또는 성명 입력" @keyup.enter="fetchList" />
										<span class="text-muted small italic"><i class="bi bi-info-circle me-1"></i>아이디 또는 성명으로 검색이 가능합니다.</span>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- [하단] 레이아웃 영역 -->
			<div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">

				<!-- ⬅️ 좌측: 사용자 목록 -->
				<div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 450px; min-width: 450px;">
					<div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
						<span class="fw-bold small text-dark">사용자 목록</span>
						<span class="badge bg-primary-subtle text-primary border-0 rounded-pill px-2" style="font-size: 10px;">{{ gridCount }}명</span>
					</div>
					<div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
						<div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
					</div>
				</div>

				<!-- ➡️ 우측: 상세 정보 폼 -->
				<div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
					<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
						<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
							<span class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-primary"></i>사용자 상세 정보</span>
							<div v-if="formData.actkind === 'U0'" class="badge bg-warning text-dark border-0 px-2" style="font-size: 10px;">수정모드</div>
							<div v-else class="badge bg-success text-white border-0 px-2" style="font-size: 10px;">신규등록</div>
						</div>
						<div class="card-body p-0 bg-white overflow-auto scrollbar-sm">
							<table class="erp-table-dense w-100">
								<colgroup>
									<col style="width: 100px;" /><col />
									<col style="width: 100px;" /><col />
									<col style="width: 110px;" />
								</colgroup>
								<tbody>
									<tr>
										<th class="required bg-light text-center">아 이 디</th>
										<td>
											<input v-model="formData.userid" type="text" class="form-control form-control-sm fw-bold text-primary" :disabled="formData.actkind === 'U0'" />
										</td>
										<th class="required bg-light text-center">성    명</th>
										<td><input v-model="formData.usernm" type="text" class="form-control form-control-sm" /></td>
										<td rowspan="4" class="text-center p-2 border-start bg-light bg-opacity-10" style="width: 140px;">
											<!-- 📸 사진 업로드 영역 -->
											<div class="photo-upload-container mx-auto position-relative shadow-sm bg-white rounded border overflow-hidden" style="width: 100px; height: 120px;">
												<img :src="profileImageSrc" class="w-100 h-100 object-fit-cover" :style="{ opacity: isUploading ? 0.3 : 1 }" alt="Profile" />

												<!-- 업로드 중 스피너 -->
												<div v-if="isUploading" class="position-absolute top-50 start-50 translate-middle text-primary">
													<div class="spinner-border spinner-border-sm"></div>
												</div>

												<label v-if="!isUploading" class="position-absolute bottom-0 end-0 bg-dark bg-opacity-75 text-white rounded-circle d-flex align-items-center justify-content-center" style="width: 24px; height: 24px; cursor: pointer; transform: translate(30%, 30%);">
													<i class="bi bi-camera-fill" style="font-size: 12px;"></i>
													<input type="file" class="d-none" @change="handleImageUpload" accept="image/*" />
												</label>
											</div>
											<div class="mt-2 small fw-bold text-muted" style="font-size: 10px;">{{ isUploading ? '업로드 중...' : '사용자 프로필' }}</div>
										</td>
									</tr>
									<tr>
										<th class="required bg-light text-center">비밀번호</th>
										<td>
											<input v-model="formData.pw" type="password" class="form-control form-control-sm" placeholder="변경 시 입력" />
										</td>
										<th class="required bg-light text-center">비밀번호 확인</th>
										<td>
											<input v-model="formData.pw_c" type="password" class="form-control form-control-sm" placeholder="비밀번호 재입력" />
										</td>
									</tr>
									<tr>
										<th class="required bg-light text-center">부    서</th>
										<td>
											<div class="input-group input-group-sm">
												<input v-model="formData.deptnm" type="text" class="form-control bg-light" readonly />
												<button class="btn btn-outline-secondary" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
											</div>
										</td>
										<th class="bg-light text-center">사    번</th>
										<td><input v-model="formData.empno" type="text" class="form-control form-control-sm" /></td>
									</tr>
									<tr>
										<th class="required bg-light text-center">권한그룹</th>
										<td>
											<select v-model="formData.usergrp" class="form-select form-select-sm border-primary-subtle">
												<option value="">-- 선택 --</option>
												<option v-for="opt in grpOptions" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
											</select>
										</td>
										<th class="bg-light text-center">직    위</th>
										<td>
											<select v-model="formData.positionoff" class="form-select form-select-sm">
												<option value="">-- 선택 --</option>
												<option v-for="opt in posOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
											</select>
										</td>
									</tr>
									<tr>
										<th class="bg-light text-center border-top">휴 대 폰</th>
										<td class="border-top"><input v-model="formData.hpno" type="text" class="form-control form-control-sm text-center" /></td>
										<th class="bg-light text-center border-top">이 메 일</th>
										<td colspan="2" class="border-top"><input v-model="formData.email" type="email" class="form-control form-control-sm" /></td>
									</tr>
									<tr>
										<th class="bg-light text-center border-top">내선번호</th>
										<td class="border-top"><input v-model="formData.inner_no" type="text" class="form-control form-control-sm text-center" /></td>
										<th class="bg-light text-center border-top">사무실번호</th>
										<td colspan="2" class="border-top"><input v-model="formData.telno" type="text" class="form-control form-control-sm text-center" /></td>
									</tr>
									<tr>
										<th class="bg-light text-center border-top">영업여부</th>
										<td class="border-top ps-4">
											<div class="form-check form-switch m-0 d-flex align-items-center h-100">
												<input v-model="formData.salsyn" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="salsYn920">
												<label class="form-check-label ms-2 small fw-bold" for="salsYn920">{{ formData.salsyn === 'Y' ? '영업사원' : '일반' }}</label>
											</div>
										</td>
										<th class="bg-light text-center border-top">사용여부</th>
										<td colspan="2" class="border-top ps-4">
											<div class="form-check form-switch m-0 d-flex align-items-center h-100">
												<input v-model="formData.useyn" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="useYn920">
												<label class="form-check-label ms-2 small fw-bold" for="useYn920">{{ formData.useyn === 'Y' ? '사용중' : '중지' }}</label>
											</div>
										</td>
									</tr>
								</tbody>
							</table>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, nextTick, computed } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { API_URL } from '@/config/api'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

const searchForm = reactive({ word: '' })
const posOptions = ref<any[]>([]); const grpOptions = ref<any[]>([])
const gridCount = ref(0); const localImagePreview = ref(''); const isUploading = ref(false)
const originalPw = ref('') // 🚀 [보정] 원본 해시 암호 보관용

const formData = reactive({
	actkind: 'S2', cmpycd: authStore.cmpycd, userid: '', pw: '', pw_c: '', usernm: '',
	telno: '', inner_no: '', hpno: '', deptcd: '', deptnm: '', empno: '',
	positionoff: '', usergrp: '', email: '', pricegbn: '1', useyn: 'Y', salsyn: 'N',
	photo_path: ''
})

const profileImageSrc = computed(() => {
	if (localImagePreview.value) return localImagePreview.value
	if (formData.photo_path) {
		const path = formData.photo_path.trim()
		if (path.startsWith('http') || path.startsWith('data:')) return path

		// 💡 [최종 보정] 조회 경로를 /api/storage/ 로 명시적으로 통일
		const cmpycd = (authStore.cmpycd || 'COIT').toUpperCase()
		return `/api/storage/${cmpycd}/profile/${path}`
	}
	return '/img/default-avatar.png'
})

const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

async function fetchList() {
	try {
		const res = await api.post('/haba/HABA_920U_STR', { actkind: 'S2', word: searchForm.word, cmpycd: authStore.cmpycd })
		await nextTick(); const data = res.data || []
		mainGrid?.setData(data); gridCount.value = data.length
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

async function save() {
	// 💡 필수 입력 항목 체크 (수정 시에는 비밀번호 제외 가능하도록 개선)
	const isUpdate = formData.actkind === 'U0'
	if (!formData.userid) return vAlertError('사용자 아이디를 입력하세요.')
	if (!formData.usernm) return vAlertError('성명을 입력하세요.')
	if (!isUpdate && !formData.pw) return vAlertError('신규 등록 시 비밀번호는 필수입니다.')
	if (formData.pw && formData.pw !== formData.pw_c) return vAlertError('비밀번호 확인이 일치하지 않습니다.')
	if (!formData.usergrp) return vAlertError('권한그룹을 선택하세요.')
	if (!formData.deptcd) return vAlertError('부서를 선택하세요.')

	try {
		const act = isUpdate ? 'U1' : 'I1'

		// 🚀 [보정] 사용자가 비밀번호를 입력했인지 여부를 플래그로 전송
		const payload = { ...formData, pw_edit_yn: 'N' }
		if (formData.pw && formData.pw.trim() !== '') {
			payload.pw_edit_yn = 'Y'
		} else if (isUpdate) {
			payload.pw = originalPw.value
		}

		const res = await api.post('/haba/HABA_920U_STR', { ...payload, actkind: act, updemp: authStore.userid })

		// 🚀 결과 판정 로직 강화 (대소문자 무시 및 필드명 유연화)
		const resList = res.data || []
		const result = resList[0] || {}

		// result나 res 필드 중 하나라도 'OK'(대소문자 무시)이면 성공으로 간주
		const isSuccess =
			String(result.result || '').toUpperCase() === 'OK' ||
			String(result.res || '').toUpperCase() === 'OK' ||
			String(result.status || '').toUpperCase() === 'S'

		if (isSuccess) {
			vAlert('✅ 성공적으로 저장되었습니다.')
			setTimeout(async () => {
				await fetchList()
				const updatedUser = mainGrid?.getData().find((u: any) => u.userid === formData.userid)
				if (updatedUser) {
					Object.assign(formData, updatedUser)
					originalPw.value = updatedUser.pw || updatedUser.passwd || ''
					formData.pw = ''
					formData.pw_c = ''
					formData.actkind = 'U0'
					localImagePreview.value = ''
				}
			}, 500)
		} else {
			const errorMsg = result.msg || result.message || '저장 중 오류가 발생했습니다.'
			vAlertError('❌ 저장 실패: ' + errorMsg)
		}
	} catch (e: any) {
		vAlertError('❌ 서버 통신 오류: ' + (e.message || '저장 요청을 처리할 수 없습니다.'))
	}
}

async function deleteData() {
	if (!confirm('정말로 삭제하시겠습니까?')) return
	try {
		await api.post('/haba/HABA_920U_STR', { ...formData, actkind: 'D0' }); vAlert('삭제되었습니다.'); initialize(); fetchList()
	} catch (e) { vAlertError('삭제 실패') }
}

function initialize() {
	resetForm(formData); formData.actkind = 'S2'; formData.pricegbn = '1'; formData.useyn = 'Y';
	formData.cmpycd = authStore.cmpycd; localImagePreview.value = ''
}

// 📸 사진 선택 시 처리
async function handleImageUpload(e: any) {
	const file = e.target.files[0]
	if (!file) return

	// 1. 미리보기 표시
	const reader = new FileReader()
	reader.onload = (event: any) => { localImagePreview.value = event.target.result }
	reader.readAsDataURL(file)

	// 2. 서버에 즉시 파일 업로드
	isUploading.value = true
	try {
		if (!formData.userid) {
			vAlertError('사진을 업로드하려면 먼저 사용자 아이디를 입력해야 합니다.')
			localImagePreview.value = ''
			return
		}

		const uploadFormData = new FormData()
		uploadFormData.append('file', file)
		uploadFormData.append('userid', formData.userid)
		// 🚀 [보정] 필수 파라미터 추가
		uploadFormData.append('cmpycd', authStore.cmpycd)

		// 🚀 [수정] Axios FormData 전송 시 Content-Type 헤더를 수동으로 설정하지 않음 (브라우저가 자동 생성)
		// 백엔드의 filePath 응답은 interceptor에 의해 filepath(소문자)로 변환됨
		const res = await api.post('/comm/upload/profile', uploadFormData)

		if (res.data?.filepath) {
			formData.photo_path = res.data.filepath
			console.log('📸 업로드 성공. DB 자동 저장 중:', formData.photo_path)

			// 🚀 [자동 저장] 사진 업로드 성공 즉시 사용자 정보(photo_path)를 DB에 자동 반영
			const isUpdate = formData.actkind === 'U0'
			if (isUpdate) {
				const payload = { ...formData, pw_edit_yn: 'N', pw: originalPw.value }
				const saveRes = await api.post('/haba/HABA_920U_STR', { ...payload, actkind: 'U1', updemp: authStore.userid })
				if (saveRes.status === 200) {
					vAlert('✅ 프로필 사진이 업로드 및 자동 저장되었습니다.')
					await fetchList()
				} else {
					vAlertError('❌ 사진은 업로드되었으나 DB 자동 저장에 실패했습니다.')
				}
			} else {
				vAlert('✅ 사진 업로드 성공! [저장] 버튼을 눌러 완료하세요.')
			}
		} else {
			throw new Error('응답 데이터에 파일 경로가 없습니다.')
		}
	} catch (e: any) {
		const errMsg = e.response?.data || e.message || '알 수 없는 오류'
		vAlertError('❌ 사진 업로드 실패: ' + errMsg)
		localImagePreview.value = ''
	} finally {
		isUploading.value = false
	}
}

function handleOpenHelp(type: string) {
	if (type === 'DEPT') {
		openHelp('DEPT', (d: any) => { formData.deptcd = d.deptcd; formData.deptnm = d.deptnm })
	}
}

onMounted(async () => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: "fitColumns", height: "100%", selectable: 1,
			placeholder: "데이터 없음",
			columnDefaults: { headerSort: false, vertAlign: "middle", headerHozAlign: 'center' },
			columns: [
				{ title: "No", formatter: "rownum", width: 40, hozAlign: "center" },
				{ title: "사용자 ID", field: "userid", width: 110, cssClass: "fw-bold text-primary" },
				{ title: "성명", field: "usernm", width: 90 },
				{ title: "부서명", field: "deptnm", widthGrow: 1, hozAlign: "left", headerSort: true },
				{ title: "상태", field: "useyn", width: 60, hozAlign: "center",
				  formatter: (c) => c.getValue() === 'Y' ? '<span class="text-primary fw-bold">사용</span>' : '<span class="text-muted">중지</span>'
				}
			]
		})
		mainGrid.on('rowClick', (e, row) => {
			const rowData = row.getData()
			Object.assign(formData, rowData)

			// 🚀 [보정] 원본 해시 암호를 보관하고 입력창 초기화
			originalPw.value = rowData.pw || rowData.passwd || ''
			formData.pw = ''
			formData.pw_c = ''
			formData.actkind = 'U0'
			localImagePreview.value = ''
		})
	}

	api.post('/ha00/HA00_00P_STR', { gubun: 'E2', cmpycd: authStore.cmpycd }).then(r => {
		if (r.data) posOptions.value = r.data
	})
	api.post('/hs00/HS00_000S_STR', { gubun: 'GB', cmpycd: authStore.cmpycd, gbncd: '600' }).then(r => {
		if (r.data) grpOptions.value = r.data
	})
	fetchList()
})
</script>

<style scoped>
.erp-container { font-family: 'Pretendard', sans-serif; letter-spacing: -0.02rem; }
.tabulator-instance { width: 100% !important; background-color: #fff; font-size: 12px; }
:deep(.tabulator-header) { background-color: #f8fafc !important; border-bottom: 1px solid #dee2e6 !important; font-weight: 700; color: #444; }
:deep(.tabulator-row) { border-bottom: 1px solid #f1f1f1 !important; min-height: 32px !important; }
:deep(.tabulator-row.tabulator-selected) { background-color: #e7f1ff !important; color: #0d6efd !important; }
.photo-upload-container { transition: all 0.2s; background-color: #fcfcfc; }
.photo-upload-container:hover { border-color: #0d6efd !important; }
.scrollbar-sm::-webkit-scrollbar { width: 4px; }
.scrollbar-sm::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
.italic { font-style: italic; }
</style>