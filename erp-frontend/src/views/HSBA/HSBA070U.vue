<!--
	=============================================================
	프로그램명	: 거래처등록 (HSBA070U)
	작성일자	: 2025.02.27
	설명        : 영업/구매 거래처 마스터 관리 (Tab 키 순항 및 Alt 키보드 단축키 연동)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <!-- 💡 Haion ERP 표준 레이아웃: 하단 공백 없이 꽉 채우는 구조 -->
  <div class="erp-container">
    <!-- 🚀 1. 상단 액션 바: 고정 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-1 px-3 sticky-top shadow-sm flex-shrink-0">
      <div class="fw-bold text-dark d-flex align-items-center" style="font-size: 13px;">
        <i class="bi bi-briefcase-fill me-2 text-primary"></i>
        기본정보 > <span class="text-primary fw-bolder">거래처등록 (HSBA070U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1">
        <button class="btn-erp btn-init" @click="initialize" title="Alt+N: 신규초기화">신규(N)</button>
        <button class="btn-erp btn-search" @click="search" title="Alt+F: 조회">조회(F)</button>
        <button class="btn-erp btn-save" @click="save" title="Alt+S: 저장">저장(S)</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역: flex-grow-1 및 overflow-hidden으로 레이아웃 고정 -->
    <div class="flex-grow-1 d-flex flex-column gap-2 p-2 overflow-hidden">
      <!-- 🅰️ 조회 조건 영역: 고정 -->
      <div class="card border shadow-sm flex-shrink-0">
        <div class="card-body p-1 bg-light-subtle">
          <div class="d-flex align-items-center gap-3">
            <div class="input-group input-group-sm flex-nowrap" style="width: 180px;">
              <span class="input-group-text fw-bold border-0 bg-transparent">종류</span>
              <select v-model="searchParams.qcustgbn" class="form-select border-0 bg-white" tabindex="1" @change="search">
                <option value="000">전체</option>
                <option v-for="opt in options.custGbn" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
              </select>
            </div>
            <div class="input-group input-group-sm flex-nowrap" style="width: 180px;">
              <span class="input-group-text fw-bold border-0 bg-transparent">상태</span>
              <select v-model="searchParams.qstatus" class="form-select border-0 bg-white" tabindex="2" @change="search">
                <option v-for="opt in options.status" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
              </select>
            </div>
            <div class="input-group input-group-sm flex-nowrap" style="width: 300px;">
              <span class="input-group-text fw-bold border-0 bg-transparent">거래처명</span>
              <input v-model="searchParams.qcustnm" type="text" class="form-control border-0 bg-white" tabindex="3" placeholder="거래처명 입력..." @keyup.enter="search" />
              <button class="btn btn-dark btn-sm" tabindex="4" @click="search"><i class="bi bi-search"></i></button>
            </div>
          </div>
        </div>
      </div>

      <!-- 🅱️ 상세 입력 폼 영역: 탭키 순서 순항 고정 -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-header py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="background-color: #f8f9fa;">
          <span class="fw-bold small text-dark"><i class="bi bi-pencil-square me-1"></i> 거래처 상세 정보</span>
          <div class="d-flex gap-2">
            <span v-if="masterData.actkind === 'U0'" class="badge bg-warning text-dark" style="font-size: 10px;">수정 모드</span>
            <span v-else class="badge bg-primary" style="font-size: 10px;">신규 등록</span>
          </div>
        </div>
        <div class="card-body p-0">
          <table class="erp-table-full border-0">
            <colgroup>
              <col style="width: 80px;" /><col />
              <col style="width: 80px;" /><col />
              <col style="width: 80px;" /><col />
              <col style="width: 80px;" /><col />
              <col style="width: 80px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required">거래처코드</th>
                <td><input v-model="masterData.custcd" type="text" class="form-control form-control-sm text-center fw-bold text-primary" tabindex="5" maxlength="7" :readonly="masterData.actkind === 'U0'" /></td>
                <th>외부코드</th>
                <td><input v-model="masterData.outcustcd" type="text" class="form-control form-control-sm" tabindex="6" maxlength="7" /></td>
                <th class="required">상&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;호</th>
                <td><input ref="firstFocusRef" v-model="masterData.custnm" type="text" class="form-control form-control-sm fw-bold border-primary-subtle" tabindex="7" maxlength="50" /></td>
                <th>사업자번호</th>
                <td><input v-model="masterData.custno" type="text" class="form-control form-control-sm" tabindex="8" maxlength="13" placeholder="- 없이" /></td>
                <th>법인번호</th>
                <td><input v-model="masterData.legalno" type="text" class="form-control form-control-sm" tabindex="9" maxlength="14" placeholder="- 없이" /></td>
              </tr>
              <tr>
                <th>종&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;류</th>
                <td>
                  <select v-model="masterData.custgbn" class="form-select form-select-sm" tabindex="10" @change="handleCustGbnChange">
                    <option v-for="opt in options.custGbn" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th>대&nbsp;&nbsp;표&nbsp;&nbsp;자</th>
                <td><input v-model="masterData.bossnm" type="text" class="form-control form-control-sm" tabindex="11" maxlength="30" /></td>
                <th>업&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;태</th>
                <td><input v-model="masterData.custtype" type="text" class="form-control form-control-sm" tabindex="12" maxlength="50" /></td>
                <th>종&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;목</th>
                <td><input v-model="masterData.custkind" type="text" class="form-control form-control-sm" tabindex="13" maxlength="50" /></td>
                <th>연&nbsp;락&nbsp;처</th>
                <td><input v-model="masterData.telno" type="text" class="form-control form-control-sm" tabindex="14" maxlength="30" /></td>
              </tr>
              <tr>
                <th>주&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;소</th>
                <td colspan="7">
                  <AddressPopupForm
                    v-model:postno="masterData.postno"
                    v-model:address="masterData.address"
                    v-model:d_address="masterData.d_address"
                    tabindex="15"
                    @open-address="handleOpenHelp('ADDR')"
                  />
                </td>
                <th>FAX번호</th>
                <td><input v-model="masterData.faxno" type="text" class="form-control form-control-sm" tabindex="16" maxlength="30" /></td>
              </tr>
              <tr>
                <th>매입단가</th>
                <td>
                  <select v-model="masterData.inprcgbn" class="form-select form-select-sm" tabindex="17">
                    <option value="000">선택</option>
                    <option v-for="opt in options.inPrcGbn" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th>매출단가</th>
                <td>
                  <select v-model="masterData.outprcgbn" class="form-select form-select-sm" tabindex="18">
                    <option value="000">선택</option>
                    <option v-for="opt in options.outPrcGbn" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th>여신한도액</th>
                <td><input v-model="masterData.hdamt" type="number" class="form-control form-control-sm text-end" tabindex="19" /></td>
                <th>여신기한</th>
                <td>
                  <div class="d-flex gap-1 align-items-center">
                    <input v-model="masterData.rcvdd" type="number" class="form-control form-control-sm text-end" style="width: 40px;" tabindex="20" />
                    <span class="small">일</span>
                    <select v-model="masterData.gigbcd" class="form-select form-select-sm flex-grow-1" tabindex="21">
                      <option v-for="opt in options.gigGbn" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                    </select>
                  </div>
                </td>
                <th>대&nbsp;분&nbsp;류</th>
                <td>
                  <select v-model="masterData.agrpcd" class="form-select form-select-sm" tabindex="22">
                    <option value="000">선택</option>
                    <option v-for="opt in options.agrp" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th>중&nbsp;분&nbsp;류</th>
                <td>
                  <select v-model="masterData.bgrpcd" class="form-select form-select-sm" tabindex="23">
                    <option value="000">선택</option>
                    <option v-for="opt in options.bgrp" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th>소&nbsp;분&nbsp;류</th>
                <td>
                  <select v-model="masterData.cgrpcd" class="form-select form-select-sm" tabindex="24">
                    <option value="000">선택</option>
                    <option v-for="opt in options.cgrp" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th>담&nbsp;당&nbsp;자</th>
                <td><input v-model="masterData.cdamdang" type="text" class="form-control form-control-sm" tabindex="25" maxlength="30" /></td>
                <th>연락처(SMS)</th>
                <td><input v-model="masterData.ctelno" type="text" class="form-control form-control-sm" tabindex="26" maxlength="30" /></td>
                <th>전자메일</th>
                <td><input v-model="masterData.cemail" type="text" class="form-control form-control-sm" tabindex="27" maxlength="50" /></td>
              </tr>
              <tr>
                <th>신용등급</th>
                <td>
                  <select v-model="masterData.singrd" class="form-select form-select-sm" tabindex="28">
                    <option value="A">A등급</option><option value="B">B등급</option><option value="C">C등급</option>
                  </select>
                </td>
                <th>적용일자</th>
                <td><input v-model="uiStdYmd" type="date" class="form-control form-control-sm" tabindex="29" /></td>
                <th>전자여부</th>
                <td>
                  <div class="form-check form-switch m-0"><input v-model="masterData.elcyn" class="form-check-input" type="checkbox" true-value="Y" false-value="N" tabindex="30"> <span class="small fw-bold">전자계산서</span></div>
                </td>
                <th>상&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;태</th>
                <td>
                  <select v-model="masterData.status" class="form-select form-select-sm" tabindex="31">
                    <option v-for="opt in options.status" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th>사용여부</th>
                <td>
                  <div class="form-check form-switch m-0"><input v-model="masterData.useyn" class="form-check-input" type="checkbox" true-value="Y" false-value="N" tabindex="32"> <span class="small fw-bold">사용</span></div>
                </td>
              </tr>
              <tr>
                <th>입금은행</th>
                <td><input v-model="masterData.banknm" type="text" class="form-control form-control-sm" tabindex="33" maxlength="30" /></td>
                <th>입금통장</th>
                <td><input v-model="masterData.gujoa" type="text" class="form-control form-control-sm" tabindex="34" maxlength="30" /></td>
                <th>비&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;고</th>
                <td colspan="5"><input v-model="masterData.remark" type="text" class="form-control form-control-sm w-100" tabindex="35" maxlength="50" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Ⓒ 거래처 목록 그리드: flex-grow-1로 하단 공백 없이 채움 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
          <span class="fw-bold small text-dark"><i class="bi bi-list-ul me-1"></i> 거래처 목록 ({{ activeItemCount }} 건)</span>
          <span class="text-muted" style="font-size: 11px;">※ 행 클릭 시 상세 정보가 로드됩니다.</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="gridElement" class="tabulator-instance flex-grow-1" tabindex="36"></div>
        </div>
      </div>
    </div>

    <Modal v-model:visible="modalVisible" :modalProps="modalProps" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import AddressPopupForm from '@/components/AddressPopupForm.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'
import { useManualStore } from '@/stores/manualStore'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()
const manualStore = useManualStore()

const firstFocusRef = ref<HTMLInputElement | null>(null)

// 1. 상태 관리
const searchParams = reactive({ qcustgbn: '000', qstatus: '010', qcustnm: '' })
const masterData = reactive<any>({
  actkind: 'I0', cmpycd: authStore.cmpycd, custcd: '', custno: '', custnm: '', custgbn: '010',
  bossnm: '', legalno: '', custkind: '', custtype: '', telno: '', faxno: '', postno: '', address: '', d_address: '',
  iogbn: '010', inprcgbn: '190', outprcgbn: '200', hdamt: 0, rcvdd: 0, gigbcd: '305',
  agrpcd: '000', bgrpcd: '000', cgrpcd: '000', area: '000', cdamdang: '', ctelno: '', cemail: '',
  remark: '', status: '010', banknm: '', gujoa: '', fndymd: '', stdymd: '', clsymd: '99991231',
  singrd: 'A', elcyn: 'Y', useyn: 'Y', outcustcd: '', agentyn: 'N', updemp: authStore.userid
})

const uiStdYmd = computed({
  get: () => masterData.stdymd ? `${masterData.stdymd.substring(0, 4)}-${masterData.stdymd.substring(4, 6)}-${masterData.stdymd.substring(6, 8)}` : '',
  set: (v) => masterData.stdymd = v.replace(/-/g, '')
})

const options = reactive<any>({ custGbn: [], status: [], inPrcGbn: [], outPrcGbn: [], gigGbn: [], agrp: [], bgrp: [], cgrp: [], area: [], ioGbn: [] })
const gridElement = ref<HTMLElement | null>(null); const grid = ref<Tabulator | null>(null); const activeItemCount = ref(0)

// 2. 그리드 초기화
const initGrid = () => {
  if (!gridElement.value) return
  grid.value = new Tabulator(gridElement.value, {
    layout: "fitColumns", height: "100%", pagination: "local", paginationSize: 10,
    columnDefaults: { headerSort: false, headerHozAlign: "center" },
    columns: [
      { title: "코드", field: "custcd", width: 90, hozAlign: "center", cssClass: "fw-bold text-primary" },
      { title: "거래처상호", field: "custnm", minWidth: 250, hozAlign: "left" },
      { title: "사업자번호", field: "custno", width: 150, hozAlign: "center" },
      { title: "대표자", field: "bossnm", width: 150 },
      { title: "연락처", field: "telno", width: 150, hozAlign: "left" },
      { title: "상태", field: "statusnm", width: 100 },
      { title: "외부CD", field: "outcustcd", width: 120 },
      { title: "사용", field: "useyn", width: 80, hozAlign: "center",
        formatter: (cell) => {
          const val = String(cell.getValue() || '').trim().toUpperCase();
          return val === 'Y' ? '<b class="text-primary">사용</b>' : '';
        }
      }
    ]
  })
  grid.value.on("rowClick", (e, row) => {
    Object.assign(masterData, row.getData()); masterData.actkind = 'U0'
  })
}

// 3. 기능 구현
async function fetchOptions() {
  const getHS = (gbn: string, cd: string) => api.post('/hs00/HS00_000S_STR', { gubun: gbn, gbncd: cd, cmpycd: authStore.cmpycd })
  const getHA = (gbn: string, cd: string) => api.post('/ha00/HA00_00P_STR', { gubun: gbn, gbncd: cd, cmpycd: authStore.cmpycd })

  try {
    const [o1, o2, o3, o4, o5, o6, o7, o8, o9] = await Promise.all([
      getHA('E0', '270'), getHA('E0', '280'), getHS('E0', '190'), getHS('E0', '200'),
      getHS('E0', '305'), getHS('E0', '400'), getHS('E0', '410'), getHS('E0', '420'),
      getHA('E0', '340')
    ])
    const map = (r: any) => r.data.map((i: any) => ({ codecd: String(i.code || i.codecd || '').trim(), codenm: String(i.cdnm || i.codenm || '').trim() }))
    options.custGbn = map(o1); options.status = map(o2); options.inPrcGbn = map(o3); options.outPrcGbn = map(o4)
    options.gigGbn = map(o5); options.agrp = map(o6); options.bgrp = map(o7); options.cgrp = map(o8); options.ioGbn = map(o9)
  } catch (e) {}
}

async function search() {
  try {
    const res = await api.post('/hsba/HSBA_070U_STR', {
      actkind: 'S0', cmpycd: authStore.cmpycd, custgbn: searchParams.qcustgbn === '000' ? '000' : searchParams.qcustgbn,
      status: searchParams.qstatus, custnm: searchParams.qcustnm, updemp: authStore.userid
    })
    grid.value?.setData(res.data); activeItemCount.value = res.data.length
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  if (!masterData.custnm) return vAlertError('상호는 필수입니다.')
  if (masterData.custgbn === '010' && !masterData.custno) return vAlertError('사업자번호를 입력하세요.')
  if (!confirm('저장하시겠습니까?')) return
  try {
    const res = await api.post('/hsba/HSBA_070U_STR', { ...masterData, actkind: masterData.actkind === 'U0' ? 'U0' : 'I0' })
    if (res.data?.[0]?.RESULT === 'Y') { vAlertError(res.data[0].MSG || '저장 실패') }
    else { vAlert('정상 처리되었습니다.'); search(); initialize() }
  } catch (e) { vAlertError('저장 중 오류 발생') }
}

function initialize() {
  resetForm(masterData)
  Object.assign(masterData, {
    actkind: 'A0', cmpycd: authStore.cmpycd, custgbn: '010', iogbn: '010', inprcgbn: '190', outprcgbn: '200',
    gigbcd: '305', stdymd: new Date().toISOString().substring(0, 10).replace(/-/g, ''), clsymd: '99991231',
    singrd: 'A', elcyn: 'Y', useyn: 'Y', agentyn: 'N', updemp: authStore.userid, outcustcd: '', area: '000'
  })
  nextTick(() => firstFocusRef.value?.focus())
}

const handleCustGbnChange = () => { if(masterData.custgbn === '030') masterData.elcyn = 'N'; }

const handleOpenHelp = (type: string) => {
  if (type === 'ADDR') {
    if (!masterData.custcd) return vAlertError('거래처를 먼저 선택하세요.');
    openHelp('ADDR', { gubun: 'C5', cmpycd: authStore.cmpycd, gbncd: masterData.custcd }, (d: any) => {
      masterData.postno = d.postno; masterData.address = d.address;
    });
  }
}

/** 🚀 [HSOD100U 표준 키보드 단축키 핸들러 연동] */
function handleGlobalShortcuts(e: KeyboardEvent) {
  if (e.altKey) {
    const key = e.key.toLowerCase()
    if (key === 'n') { e.preventDefault(); initialize() }
    else if (key === 'f') { e.preventDefault(); search() }
    else if (key === 's') { e.preventDefault(); save() }
    else if (key === 'h') { e.preventDefault(); manualStore.open('HSBA070U') }
  }
}

onMounted(async () => {
  window.addEventListener('keydown', handleGlobalShortcuts)
  await fetchOptions()
  nextTick(() => { initGrid(); search(); firstFocusRef.value?.focus() })
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
