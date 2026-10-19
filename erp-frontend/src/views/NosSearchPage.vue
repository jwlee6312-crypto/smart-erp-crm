<!-- NOS 조회-->
<!--
	=============================================================
	프로그램명	  : NOS 조회
  프로그램 ID	: -
	작성일자	    : 25.11.12
	작성자	      : 박현목
	수정일자     : 25.11.21
	수정자	      : 이현준
	수정 내용    : 조회 컬럼 추가
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	<div class="mb-2 btn-line">
		<button @click="initialize">초기화</button>
		<button @click="search">조회</button>
	</div>
	<table class="table shadow-sm" style="table-layout: fixed">
		<colgroup>
			<col style="width: 10%" />
			<col />
			<col style="width: 10%" />
			<col />
		</colgroup>
		<tbody>
			<tr>
				<th>년월</th>
				<td>
					<input
						v-model="form_01.ym"
						type="month"
						class="form-control form-control-sm w-100"
						maxlength="100"
					/>
				</td>
				<th>bundleCode</th>
				<td>
					<input v-model="form_01.bundleCode" class="form-control form-control-sm w-100" />
				</td>
				<th>clientCode</th>
				<td>
					<input v-model="form_01.clientCode" class="form-control form-control-sm w-100" />
				</td>
				<th>erpName</th>
				<td>
					<input v-model="form_01.erpName" class="form-control form-control-sm w-100" />
				</td>
			</tr>
		</tbody>
	</table>
	<div ref="tableRef1" class="tabulator-ref" />
</template>
<script setup lang="ts">
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { onMounted, reactive, ref } from 'vue'
import { useSearch } from '@/composables/useSearch'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import { getDate } from '@/composables/useDate'
import { useFormReset } from '@/composables/useFormReset'

const { vAlert, vAlertError, showAlert, showError, alertMessage } = useAlerts()
const { resetForm } = useFormReset()
const { searchStart } = useSearch()
const { currentMonth } = getDate()

interface Form_01 {
	ym: string
	bundleCode: string
	clientCode: string
	erpName: string
}

const form_01 = reactive<Form_01>({
	ym: currentMonth,
	bundleCode: '',
	clientCode: '',
	erpName: '',
})

const tableRef1 = ref<HTMLDivElement | null>(null)
let tableInstance1: Tabulator | null = null

onMounted(() => {
	if (!tableRef1.value) return
	tableInstance1 = new Tabulator(tableRef1.value, {
		placeholder: 'No data',
		rowHeader: {
			headerSort: false,
			resizable: false,
			frozen: true,
			headerHozAlign: 'center',
			hozAlign: 'center',
			formatter: 'rowSelection',
			titleFormatter: 'rowSelection',
			width: 40,
		},
		layout: 'fitData',
		data: [],
		columns: [
			{ title: '더존erpno', field: 'outcustcd', hozAlign: 'center', headerSort: true },
			{ title: '거래처명', field: 'custnm', hozAlign: 'left', headerSort: false },
			{ title: 'Client_Code', field: 'noscontno', hozAlign: 'left', headerSort: true },
			{ title: 'client_name', field: 'contnm', hozAlign: 'left', headerSort: true },
			{ title: 'Prod_Box', field: 'noscrowno', hozAlign: 'center', headerSort: false },
			{ title: 'bundle_code', field: 'mixcd', hozAlign: 'left', headerSort: true },
			{ title: 'printoutday', field: 'reqmonday', hozAlign: 'center', headerSort: true },
			{ title: 'type_code', field: 'type_code', hozAlign: 'center', headerSort: false },
			{ title: 'charge_code', field: 'charge_code', hozAlign: 'center', headerSort: false },
			{ title: 'type_text', field: 'itemnm', hozAlign: 'left', headerSort: false },
			{ title: 'charge_title', field: 'sitemnm', hozAlign: 'left', headerSort: false },
			{ title: 'sortorder', field: 'sortorder', hozAlign: 'left', headerSort: false },
			{ title: 'currency', field: 'currency', hozAlign: 'center', headerSort: false },
			{ title: 'admissprice', field: 'contamt', hozAlign: 'center', headerSort: false },
			{ title: 'prod_id', field: 'prod_id', hozAlign: 'center', headerSort: true },
			{ title: 'use_begin_date', field: 'startdate', hozAlign: 'center', headerSort: false },
			{ title: 'use_end_date', field: 'enddate', hozAlign: 'center', headerSort: false },
			{ title: 'charge_begin_date', field: 'startymd', hozAlign: 'center', headerSort: false },
			{ title: 'charge_end_date', field: 'endymd', hozAlign: 'center', headerSort: false },
			{ title: 'charge_div', field: 'charge_div', hozAlign: 'center', headerSort: false },
			{ title: 'isvat', field: 'isvat', hozAlign: 'center', headerSort: false },
			{ title: 'isautodc', field: 'isautodc', hozAlign: 'center', headerSort: false },
			{ title: 'dcratio', field: 'dcratio', hozAlign: 'center', headerSort: false },
			{ title: 'DCAmt', field: 'dcamt', hozAlign: 'center', headerSort: false },
			{ title: 'country', field: 'country', hozAlign: 'center', headerSort: false },
			{ title: 'Begin_Date', field: 'begin_date', hozAlign: 'center', headerSort: false },
			{ title: 'End_Date', field: 'end_date', hozAlign: 'center', headerSort: false },
			{ title: 'Due_Date', field: 'due_date', hozAlign: 'center', headerSort: false },
			{ title: 'paytype', field: 'paytype', hozAlign: 'center', headerSort: false },
			{ title: 'dueday', field: 'dueday', hozAlign: 'center', headerSort: false },
			{ title: 'payer_no', field: 'taxpayno', hozAlign: 'center', headerSort: true },
			{ title: 'taxrate', field: 'nosvattype', hozAlign: 'center', headerSort: false },
			{ title: 'billtype', field: 'billtype', hozAlign: 'center', headerSort: true },
			{ title: 'invoiceform', field: 'invoiceform', hozAlign: 'center', headerSort: false },
			{ title: 'InvoiceAccount', field: 'invoiceaccount', hozAlign: 'center', headerSort: false },
			{ title: 'InvoiceSign', field: 'invoicesign', hozAlign: 'center', headerSort: false },
			{ title: 'tax_email_addr', field: 'tax_email', hozAlign: 'left', headerSort: true },
			{ title: 'custno', field: 'custno', hozAlign: 'center', headerSort: false },
			{ title: 'tr_fg', field: 'nos_tr_fg', hozAlign: 'center', headerSort: true },
			{ title: 'pay_deadline', field: 'sungbnm', hozAlign: 'center', headerSort: true },
			{ title: 'months_after', field: 'delaymm', hozAlign: 'center', headerSort: true },
			{ title: 'Demand_Div', field: 'demand_div', hozAlign: 'center', headerSort: true },
			{ title: 'JongSaupjang', field: 'jongcd', hozAlign: 'left', headerSort: true },
			{ title: 'Tax_Memo', field: 'tax_remark', hozAlign: 'center', headerSort: true },
			{ title: 'GIRO_email_ADDR', field: 'giro_email', hozAlign: 'left', headerSort: false },
			{ title: 'Charge_Count', field: 'frequency', hozAlign: 'center', headerSort: true },
			{ title: 'Contract_Date', field: 'first_startymd', hozAlign: 'center', headerSort: false },
			{ title: 'Admiss_Years', field: 'stip_year', hozAlign: 'center', headerSort: true },
			{ title: 'Admiss_Months', field: 'stip_months', hozAlign: 'center', headerSort: true },
			{ title: 'Contract_Date', field: 'cont_symd', hozAlign: 'center', headerSort: false },
			{ title: 'END_Date', field: 'cont_eymd', hozAlign: 'center', headerSort: false },
			{ title: 'Valid_ym', field: 'valid_ym', hozAlign: 'center', headerSort: false },
			{ title: 'Admiss_Date', field: 'admiss_date', hozAlign: 'center', headerSort: false },
			{ title: 'Is_Settled', field: 'issettled', hozAlign: 'center', headerSort: true },
			{ title: 'Multi_Payer_No', field: 'multi_payer_no', hozAlign: 'center', headerSort: true },
			{ title: 'Is_Multiple', field: 'is_multiple', hozAlign: 'center', headerSort: true },
			{ title: 'crp_code', field: 'noscd', hozAlign: 'center', headerSort: false },
			{ title: 'chief', field: 'bossnm', hozAlign: 'center', headerSort: false },
			{ title: 'uptae', field: 'custtype', hozAlign: 'left', headerSort: false },
			{ title: 'jongmok', field: 'custkind', hozAlign: 'left', headerSort: false },
			{ title: 'bankcard_name', field: 'banknm', hozAlign: 'left', headerSort: false },
			{ title: 'account_no', field: 'account_no', hozAlign: 'left', headerSort: false },
			{ title: 'owner_name', field: 'ownernm', hozAlign: 'center', headerSort: false },
			{ title: 'charge_name', field: 'charge_name', hozAlign: 'center', headerSort: false },

			{ title: 'charge_tel', field: 'charge_tel', hozAlign: 'center', headerSort: false },
			{ title: 'charge_phone', field: 'charge_phone', hozAlign: 'center', headerSort: false },
			{ title: 'sales_manager', field: 'userid', hozAlign: 'center', headerSort: false },
			{ title: 'oper_name', field: 'oper_name', hozAlign: 'center', headerSort: false },
			{ title: 'oper_tel', field: 'oper_tel', hozAlign: 'center', headerSort: false },
			{ title: 'oper_phone', field: 'oper_phone', hozAlign: 'center', headerSort: false },
			{ title: 'oper_email', field: 'oper_email', hozAlign: 'center', headerSort: false },
			{ title: 'charge_email', field: 'charge_email', hozAlign: 'center', headerSort: false },
			{ title: 'serial', field: 'nosseq', hozAlign: 'center', headerSort: false },
			{ title: '우편번호', field: 'postno', hozAlign: 'center', headerSort: false },
			{ title: '주소', field: 'address', hozAlign: 'left', headerSort: false },
			{ title: '상세주소', field: 'd_address', hozAlign: 'left', headerSort: false },
			{ title: '최종등록일시', field: 'addtime', hozAlign: 'center', headerSort: true },
		],
	})
})

function initialize() {
	resetForm(form_01)
	form_01.ym = currentMonth
	tableInstance1?.clearData()

	return vAlert('초기화 완료')
}

async function search() {
	const path = '/admin/stipulation'

	const ym = form_01.ym.replace(/-/g, '')

	const params = {
		yyyy: ym.substring(0, 4),
		mm: ym.substring(4, 6),
		bundleCode: form_01.bundleCode,
		clientCode: form_01.clientCode,
		erpName: form_01.erpName,
	}

	try {
		const res = await searchStart(path, params)
		await tableInstance1?.setData(res)
		return vAlert('조회완료')
	} catch (error) {
		console.log(error)
		return vAlertError(error)
	}
}
</script>

<style scoped></style>
