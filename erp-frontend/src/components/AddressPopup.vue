<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	<div
		v-if="visible"
		class="modal fade show d-block"
		tabindex="-1"
		style="background: rgba(0, 0, 0, 0.5)"
	>
		<div class="modal-dialog modal-dialog-centered">
			<div class="modal-content">
				<div class="modal-header">
					<h5 class="modal-title">{{ props.props.title }}</h5>
					<button type="button" class="btn-close" @click="close"></button>
				</div>
				<div class="modal-body">
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
										<i class="bi bi-search"></i>
									</button>
								</td>
							</tr>
						</tbody>
					</table>
					<div ref="popupRef" />
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, reactive, computed } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import { popupSearch } from '@/composables/useSearch'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { useCustStore } from '@/stores/custStore'

const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const custStore = useCustStore() // 회사코드 가져옴
const popupRef = ref<HTMLDivElement | null>(null)
const path = computed(() => props.props.path) // 경로 지정
const popupGrid = ref<Tabulator | null>(null)

export interface Form {
	inputValue: string
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
		searchField: string
		path: string
	}
}>()

const emit = defineEmits(['update:visible', 'confirm'])

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

			popupGrid.value = new Tabulator(popupRef.value, {
				layout: 'fitColumns',
				height: '400',
				data: [],
				columns: props.props.columns,
			})

			await search()

			popupGrid.value.on('rowClick', (e, row) => {
				const rowData = row.getData()
				console.log('👉 선택된 행:', rowData)
				emit('confirm', rowData) // ⭐ 클릭한 값 부모로 보냄
				emit('update:visible', false) // ⭐ 모달 닫음
			})
		}
	}
)

const { searchStart } = popupSearch(path, vAlert, vAlertError)

async function search() {
	try {
		const res = await searchStart({
			[props.props.searchField]: custStore.custcd,
			sch_custaddress: form.inputValue,
		})
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
</style>
