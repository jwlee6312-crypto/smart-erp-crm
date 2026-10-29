<!--
	=============================================================
	프로그램명	: 견적등록 (HSQT100U)
	작성일자	: 2026.10.24
	작성자      : AI Assistant
	설명        : HSOD100U 표준 디자인 패턴 적용 및 업체담당자명/메일주소 필드 추가 견적등록 화면
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 상단 글로벌 액션 바 (HSOD100U 표준) -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-text-fill me-2 text-primary" style="font-size: 18px;"></i>
        영업관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">견적등록 (HSQT100U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchMaster">조회</button>
        <button class="btn-erp btn-save" @click="saveData">저장</button>
        <button class="btn-erp btn-delete" @click="deleteData">삭제</button>
        <button class="btn-erp btn-print" @click="printQuotation"><i class="bi bi-printer-fill me-1"></i>견적서출력</button>
        <button class="btn-erp btn-print" @click="sendEmailReport"><i class="bi bi-envelope-fill me-1"></i>메일보내기</button>
      </div>
    </div>

    <!-- 💡 검색 필터 (HSOD100U 표준 덴스 테이블) -->
    <div class="p-2 bg-light border-bottom flex-shrink-0">
      <div class="card border shadow-sm">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 10%;" /><col style="width: 23%;" />
              <col style="width: 10%;" /><col style="width: 23%;" />
              <col style="width: 10%;" /><col style="width: 24%;" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light fw-bold">견적일자</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <input type="date" v-model="filter.frdt" class="form-control form-control-sm" />
                  <span>~</span>
                  <input type="date" v-model="filter.todt" class="form-control form-control-sm" />
                </td>
                <th class="text-center bg-light border-start fw-bold">거래처명</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="filter.custnm" class="form-control form-control-sm" placeholder="거래처명 입력" @keyup.enter="fetchMaster" />
                    <button class="btn btn-outline-secondary" type="button" @click="openCustHelp('filter')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="text-center bg-light border-start fw-bold">담당자</th>
                <td><input v-model="filter.empno" class="form-control form-control-sm" placeholder="담당자 사번" @keyup.enter="fetchMaster" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 📑 메인 50:50 분할 레이아웃 영역 (HSOD100U 표준) -->
    <div class="flex-grow-1 p-2 bg-light overflow-hidden d-flex flex-row gap-2">
      <!-- (Left 50%) 견적 마스터 리스트 -->
      <div class="card border shadow-sm overflow-hidden d-flex flex-column" style="flex: 0 0 50%; width: 50%;">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="small fw-bold text-dark"><i class="bi bi-list-ul me-1 text-primary"></i> 견적 마스터 목록</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="masterGridRef" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>

      <!-- (Right 50%) 견적 상세 입력 폼 및 품목 그리드 -->
      <div class="card border shadow-sm overflow-hidden d-flex flex-column bg-white" style="flex: 0 0 50%; width: 50%;">
        <div class="card-header bg-white border-bottom py-1 px-3 d-flex justify-content-between align-items-center">
          <span class="small fw-bold text-primary"><i class="bi bi-pencil-square me-2"></i>견적 상세 품목 정보</span>
        </div>
        <div class="card-body p-3 overflow-auto scrollbar-sm d-flex flex-column gap-3">
          <table class="erp-table-dense w-100">
            <colgroup><col style="width: 110px;" /><col /></colgroup>
            <tbody>
              <tr>
                <th class="bg-light">견적번호</th>
                <td><input v-model="formMaster.qutno" class="form-control form-control-sm bg-light" readonly placeholder="자동생성" /></td>
              </tr>
              <tr>
                <th class="bg-light">견적일자</th>
                <td><input type="date" v-model="formMaster.qutdt" class="form-control form-control-sm w-50" /></td>
              </tr>
              <tr>
                <th class="bg-light">거래처명</th>
                <td>
                  <div class="input-group input-group-sm w-75">
                    <input v-model="formMaster.custnm" class="form-control form-control-sm" placeholder="거래처명 직접입력 또는 팝업선택" />
                    <button class="btn btn-outline-secondary" type="button" @click="openCustHelp('form')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
              <tr>
                <th class="bg-light">업체담당자명</th>
                <td><input v-model="formMaster.custemp" class="form-control form-control-sm w-50" placeholder="업체담당자명 입력" /></td>
              </tr>
              <tr>
                <th class="bg-light">메일주소</th>
                <td><input v-model="formMaster.email" type="email" class="form-control form-control-sm" placeholder="수신 메일주소 입력" /></td>
              </tr>
              <tr>
                <th class="bg-light">비고</th>
                <td><input v-model="formMaster.remk" class="form-control form-control-sm" placeholder="비고 입력" /></td>
              </tr>
            </tbody>
          </table>

          <!-- 상세 품목 그리드 -->
          <div class="flex-grow-1 d-flex flex-column" style="min-height: 250px;">
            <div class="small fw-bold text-dark mb-1 d-flex justify-content-between align-items-center">
              <span><i class="bi bi-box-seam me-1"></i> 품목 상세 목록 (품목명 클릭 시 팝업 선택 가능)</span>
              <button class="btn-erp btn-init" @click="addDetailRow">행추가</button>
            </div>
            <div ref="detailGridRef" class="tabulator-instance flex-grow-1 border rounded" style="height: 250px;"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import Modal from '@/components/Modal.vue'
import { useCommonHelp } from '@/composables/useCommonHelp'
import { useAuthStore } from '@/stores/authStore'
import { usePrintReport } from '@/composables/usePrintReport'

const authStore = useAuthStore()
const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()
const { printSpecification } = usePrintReport()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

const filter = reactive({
  frdt: '',
  todt: '',
  custcd: '',
  custnm: '',
  empno: ''
})

const formMaster = reactive({
  qutno: '',
  qutdt: new Date().toISOString().split('T')[0],
  custcd: '',
  custnm: '',
  custemp: '',
  email: '',
  remk: ''
})

let activeDetailRow: any = null

const masterGridRef = ref<HTMLDivElement | null>(null)
const detailGridRef = ref<HTMLDivElement | null>(null)
let masterTabulator: Tabulator | null = null
let detailTabulator: Tabulator | null = null

onMounted(() => {
  const today = new Date()
  const firstDay = new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0]
  const lastDay = today.toISOString().split('T')[0]
  filter.frdt = firstDay
  filter.todt = lastDay

  initMasterTable()
  initDetailTable()
  fetchMaster()
})

const initMasterTable = () => {
  if (!masterGridRef.value) return
  masterTabulator = new Tabulator(masterGridRef.value, {
    layout: 'fitColumns',
    height: '100%',
    selectable: 1,
    placeholder: '조회된 데이터가 없습니다.',
    columns: [
      { formatter: 'rownum', title: 'No', width: 80, hozAlign: 'center' },
      { title: '견적번호', field: 'qutno', width: 120, hozAlign: 'center' },
      { title: '견적일자', field: 'qutdt', width: 100, hozAlign: 'center', formatter: (cell) => {
          const val = cell.getValue()
          if (val && val.length === 8 && !val.includes('-')) {
            return `${val.substring(0, 4)}-${val.substring(4, 6)}-${val.substring(6, 8)}`
          }
          return val
        }
      },
      { title: '거래처명', field: 'custnm', width: 150, hozAlign: 'left' },
      { title: '업체담당자', field: 'custemp', width: 100, hozAlign: 'center' },
      { title: '메일주소', field: 'email', width: 150, hozAlign: 'left' },
      { title: '합계금액', field: 'totamt', width: 110, hozAlign: 'right', formatter: 'money', formatterParams: { thousand: ',' } },
      { title: '비고', field: 'remk', width: 140, hozAlign: 'left' }
    ]
  })

  masterTabulator.on('rowClick', (e, row) => {
    const data = row.getData()
    Object.assign(formMaster, data)
    if (formMaster.qutdt && formMaster.qutdt.length === 8 && !formMaster.qutdt.includes('-')) {
      formMaster.qutdt = `${formMaster.qutdt.substring(0, 4)}-${formMaster.qutdt.substring(4, 6)}-${formMaster.qutdt.substring(6, 8)}`
    }
    const quatym = data.quatym || (data.qutdt ? data.qutdt.replace(/-/g, '').substring(0, 6) : '')
    const quatno = data.quatno || data.qutno
    fetchDetail(quatym, quatno)
  })
}

const calculateRow = (row: any) => {
  const data = row.getData()
  const quty = Math.round(Number(data.quty) || 0)
  const uprc = Math.round(Number(data.uprc) || 0)
  const amtx = quty * uprc
  const vat = Math.floor(amtx * 0.1)
  row.update({ quty, uprc, amtx, vat })
}

const initDetailTable = () => {
  if (!detailGridRef.value) return
  detailTabulator = new Tabulator(detailGridRef.value, {
    layout: 'fitColumns',
    height: '100%',
    placeholder: '상세 품목이 없습니다.',
    columns: [
      { formatter: 'rownum', title: 'No', width: 70, hozAlign: 'center' },
      { title: '품목명', field: 'itemnm', minWidth: 180, hozAlign: 'left', editor: 'input', cellClick: (e, cell) => {
          if (e.target.tagName !== 'INPUT') {
            openItemHelp(cell.getRow())
          }
        }
      },
      { title: '규격', field: 'spec', width: 100, hozAlign: 'left' },
      { title: '단위', field: 'unit', width: 60, hozAlign: 'center' },
      { title: '수량', field: 'quty', width: 75, hozAlign: 'right', editor: 'input', formatter: 'money', formatterParams: { precision: 0 }, cellEdited: (cell) => calculateRow(cell.getRow()) },
      { title: '단가', field: 'uprc', width: 90, hozAlign: 'right', editor: 'input', formatter: 'money', formatterParams: { precision: 0 }, cellEdited: (cell) => calculateRow(cell.getRow()) },
      { title: '금액', field: 'amtx', width: 100, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 }, cellEdited: (cell) => calculateRow(cell.getRow()) },
      { title: '부가세', field: 'vat', width: 90, hozAlign: 'right', editor: 'input', formatter: 'money', formatterParams: { precision: 0 }, cellEdited: (cell) => calculateRow(cell.getRow()) }
    ]
  })
}

const openCustHelp = (target: 'filter' | 'form') => {
  openHelp('CUST', (d: any) => {
    if (target === 'filter') {
      filter.custcd = d.custcd || ''
      filter.custnm = d.custnm || ''
    } else {
      formMaster.custcd = d.custcd || ''
      formMaster.custnm = d.custnm || ''
      if (d.email) formMaster.email = d.email
      if (d.keymannm || d.custemp) formMaster.custemp = d.keymannm || d.custemp
    }
  }, { gubun: 'C4' })
}

const openItemHelp = (row: any) => {
  activeDetailRow = row
  openHelp('ITEM', (d: any) => {
    if (activeDetailRow) {
      const uprc = Number(d.uprc || d.saleuprc || d.outcost || 0)
      const quty = Number(activeDetailRow.getData().quty) || 1
      const amtx = quty * uprc
      const vat = Math.floor(amtx * 0.1)
      activeDetailRow.update({
        itemcd: d.itemcd || d.codecd || '',
        itemnm: d.itemnm || d.codenm || '',
        spec: d.spec || d.itemspec || '',
        unit: d.unit || d.itemunit || '',
        uprc: uprc,
        amtx: amtx,
        vat: vat
      })
    }
  }, { gubun: 'I1' })
}

const initialize = () => {
  formMaster.qutno = ''
  formMaster.qutdt = new Date().toISOString().split('T')[0]
  formMaster.custcd = ''
  formMaster.custnm = ''
  formMaster.custemp = ''
  formMaster.email = ''
  formMaster.remk = ''
  if (detailTabulator) detailTabulator.clearData()
  vAlert('초기화되었습니다.')
}

const fetchMaster = async () => {
  try {
    const res = await api.get('/api/hsqt/master', { params: filter })
    if (masterTabulator) masterTabulator.setData(res.data || [])
  } catch (err: any) {
    vAlertError(err.response?.data?.message || '마스터 조회 중 오류가 발생했습니다.')
  }
}

const fetchDetail = async (quatym: string, quatno: string) => {
  try {
    const res = await api.get('/api/hsqt/detail', { params: { quatym, quatno, qutno: quatno } })
    if (detailTabulator) detailTabulator.setData(res.data || [])
  } catch (err: any) {
    vAlertError(err.response?.data?.message || '디테일 조회 중 오류가 발생했습니다.')
  }
}

const addDetailRow = () => {
  if (detailTabulator) {
    detailTabulator.addRow({ itemcd: '', itemnm: '', spec: '', unit: '', quty: 1, uprc: 0, amtx: 0, vat: 0 })
  }
}

const saveData = async () => {
  try {
    const details = detailTabulator ? detailTabulator.getData() : []
    const formattedMaster = {
      ...formMaster,
      qutdt: formMaster.qutdt ? formMaster.qutdt.replace(/-/g, '') : ''
    }
    const payload = {
      master: formattedMaster,
      details: details
    }
    await api.post('/api/hsqt/save', payload)
    vAlert('성공적으로 저장되었습니다.')
    fetchMaster()
  } catch (err: any) {
    vAlertError(err.response?.data?.message || '저장 중 오류가 발생했습니다.')
  }
}

const deleteData = async () => {
  if (!formMaster.qutno) {
    vAlert('삭제할 견적을 선택해주세요.')
    return
  }
  try {
    await api.post('/api/hsqt/delete', { qutno: formMaster.qutno })
    vAlert('삭제되었습니다.')
    initialize()
    fetchMaster()
  } catch (err: any) {
    vAlertError(err.response?.data?.message || '삭제 중 오류가 발생했습니다.')
  }
}

const printQuotation = async () => {
  if (!formMaster.qutno) {
    vAlert('출력할 견적서를 선택해주세요.')
    return
  }
  const ioym = formMaster.qutdt ? formMaster.qutdt.replace(/-/g, '').substring(0, 6) : '202610'
  const iono = formMaster.qutno
  try {
    await printSpecification(ioym, iono, detailTabulator ? detailTabulator.getData() : [], formMaster)
  } catch (err: any) {
    vAlertError(err.message || '견적서 출력 중 오류가 발생했습니다.')
  }
}

const sendEmailReport = async () => {
  if (!formMaster.qutno) {
    vAlert('메일로 발송할 견적서를 선택해주세요.')
    return
  }
  if (!formMaster.email) {
    vAlert('수신 메일주소를 입력해주세요.')
    return
  }
  const ioym = formMaster.qutdt ? formMaster.qutdt.replace(/-/g, '').substring(0, 6) : '202610'
  const iono = formMaster.qutno
  try {
    vAlert(`[${formMaster.email}]로 견적서 메일 발송을 준비합니다...`)
    await printSpecification(ioym, iono, detailTabulator ? detailTabulator.getData() : [], formMaster)
    vAlert('견적서 메일 발송창이 호출되었습니다.')
  } catch (err: any) {
    vAlertError(err.message || '메일 발송 중 오류가 발생했습니다.')
  }
}
</script>
