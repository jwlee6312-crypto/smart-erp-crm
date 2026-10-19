<!--
	=============================================================
	프로그램명	  : Asterisk 다이얼플랜(Extensions) 관리
  프로그램 ID	: CRM_EXT_010
	작성일자	    : 25.02.24
	작성자	      : AI Assistant
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	
	<div class="mb-2 btn-line">
		<button @click="initialize">초기화</button>
		<button @click="search">조회</button>
		<button @click="addRow">행추가</button>
		<button @click="save">저장</button>
		<button @click="deleteSelected">선택삭제</button>
	</div>

	<table class="table shadow-sm" style="table-layout: fixed">
		<colgroup>
			<col style="width: 10%" />
			<col />
			<col style="width: 10%" />
			<col />
			<col style="width: 10%" />
			<col />
		</colgroup>
		<tbody>
			<tr>
				<th>컨텍스트</th>
				<td>
					<input v-model="searchForm.context" class="form-control-sm form-control w-100" placeholder="예: from-internal" />
				</td>
				<th>내선번호(Exten)</th>
				<td>
					<input v-model="searchForm.exten" class="form-control-sm form-control w-100" />
				</td>
				<th>명령어(App)</th>
				<td>
					<input v-model="searchForm.app" class="form-control-sm form-control w-100" />
				</td>
			</tr>
		</tbody>
	</table>

	<div ref="tableRef" class="tabulator-ref" />
</template>

<script setup lang="ts">
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { onMounted, reactive, ref } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import { useSave } from '@/composables/useSave'
import { useSearch } from '@/composables/useSearch'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()
const { saveBody, deleteBody } = useSave()
const { searchStart } = useSearch()

const searchForm = reactive({
	context: '',
	exten: '',
	app: ''
})

const tableRef = ref<HTMLDivElement | null>(null)
let tableInstance: Tabulator | null = null

onMounted(() => {
	if (!tableRef.value) return
	tableInstance = new Tabulator(tableRef.value, {
		placeholder: 'No data',
		layout: 'fitColumns',
		selectable: true,
		data: [],
		columns: [
			{ formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center", headerSort: false, width: 40 },
			{ title: 'Context', field: 'context', editor: 'input', hozAlign: 'center' },
			{ title: 'Extension', field: 'exten', editor: 'input', hozAlign: 'center' },
			{ title: 'Priority', field: 'priority', editor: 'number', hozAlign: 'center', width: 80 },
			{ title: 'Label', field: 'label', editor: 'input', hozAlign: 'center' },
			{ title: 'Application', field: 'app', editor: 'input', hozAlign: 'center' },
			{ title: 'AppData', field: 'appdata', editor: 'input', hozAlign: 'left' },
		],
	})
})

function initialize() {
	searchForm.context = ''
	searchForm.exten = ''
	searchForm.app = ''
	tableInstance?.clearData()
	vAlert('초기화 완료')
}

async function search() {
	const path = '/crm/asterisk/extension/search'
	try {
		const { data, message } = await searchStart(path, searchForm)
		await tableInstance?.setData(data)
		vAlert(message)
	} catch (error) {
		const message = error?.response?.data?.message || '조회에 실패했습니다.'
		vAlertError(message)
	}
}

function addRow() {
	tableInstance?.addRow({ 
		context: 'from-internal', 
		priority: 1,
		app: '',
		appdata: ''
	})
}

async function save() {
	const path = '/crm/asterisk/extension/save'
	const data = tableInstance?.getData()
	
	try {
		const { message } = await saveBody(path, data)
		vAlert(message)
		await search() // 저장 후 재조회
	} catch (error) {
		const message = error?.response?.data?.message || '저장에 실패했습니다.'
		vAlertError(message)
	}
}

async function deleteSelected() {
	const selectedData = tableInstance?.getSelectedData()
	if (selectedData.length === 0) {
		vAlertError('삭제할 행을 선택해주세요.')
		return
	}

	if (!confirm('선택한 항목을 삭제하시겠습니까?')) return

	const path = '/crm/asterisk/extension/delete'
	try {
		const { message } = await deleteBody(path, selectedData)
		vAlert(message)
		await search()
	} catch (error) {
		const message = error?.response?.data?.message || '삭제에 실패했습니다.'
		vAlertError(message)
	}
}
</script>

<style scoped>
.btn-line {
	display: flex;
	gap: 5px;
}
.tabulator-ref {
	margin-top: 10px;
	height: 500px;
}
</style>
