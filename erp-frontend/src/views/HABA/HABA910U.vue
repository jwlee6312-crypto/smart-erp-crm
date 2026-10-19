<!--기본정보/개인정보관리 [ERP 프리미엄 고밀도 표준 적용] -->
<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom shadow-sm bg-white py-1 flex-shrink-0">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 13px;">
				<i class="bi bi-person-badge-fill me-2 text-primary"></i>
				시스템관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">개인정보 관리 (HABA910U)</span>
			</div>
			<div class="btn-group-erp pe-2">
				<button class="btn-erp btn-init" @click="fetchInfo">초기화</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
			</div>
		</div>

		<!-- 💡 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-auto p-3 bg-light">
			<div class="card border-0 shadow-sm overflow-hidden mx-auto" style="max-width: 900px;">
				<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
					<span class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-primary"></i>내 프로필 정보 수정</span>
					<span class="badge bg-primary-subtle text-primary border-0 px-2" style="font-size: 10px;">사용자 본인 전용</span>
				</div>
				<div class="card-body p-4 bg-white">
					<div class="row g-4">
						<!-- 📸 좌측: 사진 업로드 섹션 -->
						<div class="col-md-3 text-center border-end pe-4">
							<div class="profile-photo-wrapper mx-auto position-relative shadow-sm bg-light rounded border overflow-hidden" style="width: 150px; height: 180px;">
								<img :src="profileImageSrc" class="w-100 h-100 object-fit-cover" :style="{ opacity: isUploading ? 0.3 : 1 }" alt="Profile" />

								<div v-if="isUploading" class="position-absolute top-50 start-50 translate-middle text-primary">
									<div class="spinner-border spinner-border-sm"></div>
								</div>

								<label v-if="!isUploading" class="photo-edit-btn position-absolute bottom-0 end-0 bg-primary text-white d-flex align-items-center justify-content-center shadow" style="width: 32px; height: 32px; cursor: pointer; border-radius: 4px 0 0 0;">
									<i class="bi bi-camera-fill"></i>
									<input type="file" class="d-none" @change="handleImageUpload" accept="image/*" />
								</label>
							</div>
							<div class="mt-3">
								<div class="fw-bold text-dark mb-1">{{ formData.usernm || '사용자' }}</div>
								<div class="text-muted extra-small">{{ formData.userid }}</div>
								<button class="btn btn-xs btn-outline-secondary mt-3 w-100" @click="triggerFileUpload" :disabled="isUploading">
									<i class="bi bi-upload me-1"></i>사진 변경
								</button>
							</div>
						</div>

						<!-- 📝 우측: 정보 입력 섹션 -->
						<div class="col-md-9">
							<table class="erp-table-dense w-100">
								<colgroup>
									<col style="width: 120px;" /><col />
									<col style="width: 120px;" /><col />
								</colgroup>
								<tbody>
									<tr>
										<th class="bg-light">아이디</th>
										<td><input v-model="formData.userid" type="text" class="form-control form-control-sm bg-light fw-bold text-primary text-center" readonly /></td>
										<th class="required bg-light">성 명</th>
										<td><input v-model="formData.usernm" type="text" class="form-control form-control-sm" placeholder="성함 입력" /></td>
									</tr>
									<tr>
										<th class="required bg-light">비밀번호</th>
										<td><input v-model="formData.pw" type="password" class="form-control form-control-sm" placeholder="변경 시에만 입력" /></td>
										<th class="required bg-light">비밀번호 확인</th>
										<td><input v-model="formData.pw_c" type="password" class="form-control form-control-sm" placeholder="비밀번호 재입력" /></td>
									</tr>
									<tr>
										<th class="bg-light">연락처(TEL)</th>
										<td><input v-model="formData.telno" type="text" class="form-control form-control-sm text-center" /></td>
										<th class="bg-light">핸드폰(HP)</th>
										<td><input v-model="formData.hpno" type="text" class="form-control form-control-sm text-center text-primary fw-bold" /></td>
									</tr>
									<tr>
										<th class="bg-light text-primary">내선번호</th>
										<td><input v-model="formData.inner_no" type="text" class="form-control form-control-sm text-center fw-bold" placeholder="CTI 내선번호" /></td>
										<th class="bg-light">이메일 주소</th>
										<td><input v-model="formData.email" type="email" class="form-control form-control-sm" placeholder="example@domain.com" /></td>
									</tr>
									<tr>
										<th class="bg-light">소속부서</th>
										<td colspan="3">
											<div class="input-group input-group-sm">
												<input v-model="formData.deptnm" type="text" class="form-control bg-light" readonly />
												<span class="input-group-text bg-light"><i class="bi bi-lock-fill small"></i></span>
											</div>
										</td>
									</tr>
								</tbody>
							</table>

							<div class="mt-4 p-3 rounded bg-primary bg-opacity-10 border-start border-4 border-primary">
								<h6 class="fw-bold small mb-2 text-primary"><i class="bi bi-info-circle-fill me-1"></i> 정보 수정 안내</h6>
								<ul class="mb-0 extra-small text-secondary list-unstyled lh-base">
									<li>· 비밀번호는 영문, 숫자 조합으로 4자 이상 설정을 권장합니다.</li>
									<li>· 소속 부서 및 기본 권한 정보는 시스템 관리자만 변경 가능합니다.</li>
									<li>· 사진을 변경하거나 정보를 수정한 후에는 반드시 상단의 <strong>[저장]</strong> 버튼을 눌러야 최종 반영됩니다.</li>
								</ul>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useAlerts } from '@/composables/useAlerts'
import { API_URL } from '@/config/api'
import AppAlert from '@/components/AppAlert.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const isUploading = ref(false)
const localImagePreview = ref('')
const originalPw = ref('') // 🚀 [보정] 원본 해시 암호 보관용

const formData = reactive({
	actkind: 'S1', cmpycd: authStore.cmpycd, userid: authStore.userid,
	usernm: '', pw: '', pw_c: '', telno: '', hpno: '', email: '',
	inner_no: '', deptcd: '', deptnm: '', pricegbn: '1', useyn: 'Y', photo_path: ''
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

async function fetchInfo() {
	try {
        // 🚀 XML에 inner_no, photo_path가 추가되었으므로 모든 파라미터 키를 채워 보냅니다.
		const res = await api.post('/haba/HABA_910U_STR', {
			actkind: 'S1', cmpycd: authStore.cmpycd, userid: authStore.userid,
            usernm: '', deptcd: '', deptnm: '', pw: '', telno: '', hpno: '', email: '',
            useyn: 'Y', pricegbn: '1', photo_path: '', inner_no: ''
		})
		if (res.data && res.data.length > 0) {
			const d = res.data[0]
			// 🚀 대소문자 및 언더바(_) 포함 컬럼명 매핑 강화 (PHOTO_PATH -> photo_path)
			Object.keys(d).forEach(key => {
				const lowKey = key.toLowerCase()
				formData[lowKey] = d[key]
				// 언더바 제거 버전도 매핑 (DB 컬럼이 PHOTO_PATH인 경우 photo_path로 들어오도록)
				const noUnderKey = lowKey.replace(/_/g, '')
				if (noUnderKey !== lowKey) {
					formData[noUnderKey] = d[key]
				}
			})
			// 🚀 [보정] 조회된 암호화된 비밀번호(해시)를 별도로 보관하고 입력창은 비웁니다.
			// 저장 시 입력값이 없으면 이 originalPw를 다시 서버로 보냅니다.
			originalPw.value = d.pw || d.passwd || ''
			formData.pw = ''
			formData.pw_c = ''
			localImagePreview.value = ''
		}
	} catch (e) {
        console.error('❌ [HABA910U] Load Error:', e)
        vAlertError('개인 정보 로드 실패. 서버 로그를 확인하세요.')
    }
}

async function save() {
	if (!formData.usernm) return vAlertError('성명을 입력하세요.')
	if (formData.pw && formData.pw !== formData.pw_c) return vAlertError('비밀번호 확인이 일치하지 않습니다.')

	try {
		// 🚀 [보정] 비밀번호 수정 여부 플래그 추가
		const payload = { ...formData, pw_edit_yn: 'N' }
		if (formData.pw && formData.pw.trim() !== '') {
			payload.pw_edit_yn = 'Y'
		} else {
			payload.pw = originalPw.value
		}

		const res = await api.post('/haba/HABA_910U_STR', {
			...payload,
			actkind: 'U1'
		})

		const result = res.data?.[0] || {}
		const isSuccess = String(result.result || result.res || '').toUpperCase() === 'OK'

		if (isSuccess) {
			vAlert('✅ 정보가 성공적으로 저장되었습니다.')
			// Store 데이터 실시간 동기화
			authStore.setUserInfo({
				...authStore.$state,
				usernm: formData.usernm,
				photo_path: formData.photo_path,
				email: formData.email,
				inner_no: formData.inner_no
			})
			await fetchInfo()
		} else {
			vAlertError(result.msg || '저장 처리 중 오류가 발생했습니다.')
		}
	} catch (e) { vAlertError('저장 통신 실패') }
}

async function handleImageUpload(e: any) {
	const file = e.target.files[0]
	if (!file) return

	const reader = new FileReader()
	reader.onload = (event: any) => { localImagePreview.value = event.target.result }
	reader.readAsDataURL(file)

	isUploading.value = true
	try {
		const uploadFormData = new FormData()
		uploadFormData.append('file', file)
		uploadFormData.append('userid', formData.userid)
		// 🚀 [보정] 서버 컨트롤러가 요구하는 cmpycd 파라미터를 추가합니다.
		uploadFormData.append('cmpycd', authStore.cmpycd)
		const res = await api.post('/comm/upload/profile', uploadFormData)
		if (res.data?.filepath) {
			formData.photo_path = res.data.filepath
			vAlert('✅ 사진이 임시 업로드되었습니다. 저장 버튼을 눌러야 반영됩니다.')
		}
	} catch (e) {
		vAlertError('❌ 사진 업로드 실패')
		localImagePreview.value = ''
	} finally {
		isUploading.value = false
	}
}

function triggerFileUpload() {
	const input = document.querySelector('input[type="file"]') as HTMLInputElement
	if (input) input.click()
}

onMounted(() => {
	fetchInfo()
})
</script>

<style scoped>
.erp-container { font-family: 'Pretendard', sans-serif; letter-spacing: -0.02rem; }
.extra-small { font-size: 11px; }
.btn-xs { padding: 2px 8px; font-size: 11px; }
.profile-photo-wrapper { transition: all 0.2s; border: 2px solid #fff; }
.profile-photo-wrapper:hover { border-color: #0d6efd; }
.photo-edit-btn:hover { background-color: #004a87 !important; }
.erp-table-dense th { padding: 8px !important; }
</style>