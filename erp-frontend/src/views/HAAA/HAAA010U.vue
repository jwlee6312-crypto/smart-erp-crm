<!--
	=============================================================
	프로그램명	: 코드정보 관리 (haaa010u)
	작성일자	: 2025.03.14
	설명        : 시스템 공통 코드 그룹 및 세부 코드 정보 관리 (완전 소문자 원칙 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showalert" :error="showerror" :message="alertmessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-tags-fill me-2 text-primary" style="font-size: 18px;"></i>
				시스템 관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">코드정보 관리 (haaa010u)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="search">조회</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
				<button class="btn-erp btn-delete" @click="deletedata" :disabled="formdata.actkind !== 'u1'">삭제</button>
		<!--		<button class="btn-erp btn-excel" @click="excel">엑셀</button>
				<button class="btn-erp btn-print" @click="print">인쇄</button> -->
			</div>
		</div>

		<!-- 💡 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-hidden d-flex flex-column gap-2 p-2 bg-light">

			<!-- 🔍 2. 상단 상세 입력 영역 -->
			<div class="card border-0 shadow-sm flex-shrink-0 overflow-hidden" style="border-radius: 8px;">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
					<i class="bi bi-pencil-square me-2 text-primary"></i>
					<span class="fw-bold small text-dark">코드 상세 정보 등록/수정</span>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 80px;" /><col />
							<col style="width: 80px;" /><col />
							<col style="width: 80px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required">코드유형</th>
								<td>
									<select v-model="formdata.cdtype" class="form-select" @change="searchdetails(formdata.cdtype)">
										<option v-for="grp in groupoptions" :key="grp.codecd" :value="grp.codecd">{{ grp.codenm }}</option>
									</select>
								</td>
								<th class="required">코 드</th>
								<td><input v-model="formdata.codecd" type="text" class="form-control fw-bold text-primary text-center" maxlength="3" :readonly="formdata.actkind === 'u1'" /></td>
								<th class="required">코드명칭</th>
								<td><input v-model="formdata.codenm" type="text" class="form-control" placeholder="코드 명칭 입력" /></td>
							</tr>
							<tr>
								<th>비 고</th>
								<td colspan="3"><input v-model="formdata.remark" type="text" class="form-control" /></td>
								<th>순서/사용</th>
								<td>
									<div class="d-flex align-items-center gap-2 px-2">
										<input v-model="formdata.dspord" type="number" class="form-control text-end" style="width: 60px;" />
										<div class="form-check form-switch m-0 ms-2">
											<input v-model="formdata.useyn" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="useyn010">
											<label class="form-check-label ms-1 small fw-bold" for="useyn010">사용</label>
										</div>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 📊 3. 하단 분할 그리드 영역 -->
			<div class="d-flex flex-row gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">
				<!-- 좌측: 코드 그룹 -->
				<div class="card border-0 shadow-sm d-flex flex-column" style="width: 350px; border-radius: 8px;">
					<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center">
						<i class="bi bi-list-stars me-2 text-primary"></i>
						<span class="fw-bold small text-dark">코드 그룹 리스트</span>
					</div>
                  <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                    <div ref="groupgridelement" class="tabulator-instance flex-grow-1"></div>
                  </div>
				</div>
				<!-- 우측: 상세 코드 -->
				<div class="card border-0 shadow-sm d-flex flex-column flex-grow-1" style="border-radius: 8px;">
					<div class="card-header bg-white py-2 px-3 border-bottom d-flex justify-content-between align-items-center">
						<div>
							<i class="bi bi-check2-square me-2 text-primary"></i>
							<span class="fw-bold small text-dark">상세 코드 리스트</span>
						</div>
						<span class="text-muted small" style="font-size: 11px;">항목 클릭 시 수정 모드로 전환됩니다.</span>
					</div>
                    <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                      <div ref="listgridelement" class="tabulator-instance flex-grow-1"></div>
                    </div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import * as XLSX from 'xlsx'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useAlerts } from '@/composables/useAlerts'
import { useFormReset } from '@/composables/useFormReset'
import AppAlert from '@/components/AppAlert.vue'

const authstore = useAuthStore()
const { showAlert: showalert, showError: showerror, alertMessage: alertmessage, vAlert: valert, vAlertError: valerterror } = useAlerts()
const { resetForm: resetform } = useFormReset()

const groupoptions = ref<any[]>([])
const formdata = reactive({ actkind: 'i1', cdtype: '000', codecd: '', codenm: '', dspord: '1', remark: '', useyn: 'Y' })

const groupgridelement = ref<HTMLElement | null>(null); const listgridelement = ref<HTMLElement | null>(null)
let groupgrid: Tabulator | null = null; let listgrid: Tabulator | null = null

async function search() {
	try {
		const res = await api.post('/haaa/HAAA_010U_STR', { actkind: 'S1', cdtype: '000', cmpycd: authstore.cmpycd })
		const processed = res.data || [];
		groupoptions.value = processed; groupgrid?.setData(processed)
		if (processed.length > 0 && !formdata.cdtype) {
			formdata.cdtype = processed[0].codecd
			searchdetails(formdata.cdtype)
		}
		valert('조회되었습니다.')
	} catch (e) { valerterror('그룹 로드 실패') }
}

async function searchdetails(cdtype: string) {
	try {
		const res = await api.post('/haaa/HAAA_010U_STR', { actkind: 'S3', cdtype: cdtype, cmpycd: authstore.cmpycd })
		const processed = res.data || [];
		listgrid?.setData(processed)
	} catch (e) { console.error('상세 목록 로드 실패') }
}

async function save() {
	if (!formdata.codecd || !formdata.codenm) return valerterror('필수항목을 입력하세요.')
	try {
		await api.post('/haaa/HAAA_010U_STR', { ...formdata, userid: authstore.userid, cmpycd: authstore.cmpycd })
		valert('저장되었습니다.')
		searchdetails(formdata.cdtype)
		resetinputform()
	} catch (e) { valerterror('저장 실패') }
}

async function deletedata() {
	if (!formdata.codecd) return valerterror('삭제할 코드를 선택하세요.')
	if (!confirm('선택한 코드 정보를 삭제하시겠습니까?')) return
	try {
		await api.post('/haaa/HAAA_010U_STR', { ...formdata, actkind: 'd1', userid: authstore.userid, cmpycd: authstore.cmpycd })
		valert('삭제되었습니다.')
		searchdetails(formdata.cdtype)
		resetinputform()
	} catch (e) { valerterror('삭제 실패') }
}

function resetinputform() {
	const currenttype = formdata.cdtype
	resetform(formdata)
	formdata.cdtype = currenttype; formdata.actkind = 'i1'; formdata.dspord = '1'; formdata.useyn = 'Y'
}

function initialize() {
	resetform(formdata); formdata.cdtype = '000'; formdata.actkind = 'i1'; formdata.dspord = '1'; formdata.useyn = 'Y'
	listgrid?.clearData()
	search()
}

function excel() {
	listgrid?.download("xlsx", `코드정보_${formdata.cdtype}_${new Date().toISOString().slice(0, 10)}.xlsx`)
}

function print() {
	// window.open(`/haaa/HAAA_010P?cdtype=${formdata.cdtype}&cmpycd=${authstore.cmpycd}`)
}

onMounted(async () => {
	if (typeof window !== 'undefined') (window as any).XLSX = XLSX
	if (groupgridelement.value) {
		groupgrid = new Tabulator(groupgridelement.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			columnDefaults: { headerSort: false, headerHozAlign: 'center', vertAlign: "middle" },
			columns: [
				{ title: '유형코드', field: 'codecd', hozAlign: 'center', width: 80 },
				{ title: '그룹명', field: 'codenm', widthGrow: 1, cssClass: 'fw-bold text-primary' }
			]
		})
		groupgrid.on('rowClick', (e, row) => {
			const d = row.getData()
			formdata.cdtype = d.codecd
			searchdetails(d.codecd)
			resetinputform()
		})
	}
	if (listgridelement.value) {
		listgrid = new Tabulator(listgridelement.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			placeholder: '데이터가 없습니다.',
			columnDefaults: { headerSort: false, headerHozAlign: 'center', vertAlign: "middle" },
			columns: [
				{ title: '코드', field: 'codecd', hozAlign: 'center', width: 80, cssClass: 'fw-bold text-primary' },
				{ title: '명칭', field: 'codenm', minWidth: 200, widthGrow: 1, cssClass: 'fw-bold', hozAlign: 'left' },
				{ title: '순서', field: 'dspord', hozAlign: 'center', width: 70 },
				{ title: '사용', field: 'useyn', hozAlign: 'center', width: 70, formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
			]
		})
		listgrid.on('rowClick', (e, row) => {
			const d = row.getData();
			Object.assign(formdata, d)
			formdata.actkind = 'u1'
		})
	}
	nextTick(() => search())
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
