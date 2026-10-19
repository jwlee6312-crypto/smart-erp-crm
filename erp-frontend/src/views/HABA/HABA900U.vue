<!--기본정보/회사정보관리 [ERP 프리미엄 고밀도 표준 적용] -->
<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom shadow-sm bg-white py-2 sticky-top">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-building me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">회사정보관리 (HABA900U)</span>
			</div>
			<div class="btn-group-erp pe-3">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchCompanyInfo">조회</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
			</div>
		</div>

		<!-- 💡 메인 컨텐츠 영역 (섹션 간 간격 확보: gap-3) -->
		<div class="flex-grow-1 overflow-auto d-flex flex-column gap-3 p-3">

			<!-- 💡 2. 회사 상세 정보 카드 (3열 고밀도 밸런스 레이아웃) -->
			<div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
				<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center">
					<i class="bi bi-pencil-square me-2 text-secondary"></i>
					<span class="fw-bold small text-dark">회사 기본 정보 상세 관리</span>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 120px;" /><col />
							<col style="width: 120px;" /><col />
							<col style="width: 120px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th>회사코드</th>
								<td><input v-model="formData.cmpycd" type="text" class="form-control bg-light fw-bold text-primary text-center" readonly /></td>
								<th class="required">회사명(상호)</th>
								<td><input v-model="formData.ltdnm" type="text" class="form-control" placeholder="상호 입력" /></td>
								<th>영문상호</th>
								<td><input v-model="formData.ltdenm" type="text" class="form-control" /></td>
							</tr>
							<tr>
								<th class="required">사업자번호</th>
								<td><input v-model="formData.saupno" type="text" class="form-control text-center" maxlength="10" /></td>
								<th>대표자명</th>
								<td><input v-model="formData.bossnm" type="text" class="form-control" /></td>
								<th class="required">법인번호</th>
								<td><input v-model="formData.legalno" type="text" class="form-control text-center" maxlength="13" /></td>
							</tr>
							<tr>
								<th>업태</th>
								<td><input v-model="formData.uptae" type="text" class="form-control" /></td>
								<th>종목</th>
								<td><input v-model="formData.upjong" type="text" class="form-control" /></td>
								<th>연락처</th>
								<td><input v-model="formData.telno" type="text" class="form-control text-center" /></td>
							</tr>
							<tr>
								<th class="required">본사주소</th>
								<td colspan="3">
                  <AddressPopupForm
                    v-model:postno="formData.postno"
                    v-model:address="formData.address"
                    v-model:d_address="formData.d_address"
                  />
								</td>
								<th>도메인</th>
								<td><input v-model="formData.domain" type="text" class="form-control" /></td>
							</tr>
							<tr>
								<th class="required">결산월</th>
								<td>
									<select v-model="formData.clsmm" class="form-select">
										<option v-for="m in 12" :key="m" :value="String(m).padStart(2, '0')">{{ m }}월</option>
									</select>
								</td>
								<th>설립일자</th>
								<td><input v-model="formData.fondymd" type="date" class="form-control" /></td>
								<th>사용여부</th>
								<td class="bg-light-subtle">
									<div class="form-check form-switch m-0 d-flex align-items-center justify-content-center h-100">
										<input v-model="formData.useyn" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="useYnSwitch900">
										<label class="form-check-label ms-2 small fw-bold" for="useYnSwitch900">{{ formData.useyn === 'Y' ? '사용 중' : '중지' }}</label>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<div class="flex-grow-1"></div> <!-- 하단 공간 확보용 -->
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import AppAlert from '@/components/AppAlert.vue'
import AddressPopupForm from '@/components/AddressPopupForm.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const formData = reactive({
	actkind: 'S0',
	cmpycd: authStore.cmpycd,
	ltdnm: '', ltdenm: '', saupno: '', bossnm: '', legalno: '',
	postno: '', address: '', d_address: '', uptae: '', upjong: '', telno: '',
	fondymd: '', clsmm: '12', domain: '', useyn: 'Y'
})

async function fetchCompanyInfo() {
	try {
		// 💡 신규 API 경로 및 프로시저 호출 (/haba/HABA_900U_STR)
		const res = await api.post('/haba/HABA_900U_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd
		})
		if (res.data && res.data.length > 0) {
      const data = res.data[0];
      // 매핑 (상세주소 필드명 통일)
      if (data.address_det) data.d_address = data.address_det;
			Object.assign(formData, data)
			if (formData.fondymd && formData.fondymd.length === 8) {
				formData.fondymd = `${formData.fondymd.substring(0, 4)}-${formData.fondymd.substring(4, 6)}-${formData.fondymd.substring(6, 8)}`
			}
		}
	} catch (e) { vAlertError('회사 정보 로드 실패') }
}

async function save() {
	if (!formData.ltdnm || !formData.saupno) return vAlertError('상호와 사업자번호는 필수입니다.')
	try {
		const param = {
			...formData,
			actkind: 'U0',
			saupno: formData.saupno.replace(/-/g, ''),
			legalno: formData.legalno.replace(/-/g, ''),
			fondymd: formData.fondymd ? formData.fondymd.replace(/-/g, '') : '',
			userid: authStore.userid,
      address_det: formData.d_address // 서버 필드명에 맞게 전달
		}
		await api.post('/haba/HABA_900U_STR', param)
		vAlert('저장되었습니다.')
		fetchCompanyInfo()
	} catch (e) { vAlertError('저장 실패') }
}

function initialize() {
	resetForm(formData)
	formData.actkind = 'S0'; formData.cmpycd = authStore.cmpycd; formData.clsmm = '12';
}

onMounted(() => {
	fetchCompanyInfo()
})
</script>
