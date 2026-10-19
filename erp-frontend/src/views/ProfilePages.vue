<!--관리정보/사용자정보/사용자등록-->
<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	<div class="mb-2 btn-line">
		<button @click="save">저장</button>
	</div>
	<table class="table shadow-sm" style="table-layout: fixed">
		<colgroup>
			<col style="width: 10%" />
			<col />
			<col style="width: 10%" />
			<col />
			<col style="width: 10%" />
			<col />
			<col style="width: 10%" />
			<col />
		</colgroup>
		<tbody>
			<tr>
				<th>이름</th>
				<td>
					<input
						v-model="form_02.usernm"
						type="text"
						class="form-control form-control-sm w-100"
						readonly
						autocomplete="off"
					/>
				</td>
				<th>아이디</th>
				<td>
					<input
						v-model="form_02.update_userid"
						type="hidden"
						class="form-control form-control-sm w-50"
					/>
					<input
						v-model="form_02.userid"
						type="text"
						class="form-control form-control-sm w-100"
						disabled
						autocomplete="username"
					/>
				</td>
				<th>비밀번호 변경</th>
				<td>
					<input
						v-model="form_02.pw"
						type="password"
						class="form-control form-control-sm w-100"
						autocomplete="new-password"
					/>
				</td>
				<th>비밀번호 확인</th>
				<td>
					<input
						v-model="form_02.pwcheck"
						type="password"
						class="form-control form-control-sm w-100"
						autocomplete="new-password"
					/>
				</td>
			</tr>
			<tr>
				<th>핸드폰</th>
				<td>
					<input v-model="form_02.hpno" type="text" class="form-control form-control-sm w-100" />
				</td>
				<th>연락처</th>
				<td>
					<input v-model="form_02.telno" type="text" class="form-control form-control-sm w-100" />
				</td>
				<th>메일주소</th>
				<td colspan="3">
					<input v-model="form_02.email" type="text" class="form-control form-control-sm w-100" />
				</td>
			</tr>
			<tr>
				<th>소속부서</th>
				<td>
					<div class="d-flex gap-1">
						<input v-model="form_02.deptcd" type="hidden" class="form-control form-control-sm" />
						<input
							v-model="form_02.deptnm"
							type="text"
							class="form-control form-control-sm w-100"
							readonly
						/>
					</div>
				</td>
				<th>사용자그룹</th>
				<td>
					<select v-model="form_02.usergrp" class="form-select form-select-sm w-100" disabled>
						<option value="" selected>선택</option>
						<option v-for="item in usergrpData" :key="item.codecd" :value="item.codecd">
							{{ item.codenm }}
						</option>
					</select>
				</td>
				<th>직위</th>
				<td colspan="3">
					<select v-model="form_02.position" class="form-select form-select-sm w-30" disabled>
						<option value="" selected>선택</option>
						<option v-for="item in positionData" :key="item.codecd" :value="item.codecd">
							{{ item.codenm }}
						</option>
					</select>
				</td>
			</tr>
			<tr>
				<th>외부 직원</th>
				<td>
					<div class="d-flex gap-1">
						<input
							v-model="form_02.custcd"
							type="hidden"
							class="form-control form-control-sm w-20"
						/>
						<input
							v-model="form_02.custnm"
							type="text"
							class="form-control form-control-sm w-100"
							disabled
						/>
					</div>
				</td>
				<th>외부 사용구분</th>
				<td>
					<select v-model="form_02.outusecd" class="form-select form-select-sm w-100" disabled>
						<option value="">선택</option>
						<option value="1">유지보수</option>
						<option value="2">판매대행</option>
						<option value="3">기타</option>
					</select>
				</td>
				<th>내선</th>
				<td>
					<input
						v-model="form_02.inner_no"
						type="text"
						class="form-control form-control-sm w-100"
						readonly
					/>
				</td>
				<th>사번</th>
				<td>
					<input
						v-model="form_02.empno"
						type="text"
						class="form-control form-control-sm w-100"
						readonly
					/>
				</td>
			</tr>
			<tr>
				<th style="height: 300px">프로필 사진</th>
				<td colspan="2">
					<input
						type="file"
						class="form-control w-50"
						accept=".jpg, .png, .jpeg"
						@change="onFileChange"
					/>
					<div style="color: #dc3b3b; font-size: 12px">
						( jpg, jpeg, png ) 용량제한 {{ maxSizeMB }}MB
					</div>

					<!-- ✅ 미리보기 -->
					<div v-if="previewLogo" class="my-2">
						<img :src="previewLogo" alt="미리보기" style="max-width: 200px" />
					</div>
					<div v-else-if="loadingLogo">
						<div
							class="spinner-border text-primary"
							role="status"
							style="width: 2rem; height: 2rem"
						>
							<span class="visually-hidden">Loading...</span>
						</div>
					</div>
					<img
						v-else-if="logoPreviewUrl"
						:src="logoPreviewUrl"
						alt="로고 미리보기"
						class="preview-img"
					/>
				</td>
			</tr>
		</tbody>
	</table>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useAlerts } from '@/composables/useAlerts'
import { fetchSelectData, type SelectData } from '@/composables/useFetchSelectData'
import { useSearch } from '@/composables/useSearch'
import AppAlert from '@/components/AppAlert.vue'
import { useFormReset } from '@/composables/useFormReset'
import { useSave } from '@/composables/useSave'
import { useAuthStore } from '@/stores/authStore'
import axios from 'axios'
import { API_URL } from '@/config/api'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { searchStart } = useSearch()
const { saveBody } = useSave()
const { resetForm } = useFormReset() // form 값 지우는 컴포저블함수

interface Form_02 {
	pwcheck: string | null
	userid: string
	usernm: string
	pw: string | null
	telno: string
	hpno: string
	inner_no: string
	deptcd: string
	deptnm: string
	empno: string
	usergrp: string
	position: string
	email: string
	set_asnyn: string
	outyn: string
	custcd: string
	custnm: string
	outusecd: string
	update_userid: string
}

const form_02 = reactive(<Form_02>{
	pwcheck: null,
	userid: '',
	usernm: '',
	pw: null,
	telno: '',
	hpno: '',
	inner_no: '',
	deptnm: '',
	empno: '',
	usergrp: '',
	position: '',
	email: '',
	set_asnyn: '',
	outyn: '',
	custcd: '',
	custnm: '',
	outusecd: '',
	update_userid: '',
})

// ✅ 이미지관련
const loadingLogo = ref(false)
const logoPreviewUrl = ref<string>('')
const originalPw = ref('') // 🚀 [보정] 원본 해시 암호 보관용
const previewLogo = ref<string | null>(null)
const logoImage = ref<File | null>(null)
const maxSizeMB = ref(4)

// ✅ 셀렉트 박스
const usergrpData = ref<SelectData[]>([])
const positionData = ref<SelectData[]>([])

const loadSelectData = async () => {
	usergrpData.value = await fetchSelectData('040')
	positionData.value = await fetchSelectData('700')
}
onMounted(async () => {
	await loadSelectData()
	await nextTick()
	await search()
})

// ✅ 조회 버튼
async function search() {
	const path = '/user/info'
	const data = {
		userid: authStore.userid,
	}
	try {
		const res = await searchStart(path, data)
		Object.assign(form_02, res)

		// 🚀 [보정] 조회된 원본 해시 암호를 보관하고 입력창은 비웁니다.
		originalPw.value = res.pw || res.passwd || ''
		form_02.pw = ''
		form_02.pwcheck = ''
		await imgSearch()
	} catch (error) {
		console.error(error)
	}
}

// ✅파일선택
function onFileChange(event: Event) {
	const target = event.target as HTMLInputElement
	const file = target.files?.[0]

	if (file) {
		const fileSizeMB = file.size / (1024 * 1024)
		if (fileSizeMB > maxSizeMB.value) {
			alert(`파일 용량이 ${maxSizeMB.value}MB를 초과했습니다.`)
			target.value = ''
			return
		}
		logoImage.value = file // ✅ 추가
		previewLogo.value = URL.createObjectURL(file)
	}
}

// ✅ 저장 (폼 테이블만)
async function save() {
	if (!form_02.userid) return vAlertError('아이디를 입력하세요.')
	if (!form_02.usernm) return vAlertError('이름을 입력하세요.')
	if (form_02.update_userid === '') {
		if (!form_02.pwcheck) return vAlertError('비밀번호를 입력하세요.')
		if (form_02.pwcheck !== form_02.pw) return vAlertError('비밀번호가 일치하지 않습니다.')
	} else {
		if (form_02.pw === null) {
			return vAlertError('비밀번호를 입력해주세요')
		}
	}

	const path = '/user/save'

	// 🚀 [보정] 비밀번호 입력이 없으면 원본 해시값을 그대로 사용
	const payload = { ...form_02 }
	if (!form_02.pw || form_02.pw.trim() === '') {
		payload.pw = originalPw.value
	}

	try {
		await saveBody(path, payload)
		await saveImg()

		// 🚀 [해결] 저장 후 authStore의 사진 경로를 즉시 업데이트하여 사이드바에 반영
		if (form_02.photo_path) {
			authStore.photo_path = form_02.photo_path
		}

		resetForm(form_02)
		await search()
		vAlert('저장 성공')
	} catch (error) {
		console.error(error)
	}
}

// ✅ 이미지 저장
async function saveImg() {
	if (!logoImage.value) return;

	const data = new FormData()
	data.append('file', logoImage.value)

	// 🚀 [해결 핵심] Multipart 데이터와 일반 파라미터 혼선 방지를 위해
	// userid와 cmpycd를 URL 쿼리 파라미터로 명시적으로 전달합니다.
	const userid = authStore.userid;
	const cmpycd = (authStore.cmpycd || '').toUpperCase();

	try {
		console.log('📸 프로필 이미지 업로드 시도:', userid, cmpycd)
		const res = await api.post(`/comm/upload/profile?userid=${userid}&cmpycd=${cmpycd}`, data, {
			headers: { 'Content-Type': 'multipart/form-data' }
		})

		if (res.data && res.data.filepath) {
			form_02.photo_path = res.data.filepath
		}
	} catch (error: any) {
		console.error('업로드 실패:', error)
		vAlertError('이미지 업로드 실패: ' + (error.response?.data || error.message))
	}
}

// ✅ 이미지 미리보기
async function imgSearch() {
	loadingLogo.value = true
	await nextTick()
	try {
		if (form_02.photo_path) {
			// 💡 [최종 보정] 조회 경로를 /api/storage/ 로 명시적으로 통일
			const cmpycd = (authStore.cmpycd || 'COIT').toUpperCase()
			logoPreviewUrl.value = `/api/storage/${cmpycd}/profile/${form_02.photo_path}`
		}
	} catch (err) {
		console.error('로고 이미지 실패', err)
	} finally {
		loadingLogo.value = false
	}
}

onUnmounted(() => {
	URL.revokeObjectURL(logoPreviewUrl.value)
})
</script>

<style scoped>
.preview-img {
	max-width: 200px;
	height: auto;
	border: 1px solid #ddd;
}
</style>
