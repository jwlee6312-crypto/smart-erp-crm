<!--
	=============================================================
	프로그램명	: 품목등록 (HSBA010U)
	작성일자	: 2025.02.27
	설명        : 영업/재고 품목 마스터 관리 (Tab 키순 이동 및 Alt 키보드 단축키 지원)
	=============================================================
-->

<template>
  <AppAlert :show="showalert" :error="showerror" :message="alertmessage" />
  <Modal v-model:visible="modalvisible" :modalProps="modalprops" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-tags-fill me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        품목관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">품목등록 (HSBA010U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize" title="Alt+N: 신규초기화">신규(N)</button>
        <button class="btn-erp btn-search" @click="search" title="Alt+F: 조회">조회(F)</button>
        <button class="btn-erp btn-save" @click="save" title="Alt+S: 저장">저장(S)</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-1 d-flex flex-column gap-1 bg-light main-content-wrapper">

      <!-- [상단] 조회 필터 영역 (간결화) -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
                <col style="width: 100px" /><col style="width: 200px" />
                <col style="width: 100px" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light">재고자산</th>
                <td>
                  <select v-model="searchdata.sch_astkind" class="form-select form-select-sm" tabindex="1" @change="handleassetchange">
                    <option v-for="opt in assetoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="text-center bg-light border-start">품목검색</th>
                <td class="px-2">
                  <input v-model="searchdata.sch_itemnm" type="text" class="form-control form-control-sm" tabindex="2" style="width: 400px;" placeholder="품목명 또는 코드를 입력하고 엔터를 누르세요" @keyup.enter="search" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [중간] 품목 상세 정보 (5열 배치 및 탭키 순차 순항) -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 90px;" /><col />
              <col style="width: 90px;" /><col />
              <col style="width: 90px;" /><col />
              <col style="width: 90px;" /><col />
              <col style="width: 90px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required bg-light">재고자산</th>
                <td>
                  <select ref="firstFocusRef" v-model="masterdata.astkind" class="form-select form-select-sm" tabindex="3">
                    <option v-for="opt in assetoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="required bg-light border-start">품목코드</th>
                <td><input v-model="masterdata.itemcd" type="text" class="form-control form-control-sm text-center fw-bold text-primary" tabindex="4" maxlength="7" :readonly="masterdata.actkind === 'U0'" /></td>
                <th class="required bg-light border-start">품목명</th>
                <td><input v-model="masterdata.itemnm" type="text" class="form-control form-control-sm fw-bold" tabindex="5" /></td>
                <th class="required bg-light border-start">규격</th>
                <td><input v-model="masterdata.itsize" type="text" class="form-control form-control-sm" tabindex="6" /></td>
                <th class="required bg-light border-start">단위</th>
                <td>
                  <select v-model="masterdata.unit" class="form-select form-select-sm" tabindex="7">
                    <option v-for="opt in unitoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th class="bg-light">대분류</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterdata.agrpnm" class="form-control bg-light" tabindex="8" readonly />
                    <button class="btn btn-outline-secondary px-1" tabindex="9" @click="openhelp('agrp')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="bg-light border-start">중분류</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterdata.bgrpnm" class="form-control bg-light" tabindex="10" readonly />
                    <button class="btn btn-outline-secondary px-1" tabindex="11" @click="openhelp('bgrp')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="bg-light border-start">영문명</th>
                <td><input v-model="masterdata.itemenm" type="text" class="form-control form-control-sm" tabindex="12" /></td>
                <th class="bg-light border-start">적정재고</th>
                <td><input v-model="masterdata.stock" type="number" class="form-control form-control-sm text-end" tabindex="13" /></td>
                <th class="bg-light border-start">바코드</th>
                <td><input v-model="masterdata.barcode" type="text" class="form-control form-control-sm" tabindex="14" /></td>
              </tr>
              <tr>
                <th class="bg-light">HS Code</th>
                <td><input v-model="masterdata.hscode" type="text" class="form-control form-control-sm" tabindex="15" /></td>
                <th class="bg-light border-start">제조사</th>
                <td><input v-model="masterdata.maker" type="text" class="form-control form-control-sm" tabindex="16" /></td>
                <th class="required bg-light border-start">매입처</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterdata.in_custcd" type="text" class="form-control text-center fw-bold bg-light" style="max-width: 70px;" tabindex="17" readonly />
                    <input v-model="masterdata.in_custnm" class="form-control bg-light" tabindex="18" readonly />
                    <button class="btn btn-outline-secondary px-1" tabindex="19" @click="openhelp('cust')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="bg-light border-start">바코드사용</th>
                <td>
                  <div class="d-flex gap-2 align-items-center h-100 justify-content-center">
                    <div class="form-check form-switch m-0 small"><input v-model="masterdata.autoyn" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="swauto" tabindex="20"> <label class="fw-bold" for="swauto">사용</label></div>
                  </div>
                </td>
                <th class="bg-light border-start">사용/세트</th>
                <td>
                  <div class="d-flex gap-2 align-items-center h-100 justify-content-center">
                    <div class="form-check form-switch m-0 small"><input v-model="masterdata.useyn" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="swuse" tabindex="21"> <label class="fw-bold" for="swuse">사용</label></div>
                    <div class="form-check form-switch m-0 small"><input v-model="masterdata.setyn" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="swset" tabindex="22"> <label class="fw-bold" for="swset">세트</label></div>
                  </div>
                </td>
              </tr>
              <tr>
                <th class="bg-light text-center">매입단위/환산</th>
                <td>
                  <div class="d-flex gap-1">
                    <select v-model="masterdata.inunit" class="form-select form-select-sm" style="width: 70px;" tabindex="23">
                      <option v-for="opt in unitoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                    </select>
                    <input v-model="masterdata.inqty" type="number" class="form-control form-control-sm text-end" tabindex="24" />
                  </div>
                </td>
                <th class="bg-light border-start text-center">매출단위/환산</th>
                <td>
                  <div class="d-flex gap-1">
                    <select v-model="masterdata.outunit" class="form-select form-select-sm" style="width: 70px;" tabindex="25">
                      <option v-for="opt in unitoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                    </select>
                    <input v-model="masterdata.outqty" type="number" class="form-control form-control-sm text-end" tabindex="26" />
                  </div>
                </td>
                <th class="bg-light border-start text-center">매입단가</th>
                <td><input v-model="masterdata.imprice" type="number" class="form-control form-control-sm text-end" tabindex="27" /></td>
                <th class="bg-light border-start text-center">매출단가</th>
                <td><input v-model="masterdata.omprice" type="number" class="form-control form-control-sm text-end" tabindex="28" /></td>
                <th class="bg-light border-start text-center">부피(CBM)</th>
                <td><input v-model="masterdata.cbm" type="number" class="form-control form-control-sm text-end" tabindex="29" /></td>
              </tr>
              <tr>
                <th class="bg-light text-center">총중량(G)</th>
                <td><input v-model="masterdata.g_weight" type="number" class="form-control form-control-sm text-end" tabindex="30" /></td>
                <th class="bg-light border-start text-center">순중량(N)</th>
                <td><input v-model="masterdata.net_weight" type="number" class="form-control form-control-sm text-end" tabindex="31" /></td>
                <th class="bg-light border-start text-center">비&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                <td colspan="5"><input v-model="masterdata.remark" type="text" class="form-control form-control-sm" tabindex="32" placeholder="기타 특기사항을 입력하세요" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 품목 리스트 그리드 (최대 확장) -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark"><i class="bi bi-grid-3x3-gap-fill me-1"></i> 품목 리스트 ({{ activeitemcount }} 건)</span>
        </div>
        <div class="card-body p-0 flex-grow-1 overflow-hidden d-flex flex-column">
          <div ref="gridelement" class="tabulator-instance flex-grow-1" tabindex="33"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, onUnmounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useManualStore } from '@/stores/manualStore'
import type { ModalProps } from '@/types/modal'

const authstore = useAuthStore()
const { showAlert: showalert, showError: showerror, alertMessage: alertmessage, vAlert: valert, vAlertError: valerterror } = useAlerts()
const { resetForm: resetform } = useFormReset()
const manualStore = useManualStore()

const firstFocusRef = ref<HTMLInputElement | null>(null)
const searchdata = reactive({ sch_astkind: '120', sch_itemnm: '' })
const masterdata = reactive<any>({
  actkind: 'A0', cmpycd: authstore.cmpycd, astkind: '120', itemcd: '',
  itsize: '', unit: 'EA', itemnm: '', itemenm: '',
  inunit: 'EA', inqty: 1, outunit: 'EA', outqty: 1, setyn: 'N',
  agrpcd: '', agrpnm: '', bgrpcd: '', bgrpnm: '',
  stock: 0, qtypnt: 0, vatyn: 'Y', sotaxyn: 'N', udogyn: 'N',
  barcode: '', hscode: '', remark: '', useyn: 'Y', autoyn: 'N',
  in_custcd: '', in_custnm: '', maker: '', updemp: authstore.userid,
  g_weight: 0, net_weight: 0, cbm: 0, imprice: 0, omprice: 0
})

const assetoptions = ref<any[]>([]); const unitoptions = ref<any[]>([])
const gridelement = ref<HTMLElement | null>(null); let grid: Tabulator | null = null; const activeitemcount = ref(0)

const initgrid = () => {
  if (!gridelement.value) return
  grid = new Tabulator(gridelement.value, {
    layout: "fitColumns", height: "100%", pagination: "local", paginationSize: 50,
    paginationButtonCount: 5, paginationSizeSelector: [50, 100, 200, 500],
    placeholder: "데이터가 없습니다.", columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
    columns: [
      { title: "No", formatter: "rownum", width: 50, hozAlign: "center" },
      { title: "품목코드", field: "itemcd", width: 100, cssClass: "fw-bold text-primary" },
      { title: "품목명", field: "itemnm", minWidth: 200, hozAlign: "left", cssClass: "fw-bold" },
      { title: "규격", field: "itsize", width: 180, hozAlign: "left" },
      { title: "단위", field: "unit", width: 60 },
      { title: "대분류", field: "agrpnm", width: 120, hozAlign: "left" },
      { title: "중분류", field: "bgrpnm", width: 120, hozAlign: "left" },
      { title: "바코드필수", field: "autoyn", width: 90, hozAlign: "center",
        formatter: (cell) => (String(cell.getValue() || '')).toUpperCase() === 'Y' ? '<span class="badge bg-warning text-dark fw-bold">필수</span>' : '<span class="text-muted">일반</span>'
      },
      { title: "사용", field: "useyn", width: 60, hozAlign: "center",
        formatter: (cell) => cell.getValue() === 'Y' ? '<span class="text-primary fw-bold">사용</span>' : ''
      }
    ]
  })
  grid.on("rowClick", (e, row) => {
    const data = row.getData();
    Object.assign(masterdata, data);
    masterdata.in_custnm = data.custnm || data.in_custnm || '';
    masterdata.actkind = 'U0';
  })
}

async function fetchoptions() {
  try {
    const p1 = api.get('/hs00/HS00_000S_STR', { params: { gubun: 'E0', cmpycd: authstore.cmpycd, gbncd: '100' } })
    const p2 = api.get('/hs00/HS00_000S_STR', { params: { gubun: 'U0', cmpycd: authstore.cmpycd, gbncd: '', code: '' } })
    const [r1, r2] = await Promise.all([p1, p2])
    assetoptions.value = r1.data.map((n: any) => ({ codecd: n.code || n.codecd, codenm: n.cdnm || n.codenm }))
    unitoptions.value = r2.data.map((n: any) => ({ codecd: n.unit, codenm: n.unitnm }))
  } catch (e) { console.error('코드 로드 실패', e) }
}

async function search() {
  try {
    const res = await api.post('/hsba/HSBA_010U_STR', {
      actkind: 'S0', cmpycd: authstore.cmpycd,
      astkind: searchdata.sch_astkind,
      itemnm: searchdata.sch_itemnm,
      icqty: 0,  ocqty: 0,  imprice: 0, omprice: 0, stock: 0, qtypnt: 0
    })
    const data = res.data || [];
    if (grid) { grid.setData(data); activeitemcount.value = data.length }
  } catch (e) { valerterror('조회 실패') }
}

async function save() {
  if (!masterdata.itemnm || !masterdata.itsize) return valerterror('품목명과 규격은 필수입니다.')
  if (!confirm('저장하시겠습니까?')) return
  try {
    const payload = {
      ...masterdata,
      userid: authstore.userid,
      updemp: authstore.userid,
      icqty: Number(masterdata.icqty || masterdata.inqty || 1),
      ocqty: Number(masterdata.ocqty || masterdata.outqty || 1),
      imprice: Number(masterdata.imprice || 0),
      omprice: Number(masterdata.omprice || 0),
      stock: Number(masterdata.stock || 0),
      qtypnt: Number(masterdata.qtypnt || 0),
      agrpcd: masterdata.agrpcd || '',
      bgrpcd: masterdata.bgrpcd || '',
      itemenm: masterdata.itemenm || '',
      hscode: masterdata.hscode || '',
      in_custcd: masterdata.in_custcd || '',
      remark: masterdata.remark || ''
    }
    await api.post('/hsba/HSBA_010U_STR', payload)
    valert('저장되었습니다.')
    search()
    initialize()
  } catch (e: any) {
    const errMsg = e.response?.data?.message || e.response?.data?.msg || e.message || '저장 중 오류 발생'
    valerterror(errMsg)
  }
}

function initialize() {
  const currentasset = searchdata.sch_astkind; resetform(masterdata)
  Object.assign(masterdata, {
    actkind: 'A0', cmpycd: authstore.cmpycd, unit: 'EA', astkind: currentasset,
    setyn: 'N', useyn: 'Y', autoyn: 'N', stock: 0, qtypnt: 0, imprice: 0, omprice: 0,
    vatyn: 'Y', sotaxyn: 'N', udogyn: 'N', inunit: 'EA', inqty: 1, outunit: 'EA', outqty: 1,
    updemp: authstore.userid, in_custcd: '', in_custnm: '', agrpcd: '', agrpnm: '', bgrpcd: '', bgrpnm: '', maker: '',
    g_weight: 0, net_weight: 0, cbm: 0
  })
  nextTick(() => firstFocusRef.value?.focus())
}

const handleassetchange = () => { initialize(); search(); }

const modalvisible = ref(false); const modalprops = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openhelp(type: string) {
  let config: any = {}
  if (type === 'agrp') {
    config = {
      title: '대분류 선택',
      path: '/hs00/HS00_000S_STR',
      data: { gubun: 'g0', gbncd: masterdata.astkind },
      field: 'agrpnm',
      columns: [{ title: '코드', field: 'agrpcd', width: 80 }, { title: '분류명', field: 'agrpnm', width: 180 }]
    }
  } else if (type === 'bgrp') {
    config = {
      title: '중분류 선택',
      path: '/hs00/HS00_000S_STR',
      data: { gubun: 'g1', gbncd: masterdata.astkind, code: masterdata.agrpcd },
      field: 'bgrpnm',
      columns: [{ title: '코드', field: 'bgrpcd', width: 80 }, { title: '분류명', field: 'bgrpnm', width: 180 }]
    }
  } else if (type === 'cust') {
    config = {
      title: '거래처 선택',
      path: '/ha00/HA00_00P_STR',
      data: { gubun: 'c4' },
      field: 'custnm',
      columns: [{ title: '코드', field: 'custcd', width: 100 }, { title: '거래처명', field: 'custnm', width: 200 }]
    }
  }

  if (!config.path) return
  Object.assign(modalprops, {
    title: config.title, path: config.path, defaultField: config.field,
    data: { ...config.data, cmpycd: authstore.cmpycd }, columns: config.columns,
    onConfirm: (d: any) => {
      const n = d;
      if (type === 'agrp') { masterdata.agrpcd = n.agrpcd || n.code || ''; masterdata.agrpnm = n.agrpnm || n.cdnm || ''; }
      else if (type === 'bgrp') { masterdata.bgrpcd = n.bgrpcd || n.code || ''; masterdata.bgrpnm = n.bgrpnm || n.cdnm || ''; }
      else if (type === 'cust') { masterdata.in_custcd = n.custcd || n.code || ''; masterdata.in_custnm = n.custnm || n.cdnm || ''; }
    }
  })
  modalvisible.value = true
}

/** 🚀 [HSOD100U 표준 키보드 단축키 핸들러 연동] */
function handleGlobalShortcuts(e: KeyboardEvent) {
  if (e.altKey) {
    const key = e.key.toLowerCase()
    if (key === 'n') { e.preventDefault(); initialize() }
    else if (key === 'f') { e.preventDefault(); search() }
    else if (key === 's') { e.preventDefault(); save() }
    else if (key === 'h') { e.preventDefault(); manualStore.open('HSBA010U') }
  }
}

onMounted(async () => {
  window.addEventListener('keydown', handleGlobalShortcuts)
  await fetchoptions()
  nextTick(() => { initgrid(); search(); firstFocusRef.value?.focus() })
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleGlobalShortcuts)
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
input:focus, select:focus, button:focus {
  border-color: #005a9f !important;
  box-shadow: 0 0 0 0.2rem rgba(0, 90, 159, 0.25) !important;
  outline: none;
}
</style>
