<!--기본정보/환경설정 [ERP 프리미엄 고밀도 표준 - 원본 ASP 기능 완전 복구 버전] -->
<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 shadow-sm sticky-top">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-gear-wide-connected me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">결재 및 회사 환경설정 (HABA100U)</span>
			</div>
			<div class="btn-group-erp pe-3">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchConfig">조회</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
			</div>
		</div>

		<!-- 💡 2. 메인 설정 영역 -->
		<div class="flex-grow-1 overflow-auto p-3 d-flex flex-column gap-3">

			<!-- 🅰️ 결재 및 마감 통제 -->
			<div class="card border-0 shadow-sm overflow-hidden">
				<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center">
					<i class="bi bi-pen-fill me-2 text-secondary"></i>
					<span class="fw-bold small text-dark">결재 및 마감 통제 설정</span>
				</div>
				<div class="card-body p-0">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 120px;" /><col />
							<col style="width: 120px;" /><col />
							<col style="width: 120px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required">마감 기준일</th>
								<td>
									<div class="d-flex align-items-center gap-1 flex-nowrap">
										<input v-model="uiDate.yy" type="text" class="form-control text-center fw-bold" style="width: 70px;" maxlength="4" />
										<span class="small">년</span>
										<input v-model="uiDate.mm" type="text" class="form-control text-center fw-bold" style="width: 50px;" maxlength="2" />
										<span class="small">월</span>
										<input v-model="uiDate.dd" type="text" class="form-control text-center fw-bold" style="width: 50px;" maxlength="2" />
										<span class="small">일</span>
									</div>
								</td>
								<th>장부 반영</th>
								<td>
									<select v-model="formData.slipyn" class="form-select">
										<option value="N">미사용 (승인 후 반영)</option>
										<option value="Y">즉시반영 (자동 발행 시)</option>
									</select>
								</td>
								<th>비용예산 통제</th>
								<td>
									<select v-model="formData.bgtype" class="form-select">
										<option value="000">관리안함</option>
										<option v-for="opt in bgOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
									</select>
								</td>
							</tr>
							<tr>
								<th>결재라인 명칭</th>
								<td colspan="5">
									<div class="d-flex gap-2 flex-nowrap">
										<div class="input-group input-group-sm flex-nowrap" v-for="n in 5" :key="n">
											<span class="input-group-text bg-light">{{ n }}차</span>
											<input v-model="formData['gline'+n]" type="text" class="form-control" placeholder="명칭" />
										</div>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 🅱️ 물류 및 운영 로직 -->
			<div class="card border-0 shadow-sm overflow-hidden">
				<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center">
					<i class="bi bi-box-seam-fill me-2 text-secondary"></i>
					<span class="fw-bold small text-dark">운영 로직 및 물류 환경설정</span>
				</div>
				<div class="card-body p-0">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 120px;" /><col />
							<col style="width: 120px;" /><col />
							<col style="width: 120px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th>재고 평가방법</th>
								<td>
									<select v-model="formData.stkgbn" class="form-select">
										<option value="100">총평균법</option>
										<option value="200">이동평균법</option>
										<option value="300">선입선출법</option>
									</select>
								</td>
								<th>매출 단가기준</th>
								<td>
									<select v-model="formData.pricegbn" class="form-select">
										<option value="1">공급가액 기준</option>
										<option value="2">공급가+부가세 합산</option>
									</select>
								</td>
								<th>재고부족 통제</th>
								<td>
									<select v-model="formData.stokyn" class="form-select">
										<option value="Y">부족시 차단</option>
										<option value="N">마이너스 허용</option>
									</select>
								</td>
							</tr>
							<tr>
								<th>생산 연동</th>
								<td>
									<select v-model="formData.mnfyn" class="form-select">
										<option value="N">제조안함</option>
										<option value="Y">제조업체</option>
									</select>
								</td>
								<th>회계 연동</th>
								<td>
									<select v-model="formData.outacctyn" class="form-select">
										<option value="N">자체회계</option>
										<option value="Y">외부연동</option>
									</select>
								</td>
								<th>카드 대행사</th>
								<td>
									<div class="input-group input-group-sm">
										<input v-model="formData.cardcustnm" class="form-control fw-bold text-primary" readonly placeholder="대행사 선택" />
										<button class="btn btn-outline-secondary px-2" @click="popVisible.cust = true"><i class="bi bi-search"></i></button>
									</div>
								</td>
							</tr>
							<tr>
								<th>여신 확인</th>
								<td>
									<div class="form-check form-switch m-0 d-flex align-items-center justify-content-center h-100">
										<input v-model="formData.yeosinyn" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="yeosinSwitch">
										<label class="form-check-label ms-2 small fw-bold" for="yeosinSwitch">사용</label>
									</div>
								</td>
								<th>출고 확정</th>
								<td>
									<div class="form-check form-switch m-0 d-flex align-items-center justify-content-center h-100">
										<input v-model="formData.iocnfmyn" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="iocnfmSwitch">
										<label class="form-check-label ms-2 small fw-bold" for="iocnfmSwitch">필수</label>
									</div>
								</td>
								<th>상담 AI 요약</th>
								<td>
									<div class="form-check form-switch m-0 d-flex align-items-center justify-content-center h-100">
										<input v-model="formData.ai_mode" class="form-check-input mt-0" type="checkbox" true-value="auto" false-value="manual" id="aiModeSwitch">
										<label class="form-check-label ms-2 small fw-bold text-primary" for="aiModeSwitch">사용</label>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 💡 3. 하단 이미지 영역: 로고 및 직인 (삭제 기능 포함) -->
			<div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
				<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
					<div><i class="bi bi-image me-2 text-secondary"></i><span class="fw-bold small text-dark">회사 인장 및 로고 관리</span></div>
					<span class="badge bg-light text-secondary border fw-normal">90x25 / 70x70 규격 권장</span>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 120px;" /><col />
							<col style="width: 120px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th>회사 로고</th>
								<td>
									<div class="d-flex align-items-center gap-2 flex-nowrap">
										<input type="file" class="form-control" style="max-width: 250px;" @change="e => onFileChange(e, 'logoimg')" />
										<div v-if="formData.logoimg" class="d-flex align-items-center gap-2 border rounded p-1 bg-light">
											<img :src="getImageUrl(formData.logoimg, 'logoimg')" height="25" />
											<button class="btn btn-xs btn-outline-danger border-0 p-0 px-1" title="삭제" @click="handleDeleteImage('D1')"><i class="bi bi-trash-fill"></i></button>
										</div>
									</div>
								</td>
								<th>공인 직인</th>
								<td>
									<div class="d-flex align-items-center gap-2 flex-nowrap">
										<input type="file" class="form-control" style="max-width: 250px;" @change="e => onFileChange(e, 'stampimg')" />
										<div v-if="formData.stampimg" class="d-flex align-items-center gap-2 border rounded p-1 bg-light">
											<img :src="getImageUrl(formData.stampimg, 'stampimg')" height="50" />
											<button class="btn btn-xs btn-outline-danger border-0 p-0 px-1" title="삭제" @click="handleDeleteImage('D2')"><i class="bi bi-trash-fill"></i></button>
										</div>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>
	</div>

	<!-- 🚀 도움창 팝업 연동 -->
	<SaleCustHelp v-model:visible="popVisible.cust" @confirm="onCustConfirm" @close="restoreFocus" />
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import AppAlert from '@/components/AppAlert.vue'
import SaleCustHelp from '@/components/help/SaleCustHelp.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { API_URL } from '@/config/api'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const uiDate = reactive({ yy: '',mm: '', dd: '' })
const popVisible = reactive({ cust: false })
const formData = reactive<any>({
	actkind: 'S0', cmpycd: authStore.cmpycd, userid: authStore.userid,
	gline1: '', gline2: '', gline3: '', gline4: '', gline5: '',
	bgtype: '000', cardcust: '', cardcustnm: '', mnfyn: 'N', stkgbn: '100',
	stokyn: 'Y', pricegbn: '1', slipyn: 'N', yeosinyn: 'N', iocnfmyn: 'N',
	outacctyn: 'N', logoimg: '', stampimg: '', ai_mode: 'manual'
})

const restoreFocus = () => {}
const onCustConfirm = (d: any) => {
	formData.cardcust = d.custcd; formData.cardcustnm = d.custnm;
}

const getImageUrl = (filename: string, type: string) => {
	if (!filename) return ''
	const baseUrl = API_URL || window.location.origin
	const cmpycd = (authStore.cmpycd || 'COIT').toUpperCase()
	return `${baseUrl}/storage/${cmpycd}/${type}/${filename}`.replace(/([^:]\/)\/+/g, "$1")
}

const bgOptions = ref<any[]>([])

async function fetchConfig() {
	try {
		const res = await api.post('/haba/HABA_100U_STR', { actkind: 'S0', cmpycd: authStore.cmpycd })
		if (res.data && res.data.length > 0) {
			const d = res.data[0]
			Object.assign(formData, d)
			if (d.custnm) formData.cardcustnm = d.custnm;
			const rawClsymd = formData.clsymd || ''
			if (rawClsymd && rawClsymd.length >= 8) {
				uiDate.yy = rawClsymd.substring(0, 4)
				uiDate.mm = rawClsymd.substring(4, 6)
				uiDate.dd = rawClsymd.substring(6, 8)
			}
			vAlert('환경설정 정보를 로드했습니다.')
		}
	} catch (e) { vAlertError('설정 로드 실패') }
}

async function save() {
	if (!uiDate.yy || !uiDate.mm || !uiDate.dd) return vAlertError('마감 기준일을 입력하십시오.')
	const mm = String(uiDate.mm).padStart(2, '0'); const dd = String(uiDate.dd).padStart(2, '0');
	try {
		// 🚀 [표준] 사용자 확정 XML 22개 파라미터 스펙 엄수
		const param = {
			actkind: 'U0',
			cmpycd: authStore.cmpycd,
			clsymd: `${uiDate.yy}${mm}${dd}`,
			gline1: formData.gline1,
			gline2: formData.gline2,
			gline3: formData.gline3,
			gline4: formData.gline4,
			gline5: formData.gline5,
			bgtype: formData.bgtype,
			stkgbn: formData.stkgbn,
			mnfyn: formData.mnfyn,
			stokyn: formData.stokyn,
			slipyn: formData.slipyn,
			pricegbn: formData.pricegbn,
			cardcust: formData.cardcust,
			logoimg: formData.logoimg,
			stampimg: formData.stampimg,
			yeosinyn: formData.yeosinyn,
			iocnfmyn: formData.iocnfmyn,
			outacctyn: formData.outacctyn,
			ai_mode: formData.ai_mode,
			updemp: authStore.userid
		}
		await api.post('/haba/HABA_100U_STR', param)
		vAlert('환경설정이 저장되었습니다.')
		fetchConfig()
	} catch (e) { vAlertError('저장 실패') }
}

/** 🚀 [해결] 로고/직인 이미지 삭제 처리 (ASP D1, D2 로직 이식) */
async function handleDeleteImage(kind: string) {
	if (!confirm('이미지를 삭제하시겠습니까?')) return
	try {
		const param = { ...formData, actkind: kind, updemp: authStore.userid }
		await api.post('/haba/HABA_100U_STR', param)
		vAlert('이미지가 삭제되었습니다.')
		fetchConfig()
	} catch (e) { vAlertError('이미지 삭제 실패') }
}

function initialize() {
	resetForm(formData); formData.actkind = 'S0'; formData.cmpycd = authStore.cmpycd;
}

const onFileChange = async (e: any, target: string) => {
	const file = e.target.files[0]; if (!file) return
	const data = new FormData(); data.append('file', file); data.append('cmpycd', authStore.cmpycd); data.append('type', target)
	try {
		const res = await api.post('/comm/upload/company', data, { headers: { 'Content-Type': 'multipart/form-data' } })
		const filename = res.data.filename || res.data.fileName
		if (filename) { formData[target] = filename; vAlert('업로드 성공. 저장 버튼을 눌러주세요.') }
	} catch (err) { vAlertError('파일 업로드 실패') }
}

onMounted(async () => {
	// 🚀 [해결] 지시 사항 반영: 회사코드는 공백, gbncd는 '200' 지정
	api.post('/ha00/HA00_00P_STR', {
		gubun: 'E0',
		cmpycd: ' ',
		gbncd: '200',
		code: ' '
	}).then(r => {
		bgOptions.value = r.data.map((i: any) => ({
			codecd: String(i.codecd || '').trim(),
			codenm: String(i.codenm || '').trim()
		}))
	})
	fetchConfig()
})
</script>

<style scoped>
.erp-container { height: 100%; display: flex; flex-direction: column; background-color: #f8f9fa; }
.erp-table-full { width: 100%; table-layout: fixed; }
.erp-table-full th { background-color: #f1f3f5; padding: 10px; font-size: 12px; font-weight: 600; color: #495057; border: 1px solid #dee2e6; text-align: center; }
.erp-table-full td { padding: 8px; border: 1px solid #dee2e6; vertical-align: middle; background-color: #fff; }
.erp-table-full th.required::after { content: ' *'; color: #e03131; }
.btn-xs { padding: 1px 5px; font-size: 10px; }
input:focus, select:focus { border-color: #339af0; box-shadow: 0 0 0 0.2rem rgba(51, 154, 240, 0.25); outline: none; }
</style>
