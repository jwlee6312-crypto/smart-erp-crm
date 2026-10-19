<!--기본정보/조직도관리 [좌우 흐름 시각화 표준화 버전] -->
<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom shadow-sm bg-white py-2 sticky-top flex-shrink-0">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-diagram-3-fill me-2 text-primary" style="font-size: 18px;"></i>
				시스템관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				기초관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">조직도 관리 (HABA060U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1 pe-3">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="search">조회</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
			</div>
		</div>

		<!-- 💡 메인 컨텐츠 영역 (좌/우 분할 레이아웃) -->
		<div class="flex-grow-1 overflow-hidden d-flex gap-2 p-2 bg-light">

			<!-- ⬅️ 좌측: 입력 및 리스트 (60%) -->
			<div class="d-flex flex-column gap-2 overflow-hidden" style="flex: 1.1; min-width: 0;">
				<!-- [좌-상] 검색 바 -->
				<div class="card border-0 shadow-sm flex-shrink-0">
					<div class="card-body p-2 bg-white rounded">
						<div class="input-group input-group-sm flex-nowrap">
							<span class="input-group-text bg-light fw-bold px-3 border-0">부서명 검색</span>
							<input v-model="searchForm.deptnm_s" class="form-control border-light-subtle" placeholder="부서명을 입력하세요..." @keyup.enter="search" />
							<button class="btn btn-dark px-3" @click="search"><i class="bi bi-search"></i></button>
						</div>
					</div>
				</div>

				<!-- [좌-중] 상세 입수정 영역 -->
				<div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
					<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
						<div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-secondary"></i>부서 상세 정보 관리</div>
						<div v-if="formData.actkind === 'U0'" class="badge bg-warning text-dark px-2">수정 중</div>
						<div v-else class="badge bg-primary text-white px-2">신규 등록</div>
					</div>
					<div class="card-body p-0 bg-white">
						<table class="erp-table-full border-0">
							<colgroup>
								<col style="width: 100px;" /><col />
								<col style="width: 100px;" /><col />
							</colgroup>
							<tbody>
								<tr>
									<th class="required">부서코드</th>
									<td><input v-model="formData.deptcd" type="text" class="form-control fw-bold text-primary text-center" maxlength="5" :disabled="formData.actkind === 'U0'"/></td>
									<th class="required">부서명칭</th>
									<td><input v-model="formData.deptnm" type="text" class="form-control" /></td>
								</tr>
								<tr>
									<th>상위부서</th>
									<td>
										<div class="input-group input-group-sm flex-nowrap">
											<input v-model="formData.updept" type="text" class="form-control bg-light text-center fw-bold" style="max-width: 60px;" readonly />
											<input v-model="formData.updeptnm" type="text" class="form-control border-start-0" placeholder="검색" readonly />
											<button class="btn btn-outline-secondary px-2" @click="openDeptHelp"><i class="bi bi-search"></i></button>
										</div>
									</td>
									<th>사용여부</th>
									<td class="ps-4">
										<div class="form-check form-switch m-0 d-inline-block">
											<input v-model="formData.useyn" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="useyn060">
											<label class="form-check-label ms-2 small fw-bold" for="useyn060">{{ formData.useyn === 'Y' ? '사용 중' : '중지' }}</label>
										</div>
									</td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>

				<!-- [좌-하] 그리드 영역 -->
				<div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
					<div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
						<div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
					</div>
				</div>
			</div>

			<!-- ➡️ 우측: 좌우 흐름 조직도 (아름다운 계층 구조 - 이미지 참고 튜닝) (40%) -->
			<div class="card border shadow-sm d-flex flex-column overflow-hidden bg-white" style="flex: 1; min-width: 0;">
				<div class="card-header py-2 px-3 border-bottom bg-white d-flex align-items-center justify-content-between">
					<span class="fw-bold small text-primary"><i class="bi bi-share-fill me-2"></i>조직 흐름도 (Left-to-Right)</span>
					<div class="small text-muted italic" style="font-size: 10px;">가로/세로 스크롤 가능</div>
				</div>
				<div class="card-body p-0 overflow-auto bg-white custom-scrollbar">
					<!-- 🚀 가로형 조직도 렌더링 컨테이너 -->
					<div class="org-horizontal-container p-2 pt-0">
						<HorizontalTreeNode v-for="node in orgTree" :key="node.deptcd" :node="node" @select="onNodeSelect" />
					</div>
					<div v-if="orgTree.length === 0" class="h-100 d-flex align-items-center justify-content-center text-muted">
						데이터가 없습니다.
					</div>
				</div>
			</div>

		</div>
	</div>

	<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, nextTick, computed, h, defineComponent } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useAlerts } from '@/composables/useAlerts'
import { useFormReset } from '@/composables/useFormReset'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'

/** 🚀 [재귀 컴포넌트] 좌우 흐름 조직도 노드 */
const HorizontalTreeNode = defineComponent({
	props: ['node'],
	emits: ['select'],
	setup(props, { emit }) {
		return () => {
			const hasChildren = props.node.children && props.node.children.length > 0;
			return h('div', { class: ['org-h-wrapper', { 'is-leaf': !hasChildren }] }, [
				// 현재 부서 박스
				h('div', {
					class: ['org-h-box', { 'is-active': props.node.deptcd === formData.deptcd }],
					onClick: () => emit('select', props.node)
				}, [
					h('div', { class: 'dept-name' }, props.node.deptnm),
					h('div', { class: 'dept-code' }, props.node.deptcd)
				]),
				// 자식 부서 리스트
				hasChildren ? h('div', { class: 'org-h-children' },
					props.node.children.map((child: any) => h(HorizontalTreeNode, {
						node: child,
						onSelect: (n: any) => emit('select', n)
					}))
				) : null
			]);
		};
	}
});

const authstore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const searchForm = reactive({ deptnm_s: '' })
const formData = reactive({
	actkind: 'S0', cmpycd: authstore.cmpycd, deptcd: '', deptnm: '', updept: '', updeptnm: '', useyn: 'Y', deptopn: new Date().toISOString().substring(0, 10)
})

const allDepts = ref<any[]>([])
const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null
const modalVisible = ref(false)
const modalProps = reactive<any>({ title: '', path: '', data: {}, columns: [], onConfirm: () => {} })

const orgTree = computed(() => {
	const map: any = {}; const tree: any[] = [];
	allDepts.value.forEach(node => { map[node.deptcd] = { ...node, children: [] }; });
	allDepts.value.forEach(node => {
		const parent = map[node.updept];
		if (parent) parent.children.push(map[node.deptcd]);
		else if (node.updept === '00000' || !node.updept || !map[node.updept]) tree.push(map[node.deptcd]);
	});
	return tree;
});

async function search() {
	try {
		const res = await api.post('/haba/HABA_060U_STR', { actkind: 'S0', cmpycd: authstore.cmpycd, deptnm: searchForm.deptnm_s })
		const processed = res.data || [];
		allDepts.value = processed;
		mainGrid?.setData(processed)
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

async function save() {
	if (!formData.deptcd || !formData.deptnm) return vAlertError('필수값 확인')
	try {
		const act = formData.actkind === 'S0' ? 'A0' : 'U0';
		await api.post('/haba/HABA_060U_STR', { ...formData, actkind: act, updemp: authstore.userid })
		vAlert('저장되었습니다.'); search()
	} catch (e) { vAlertError('저장 실패') }
}

function onNodeSelect(node: any) {
	Object.assign(formData, node);
	formData.actkind = 'U0';
	if (mainGrid) {
		const targetRow = mainGrid.getRows().find(r => r.getData().deptcd === node.deptcd);
		if (targetRow) { mainGrid.deselectRow(); targetRow.select(); targetRow.scrollTo(); }
	}
}

function initialize() {
	resetForm(formData); Object.assign(formData, { actkind: 'S0', useyn: 'Y', cmpycd: authstore.cmpycd, deptopn: new Date().toISOString().substring(0, 10) });
	mainGrid?.deselectRow();
}

function openDeptHelp() {
	Object.assign(modalProps, {
		title: '부서 검색', path: '/ha00/HA00_00P_STR', data: { gubun: 'D0', cmpycd: authstore.cmpycd, gbncd: '', code: '', remark: '' },
		columns: [{ title: '부서코드', field: 'deptcd', width: 100 }, { title: '부서명', field: 'deptnm', width: 200 }],
		onConfirm: (d: any) => { formData.updept = d.deptcd; formData.updeptnm = d.deptnm; }
	});
	modalVisible.value = true;
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			columnDefaults: { headerSort: false, headerHozAlign: 'center', hozAlign: 'center', vertAlign: 'middle' },
			columns: [
				{ title: "No", formatter: "rownum", width: 40 },
				{ title: '부서코드', field: 'deptcd', width: 90, cssClass: 'fw-bold text-primary border-end' },
				{ title: '부서명칭', field: 'deptnm', minWidth: 150, widthGrow: 1, cssClass: 'fw-bold', hozAlign: 'left' }
			]
		})
		mainGrid.on('rowClick', (e, row) => { Object.assign(formData, row.getData()); formData.actkind = 'U0' })
	}
	search()
})
</script>

<style>
/* 🚀 조직도 전용 스타일 (Scoped 제거하여 재귀 컴포넌트 완벽 적용) */
.org-horizontal-container {
	display: flex;
	flex-direction: column;
	align-items: flex-start;
}

.org-h-wrapper {
	display: flex;
	align-items: center;
	position: relative;
	padding: 8px 0; /* 상하 폭 최적화 */
}

/* 부서 박스 디자인 */
.org-h-box {
	width: 90px;
	padding: 8px 4px;
	background: #ffffff;
	border: 1px solid #d1d5db;
	border-radius: 4px;
	box-shadow: 0 1px 3px rgba(0,0,0,0.1);
	cursor: pointer;
	z-index: 10;
	transition: all 0.2s;
	text-align: center;
	position: relative;
}

.org-h-box:hover {
	border-color: #3b82f6;
	background: #f0f9ff;
	box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.org-h-box.is-active {
	background: #3b82f6;
	color: #fff;
	border-color: #2563eb;
	font-weight: bold;
}

.org-h-box .dept-name { font-size: 11px; margin-bottom: 2px; }
.org-h-box .dept-code { font-size: 9px; opacity: 0.7; }

/* 🚀 연결선 로직 (이미지 참고 정밀 구현) */
.org-h-children {
	display: flex;
	flex-direction: column;
	padding-left: 40px;
	position: relative;
}

.org-h-wrapper:not(.is-leaf) > .org-h-box::after {
	content: "";
	position: absolute;
	right: -20px;
	top: 50%;
	width: 20px;
	height: 1.5px;
	background: #cbd5e1;
}

.org-h-children > .org-h-wrapper::before {
	content: "";
	position: absolute;
	left: -20px;
	top: 50%;
	width: 20px;
	height: 1.5px;
	background: #cbd5e1;
}

.org-h-children::before {
	content: "";
	position: absolute;
	left: 20px;
	top: 50%;
	bottom: 50%;
	width: 1.5px;
	background: #cbd5e1;
}

.org-h-children > .org-h-wrapper::after {
	content: "";
	position: absolute;
	left: -20px;
	top: 0;
	bottom: 0;
	width: 1.5px;
	background: #cbd5e1;
	z-index: 1;
}

.org-h-children > .org-h-wrapper:first-child::after { top: 50%; }
.org-h-children > .org-h-wrapper:last-child::after { bottom: 50%; }

.org-horizontal-container > .org-h-wrapper::before { display: none; }

.custom-scrollbar::-webkit-scrollbar { width: 6px; height: 6px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
</style>
