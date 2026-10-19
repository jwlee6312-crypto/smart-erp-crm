<!--
	=============================================================
	프로그램명	  : -
  프로그램 ID	: -
	작성일자	    : 25.05.12
	작성자	      : 이현준
	수정일자     : 25.07.31
	수정자	      : 이현준
	수정 내용    : 팝업창 결과 미조회 시, 직접 데이터 입력하는 기능 추가
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	<div v-if="visible" class="modal fade show d-block" style="background: rgba(0, 0, 0, 0.5)">
		<div class="modal-dialog modal-dialog-centered">
			<div class="modal-content">
				<div class="modal-header">
					<h5 class="modal-title">{{ props.props.title }}</h5>
					<button type="button" class="btn-close" @click="close"></button>
				</div>
				<div class="modal-body">
					<div v-if="props.props.inputVisible">
						<table class="table" style="table-layout: fixed">
							<colgroup>
								<col style="width: 30%" />
								<col />
							</colgroup>
							<tbody>
								<tr>
									<th class="shadow-sm">{{ props.props.tableName }}</th>
									<td class="shadow-sm d-flex gap-2">
										<input
											v-model="form.inputValue"
											type="text"
											class="form-control form-control-sm w-100"
										/>
										<button type="button" class="btn btn-sm btn-dark" @click="search">
											<i class="bi bi-search" />
										</button>
									</td>
								</tr>
							</tbody>
						</table>
					</div>
					<div ref="popupRef" />
					<div>
						<p class="noResult" @click="toggleNoResult">찾으시는 결과가 없으신가요?</p>
						<div v-if="hasNoResult" class="d-flex gap-2 align-items-center">
							<input v-model="directInput" type="text" class="form-control form-control-sm" />
							<button
								class="btn btn-dark btn-sm"
								style="white-space: nowrap"
								@click="submitDirectInput"
							>
								등록
							</button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, reactive } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import { useSearch } from '@/composables/useSearch'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'

const { showAlert, showError, alertMessage } = useAlerts()
const { searchStart } = useSearch()

// 팝업, 팝업 그리드
const popupRef = ref<HTMLDivElement | null>(null)
// let popupGrid: Tabulator | null = null
const popupGrid = ref<Tabulator | null>(null)

export interface Form {
	inputValue: string
}

const hasNoResult = ref<boolean>(false)
const toggleNoResult = () => {
	hasNoResult.value = !hasNoResult.value
}

const directInput = ref<string>('')
const submitDirectInput = () => {
	console.log('👉 선택된 행:', directInput.value)
	emit('confirm', directInput.value) // ⭐ 클릭한 값 부모로 보냄
	directInput.value = ''
	emit('update:visible', false) // ⭐ 모달 닫음
}

const form = reactive<Form>({
	inputValue: '',
})

const props = defineProps<{
	visible: boolean
	props: {
		title: string
		tableName: string
		columns: any[]
		path: string
		searchKeys: string[]
		searchValues: any[]
		inputVisible?: boolean
	}
}>()

const emit = defineEmits(['update:visible', 'confirm'])

// 닫기
function close() {
	emit('update:visible', false)
}

// ✅ 팝업 그리드 생성
watch(
	() => props.visible,
	async (isVisible) => {
		if (isVisible) {
			await nextTick() // DOM 그려진 다음에
			if (!popupRef.value) return

			// 만약 popupGrid가 이미 만들어져있으면 제거
			if (popupGrid.value) {
				popupGrid.value.destroy()
			}
			hasNoResult.value = false
			form.inputValue = ''

			// 팝업창 안의 그리드
			popupGrid.value = new Tabulator(popupRef.value, {
				layout: 'fitColumns',
				height: '400',
				data: [],
				columns: props.props.columns, // 부모에게 props로 받은 컬럼
			})

			await search() // 초기 데이터 보여주기
			popupGrid.value.on('rowClick', (e, row) => {
				const rowData = row.getData()
				console.log('👉 선택된 행:', rowData)
				emit('confirm', rowData) // ⭐ 클릭한 값 부모로 보냄
				emit('update:visible', false) // ⭐ 모달 닫음
			})
		}
	}
)

// 조회로직
async function search() {
	try {
		const searchKeyValue: { [key: string]: any } = {}

		props.props.searchKeys.forEach((key, index) => {
			const value = props.props.searchValues[index]
			searchKeyValue[key] = value?.trim() === '' ? form.inputValue : value
		})
		const res = await searchStart(props.props.path, searchKeyValue)
		if (popupGrid.value) {
			await popupGrid.value.setData(res)
		}
	} catch (error) {
		console.error(error)
	}
}
</script>

<style scoped>
.modal {
	display: block;
}

.noResult:hover {
	color: var(--bs-primary);
	cursor: pointer;
}
</style>
