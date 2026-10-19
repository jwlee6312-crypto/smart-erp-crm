<!--
	=============================================================
	프로그램명	: 코드정보 등록 (HSBA900U)
	작성일자	: 2025.02.24
	설명        : 코드 그룹 및 상세 코드 관리 (최소 수정을 통한 스크롤 최적화)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-code-square me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
        코드정보 등록 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">코드정보 등록 (HSBA900U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-2">
        <button class="btn-erp btn-init" @click="initializeForm">초기화</button>
        <button class="btn-erp btn-search" @click="fetchGroups">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- 🔍 [상단] 조회 필터 영역 (erp-table-dense 적용) -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
                <col style="width: 10%" /><col style="width: 40%" />
                <col style="width: 10%" /><col style="width: 40%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light">그룹명/코드</th>
                <td>
                  <input v-model="searchParam.SCH_TEXT" class="form-control form-control-sm w-75" placeholder="검색어 입력" @keyup.enter="fetchGroups" />
                </td>
                <th class="text-center bg-light">사용여부</th>
                <td>
                  <select v-model="searchParam.SCH_useyn" class="form-select form-select-sm w-50" @change="fetchGroups">
                    <option value="">전체</option>
                    <option value="Y">사용</option>
                    <option value="N">미사용</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅰️ 코드 상세 입력 폼 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center" style="background-color: #f0f7ff !important;">
          <span class="fw-bold small text-primary"><i class="bi bi-pencil-square me-1"></i> 코드 상세 정보</span>
        </div>
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required bg-light">코드구분</th>
                <td>
                  <select v-model="masterData.cdgbn" class="form-select" :disabled="masterData.actkind === 'U0'">
                    <option v-for="opt in groupOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="required bg-light">코&nbsp;&nbsp;&nbsp;&nbsp;드</th>
                <td><input v-model="masterData.code" class="form-control text-center fw-bold" maxlength="3" :readonly="masterData.actkind === 'U0'" /></td>
                <th class="required bg-light">코&nbsp;드&nbsp;명</th>
                <td><input v-model="masterData.cdnm" class="form-control" maxlength="30" /></td>
                <th class="bg-light text-center">순서</th>
                <td><input v-model="masterData.dspord" class="form-control text-center" maxlength="3" /></td>
                <th class="bg-light text-center">사용여부</th>
                <td>
                  <div class="form-check form-switch m-0 d-flex align-items-center justify-content-center">
                    <input v-model="masterData.useyn" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="useYn900">
                    <label class="form-check-label ms-2 small" for="useYn900">{{ masterData.useyn === 'Y' ? '사용' : '중지' }}</label>
                  </div>
                </td>
              </tr>
              <tr>
                <th class="bg-light">비&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                <td colspan="9"><input v-model="masterData.remark" class="form-control" maxlength="50" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 🅱️ 하단 그리드 영역 (💡 사용자 원칙 적용: tabulator-instance 클래스 명시) -->
      <div class="d-flex flex-grow-1 gap-2 overflow-hidden" style="min-height: 0;">
        <!-- 좌측: 코드 그룹 목록 -->
        <div class="card border shadow-sm flex-column-fill overflow-hidden" style="width: 300px; min-width: 300px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark flex-shrink-0">
            <i class="bi bi-folder-fill me-1 text-primary"></i> 코드 그룹
          </div>
          <div class="card-body p-0 flex-grow-1 overflow-hidden d-flex flex-column">
            <div ref="groupGridElement" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <!-- 우측: 상세 코드 목록 -->
        <div class="card border shadow-sm flex-grow-1 flex-column-fill overflow-hidden">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark d-flex justify-content-between flex-shrink-0">
            <span><i class="bi bi-list-check me-1 text-primary"></i> 상세 코드 목록</span>
            <span v-if="selectedGroupName" class="text-primary fw-bold">[{{ selectedGroupName }}]</span>
          </div>
          <div class="card-body p-0 flex-grow-1 overflow-hidden d-flex flex-column">
            <div ref="codeGridElement" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick, watch } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'
import Modal from '@/components/Modal.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps } = useCommonHelp()

// [1] 데이터 모델링
const searchParam = reactive({ SCH_TEXT: '', SCH_useyn: '' })
const masterData = reactive<any>({
  actkind: 'A0', cmpycd: authStore.cmpycd, cdgbn: '010',
  code: '', cdnm: '', remark: '', dspord: '', useyn: 'Y'
})

const groupOptions = ref<any[]>([])
const selectedGroupName = ref('')
const activeItemCount = ref(0)

const groupGridElement = ref<HTMLElement | null>(null)
const codeGridElement = ref<HTMLElement | null>(null)
let groupGrid: Tabulator | null = null
let codeGrid: Tabulator | null = null

// [2] 그리드 초기화
const initGrids = () => {
  if (groupGridElement.value) {
    groupGrid = new Tabulator(groupGridElement.value, {
      layout: "fitColumns", height: "100%", placeholder: "그룹 데이터 없음",
      columns: [
        { title: "코드 그룹", field: "cdnm", headerSort: false, formatter: (cell) => `<span class="cursor-pointer text-primary fw-bold">${cell.getValue()}</span>` }
      ]
    })

    groupGrid.on("rowClick", (e, row) => {
      const data = row.getData()
      masterData.cdgbn = data.code; selectedGroupName.value = data.cdnm
      masterData.actkind = 'A0'; masterData.code = ''; masterData.cdnm = ''; masterData.remark = ''; masterData.dspord = ''; masterData.useyn = 'Y'
      fetchCodes(data.code)
    })
  }

  if (codeGridElement.value) {
    codeGrid = new Tabulator(codeGridElement.value, {
      layout: "fitColumns", height: "100%", placeholder: "그룹을 선택하세요",
      columnDefaults: { headerSort: false, headerHozAlign: "center", vertAlign: "middle" },
      columns: [
        { title: "코드", field: "code", width: 80, hozAlign: "center", cssClass: "fw-bold text-primary" },
        { title: "코드 명", field: "cdnm", minWidth: 200, widthGrow: 1 },
        { title: "비고", field: "remark", width: 200 },
        { title: "순서", field: "dspord", width: 60, hozAlign: "center" },
        {
          title: "사용", field: "useyn", width: 70, hozAlign: "center",
          formatter: (c) => c.getValue() === 'Y' ? '<span class="text-success">O</span>' : '<span class="text-danger">X</span>'
        }
      ]
    })

    codeGrid.on("rowClick", (e, row) => {
      const data = row.getData()
      Object.assign(masterData, data)
      masterData.cdgbn = data.cdgbn || masterData.cdgbn
      masterData.actkind = 'U0'
    })
  }
}

// [3] 기능 구현
async function fetchGroupOptions() {
  try {
    const res = await api.get('/hs00/HS00_000S_STR', { params: { gubun: 'E0', cmpycd: authStore.cmpycd, gbncd: '010' } })
    groupOptions.value = res.data.map((i: any) => ({
      codecd: String(i.code || i.codecd || '').trim(), codenm: String(i.cdnm || i.codenm || '').trim()
    }))
  } catch (e) { console.error('코드구분 로드 실패') }
}

async function fetchGroups() {
  try {
    const res = await api.post('/hsba/HSBA_900U_STR', {
      actkind: 'S1', cmpycd: authStore.cmpycd, cdgbn: '010', SCH_TEXT: searchParam.SCH_TEXT, useyn: searchParam.SCH_useyn
    })
    groupGrid?.setData(res.data)
  } catch (e) { vAlertError('그룹 목록 조회 실패') }
}

async function fetchCodes(cdgbn: string) {
  try {
    const res = await api.post('/hsba/HSBA_900U_STR', { actkind: 'S0', cmpycd: authStore.cmpycd, cdgbn: cdgbn })
    codeGrid?.setData(res.data)
    activeItemCount.value = res.data.length
  } catch (e) { vAlertError('상세 코드 조회 실패') }
}

async function save() {
  if (!masterData.code || !masterData.cdnm) return vAlertError('코드와 코드명은 필수입니다.')
  if (!confirm('저장하시겠습니까?')) return
  try {
    await api.post('/hsba/HSBA_900U_STR', { ...masterData, userid: authStore.userid })
    vAlert('정상적으로 처리되었습니다.')
    fetchCodes(masterData.cdgbn)
    if (masterData.actkind === 'A0') {
      const currentGbn = masterData.cdgbn; initializeForm(); masterData.cdgbn = currentGbn
    } else {
        masterData.actkind = 'A0'; masterData.code = ''; masterData.cdnm = ''; masterData.remark = ''; masterData.dspord = ''
    }
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initializeForm() {
  const currentGbn = masterData.cdgbn
  resetForm(masterData)
  Object.assign(masterData, { actkind: 'A0', cmpycd: authStore.cmpycd, cdgbn: currentGbn, useyn: 'Y' })
}

onMounted(async () => {
  await fetchGroupOptions()
  nextTick(() => { initGrids(); fetchGroups() })
})

watch(() => masterData.cdgbn, (newVal) => {
    if (masterData.actkind === 'A0') {
        const group = groupOptions.value.find(o => o.codecd === newVal)
        selectedGroupName.value = group ? group.codenm : ''; fetchCodes(newVal)
    }
})
</script>

<style scoped>
.tabulator-instance {
  background-color: #fff;
}
</style>