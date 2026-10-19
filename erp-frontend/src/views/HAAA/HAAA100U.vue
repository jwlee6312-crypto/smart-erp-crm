<!--
	=============================================================
	프로그램명	: 회사정보관리 (HAAA100U)
	작성일자	: 2025.03.14
	설명        : 시스템 전체 회사 정보 관리 (ASP 원본 HAAA_100U_IF.asp 기준)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-building-fill me-2 text-primary" style="font-size: 18px;"></i>
        시스템 관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">회사정보관리 (HAAA100U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [상단] 조회 필터 영역 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
              <col style="width: 100px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light">회 사 명</th>
                <td class="px-2">
                  <input v-model="searchForm.cmpynm" class="form-control form-control-sm" style="max-width: 300px;" placeholder="상호 검색" @keyup.enter="search" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [중간] 상세 입력 영역 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <span class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-primary"></i>회사 상세 정보</span>
          <div v-if="formData.actkind === 'U1'" class="badge bg-warning text-dark px-2">수정 중</div>
          <div v-else class="badge bg-primary text-white px-2">신규 등록</div>
        </div>
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required bg-light">회사코드</th>
                <td><input v-model="formData.ltdcd" type="text" class="form-control form-control-sm fw-bold text-primary text-center" maxlength="10" :disabled="formData.actkind === 'U1'" /></td>
                <th class="required bg-light">상 호</th>
                <td><input v-model="formData.ltdnm" type="text" class="form-control form-control-sm" /></td>
                <th class="bg-light">상호(영문)</th>
                <td><input v-model="formData.ltdenm" type="text" class="form-control form-control-sm" /></td>
              </tr>
              <tr>
                <th class="required bg-light">사업자번호</th>
                <td><input v-model="formData.saupno" type="text" class="form-control form-control-sm text-center" maxlength="10" /></td>
                <th class="bg-light">대 표 자</th>
                <td><input v-model="formData.bossnm" type="text" class="form-control form-control-sm" /></td>
                <th class="required bg-light">법인번호</th>
                <td><input v-model="formData.legalno" type="text" class="form-control form-control-sm text-center" maxlength="13" /></td>
              </tr>
              <tr>
                <th class="required bg-light">주 소</th>
                <td colspan="3">
                  <AddressPopupForm
                    v-model:postno="formData.postno"
                    v-model:address="formData.address"
                    v-model:d_address="formData.d_address"
                  />
                </td>
                <th class="bg-light">연 락 처</th>
                <td><input v-model="formData.telno" type="text" class="form-control form-control-sm text-center" /></td>
              </tr>
              <tr>
                <th class="bg-light">업 태</th>
                <td><input v-model="formData.uptae" type="text" class="form-control form-control-sm" /></td>
                <th class="bg-light">종 목</th>
                <td><input v-model="formData.upjong" type="text" class="form-control form-control-sm" /></td>
                <th class="bg-light">도 메 인</th>
                <td><input v-model="formData.domain" type="text" class="form-control form-control-sm" /></td>
              </tr>
              <tr>
                <th class="bg-light">설 립 일</th>
                <td><input v-model="formData.fondymd" type="date" class="form-control form-control-sm" /></td>
                <th class="required bg-light">결 산 월</th>
                <td>
                  <input v-model="formData.clsmm" type="number" class="form-control form-control-sm text-center" min="1" max="12" />
                </td>
                <th class="bg-light">접수여부</th>
                <td class="ps-4">
                  <div class="form-check form-switch m-0 d-flex align-items-center h-100">
                    <input v-model="formData.useyn" class="form-check-input" type="checkbox" true-value="Y" false-value="N" id="useyn100">
                    <label class="form-check-label ms-2 small fw-bold" for="useyn100">{{ formData.useyn === 'Y' ? '접수' : '미접수' }}</label>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
          <i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>
          <span class="fw-bold small text-dark">회사 리스트</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, nextTick } from 'vue'
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

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps } = useCommonHelp()

const searchForm = reactive({ cmpynm: '' })
const formData = reactive({
  actkind: 'S1', ltdcd: '', ltdnm: '', ltdenm: '', saupno: '', bossnm: '', legalno: '',
  postno: '', address: '', d_address: '', telno: '', uptae: '', upjong: '', domain: '',
  fondymd: '', clsmm: '12', useyn: 'Y', cmpycd: authStore.cmpycd, userid: authStore.userid
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

async function search() {
  try {
    const res = await api.post('/haba/HABA_900U_STR', {
      actkind: 'S1',
      cmpycd: authStore.cmpycd,
      ltdnm: searchForm.cmpynm,
      saupno: '', legalno: '', ltdenm: '', bossnm: '', address: '', upjong: '', uptae: ''
    })
    mainGrid?.setData(res.data || [])
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

async function save() {
  if (!formData.ltdcd || !formData.ltdnm || !formData.saupno) return vAlertError('필수 입력 항목을 확인하세요.')
  if (!confirm('저장하시겠습니까?')) return
  try {
    const act = formData.actkind === 'S1' ? 'A0' : 'A0' // ASP 원본에서도 저장 시 A0 사용
    const param = {
      ...formData,
      actkind: 'A0',
      cmpycd: formData.ltdcd, // LTDCD를 회사코드로 사용
      saupno: formData.saupno.replace(/-/g, ''),
      legalno: formData.legalno.replace(/-/g, ''),
      fondymd: formData.fondymd.replace(/-/g, ''),
      userid: authStore.userid
    }
    await api.post('/haba/HABA_900U_STR', param)
    vAlert('저장되었습니다.')
    search()
  } catch (e) { vAlertError('저장 실패') }
}

function initialize() {
  resetForm(formData)
  formData.actkind = 'S1'; formData.clsmm = '12'; formData.useyn = 'Y';
  formData.cmpycd = authStore.cmpycd; formData.userid = authStore.userid;
}

onMounted(() => {
  if (mainGridRef.value) {
    mainGrid = new Tabulator(mainGridRef.value, {
      layout: "fitColumns", height: "100%", selectable: 1,
      columnDefaults: { headerSort: false, headerHozAlign: "center", vertAlign: "middle" },
      columns: [
        { title: "회사코드", field: "cmpycd", width: 100, hozAlign: "center", cssClass: "fw-bold text-primary border-end" },
        { title: "상호", field: "cmpynm", minWidth: 200, hozAlign: "left", cssClass: "fw-bold" },
        { title: "사업자번호", field: "saupno", width: 130, hozAlign: "center",
          formatter: (c) => {
            const v = String(c.getValue() || '');
            return v.length === 10 ? `${v.substring(0,3)}-${v.substring(3,5)}-${v.substring(5)}` : v;
          }
        },
        { title: "대표자", field: "bossnm", width: 100, hozAlign: "center" },
        { title: "연락처", field: "telno", width: 130, hozAlign: "center" },
        { title: "등록일", field: "addtime", width: 110, hozAlign: "center",
          formatter: (c) => String(c.getValue() || '').substring(0, 10)
        },
        { title: "접수", field: "useyn", width: 60, hozAlign: "center",
          formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X'
        }
      ]
    })

    mainGrid.on("rowClick", (e, row) => {
      const d = row.getData()
      Object.assign(formData, {
        actkind: 'U1',
        ltdcd: d.cmpycd,
        ltdnm: d.cmpynm,
        ltdenm: d.cmpyenm,
        saupno: d.saupno,
        legalno: d.legalno,
        bossnm: d.bossnm,
        postno: d.postno,
        address: d.address,
        d_address: '', // 상세주소 분리 필요 시 추가 처리
        telno: d.telno,
        upjong: d.upjong,
        uptae: d.uptae,
        domain: d.domain,
        fondymd: d.fondymd ? `${d.fondymd.substring(0,4)}-${d.fondymd.substring(4,6)}-${d.fondymd.substring(6,8)}` : '',
        clsmm: d.closemm,
        useyn: d.useyn
      })
    })
  }
  nextTick(search)
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
