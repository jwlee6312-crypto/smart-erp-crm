<!--
	=============================================================
	프로그램명	: 회의록 관리 (HMET100U)
	작성일자	: 2026.10.26
	작성자      : AI Assistant
	설명        : 녹음파일 첨부 -> AI STT 변환 -> 회의록 조회 및 검토 -> 저장 처리 워크플로우 지원 회의록 관리 화면
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 상단 글로벌 액션 바 (HSOD100U 표준) -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-journal-text me-2 text-primary" style="font-size: 18px;"></i>
        협업관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">회의록 관리 (HMET100U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="fetchMaster">조회</button>
        <button class="btn-erp btn-save" @click="saveData">저장</button>
        <button class="btn-erp btn-delete" @click="deleteData">삭제</button>
        <button class="btn-erp btn-print" @click="runSttSimulation"><i class="bi bi-mic-fill me-1"></i>AI STT 변환</button>
        <button class="btn-erp btn-print" @click="runSpellCheck"><i class="bi bi-check2-all me-1"></i>AI 맞춤법 교정</button>
      </div>
    </div>

    <!-- 💡 검색 필터 -->
    <div class="p-2 bg-light border-bottom flex-shrink-0">
      <div class="card border shadow-sm">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 10%;" /><col style="width: 25%;" />
              <col style="width: 10%;" /><col style="width: 55%;" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light fw-bold">회의일자</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <input type="date" v-model="filter.frdt" class="form-control form-control-sm" />
                  <span>~</span>
                  <input type="date" v-model="filter.todt" class="form-control form-control-sm" />
                </td>
                <th class="text-center bg-light border-start fw-bold">회의제목/안건</th>
                <td>
                  <input v-model="filter.title" class="form-control form-control-sm" placeholder="회의 제목 또는 안건 검색" @keyup.enter="fetchMaster" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 📑 메인 50:50 분할 레이아웃 영역 -->
    <div class="flex-grow-1 p-2 bg-light overflow-hidden d-flex flex-row gap-2">
      <!-- (Left 50%) 회의 마스터 리스트 -->
      <div class="card border shadow-sm overflow-hidden d-flex flex-column" style="flex: 0 0 50%; width: 50%;">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
          <span class="small fw-bold text-dark"><i class="bi bi-list-ul me-1 text-primary"></i> 회의록 마스터 목록</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="masterGridRef" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>

      <!-- (Right 50%) 회의 상세 정보 및 녹취 파일 첨부 -> STT 변환 -> 회의록 조회/검토 -> 저장 워크플로우 폼 -->
      <div class="card border shadow-sm overflow-hidden d-flex flex-column bg-white" style="flex: 0 0 50%; width: 50%;">
        <div class="card-header bg-white border-bottom py-1 px-3 d-flex justify-content-between align-items-center">
          <span class="small fw-bold text-primary"><i class="bi bi-pencil-square me-2"></i>녹취 첨부 및 회의록 상세 검토</span>
        </div>
        <div class="card-body p-3 overflow-auto scrollbar-sm d-flex flex-column gap-3">
          <table class="erp-table-dense w-100">
            <colgroup><col style="width: 130px;" /><col /></colgroup>
            <tbody>
              <tr>
                <th class="bg-light">회의번호</th>
                <td><input v-model="formMaster.meetno" class="form-control form-control-sm bg-light" readonly placeholder="자동생성" /></td>
              </tr>
              <tr>
                <th class="bg-light">녹음파일 첨부</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input type="file" class="form-control form-control-sm" @change="onFileSelected" accept="audio/*" />
                    <button class="btn btn-outline-primary" type="button" @click="uploadAudioFile">업로드 & 변환</button>
                  </div>
                  <div class="x-small text-muted mt-1" v-if="uploadedFileName">첨부된 파일: {{ uploadedFileName }}</div>
                </td>
              </tr>
              <tr>
                <th class="bg-light">회의일자</th>
                <td><input type="date" v-model="formMaster.meetdt" class="form-control form-control-sm w-50" /></td>
              </tr>
              <tr>
                <th class="bg-light">회의제목</th>
                <td><input v-model="formMaster.title" class="form-control form-control-sm" placeholder="회의 제목 입력" /></td>
              </tr>
              <tr>
                <th class="bg-light">회의장소</th>
                <td><input v-model="formMaster.location" class="form-control form-control-sm w-75" placeholder="회의 장소 입력" /></td>
              </tr>
              <tr>
                <th class="bg-light">참석자</th>
                <td><input v-model="formMaster.attendees" class="form-control form-control-sm" placeholder="참석자 명단 입력 (예: 홍길동, 김철수)" /></td>
              </tr>

              <tr>
                <th class="bg-light">AI 자동 요약</th>
                <td><textarea v-model="formMaster.aiSummary" class="form-control form-control-sm bg-light text-primary fw-bold" rows="3" style="resize: vertical;" placeholder="AI가 요약한 핵심 회의록 (검토 및 수정 가능)..."></textarea></td>
              </tr>
            </tbody>
          </table>

          <!-- 녹취록 STT 내용을 화면 전체에 맞게 최대한 늘림 -->
          <div class="flex-grow-1 d-flex flex-column" style="min-height: 250px;">
            <div class="small fw-bold text-dark mb-1 d-flex justify-content-between align-items-center">
              <span><i class="bi bi-mic me-1 text-primary"></i> 녹취록 STT 내용 (상세 전체 화면)</span>
            </div>
            <textarea v-model="formMaster.sttText" class="form-control form-control-sm flex-grow-1 font-monospace" style="resize: vertical; min-height: 220px;" placeholder="녹취 파일 업로드 후 [AI STT 변환]을 실행하면 STT 전체 텍스트가 여기에 채워집니다..."></textarea>
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

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

const filter = reactive({
  frdt: '',
  todt: '',
  title: ''
})

const formMaster = reactive({
  meetno: '',
  meetdt: new Date().toISOString().split('T')[0],
  title: '',
  location: '',
  attendees: '',
  sttText: '',
  aiSummary: ''
})

const uploadedFileName = ref('')
const selectedFile = ref<File | null>(null)

const masterGridRef = ref<HTMLDivElement | null>(null)
let masterTabulator: Tabulator | null = null

onMounted(() => {
  const today = new Date()
  const firstDay = new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0]
  const lastDay = today.toISOString().split('T')[0]
  filter.frdt = firstDay
  filter.todt = lastDay

  initMasterTable()
  fetchMaster()
})

const initMasterTable = () => {
  if (!masterGridRef.value) return
  masterTabulator = new Tabulator(masterGridRef.value, {
    layout: 'fitColumns',
    height: '100%',
    selectable: 1,
    placeholder: '조회된 회의록이 없습니다.',
    columns: [
      { formatter: 'rownum', title: 'No', width: 60, hozAlign: 'center' },
      { title: '회의번호', field: 'meetno', width: 110, hozAlign: 'center' },
      { title: '회의일자', field: 'meetdt', width: 110, hozAlign: 'center' },
      { title: '회의제목', field: 'title', minWidth: 180, hozAlign: 'left' },
      { title: '장소', field: 'location', width: 120, hozAlign: 'center' }
    ]
  })

  masterTabulator.on('rowClick', (e, row) => {
    const data = row.getData()
    Object.assign(formMaster, data)
  })
}

const initialize = () => {
  formMaster.meetno = ''
  formMaster.meetdt = new Date().toISOString().split('T')[0]
  formMaster.title = ''
  formMaster.location = ''
  formMaster.attendees = ''
  formMaster.sttText = ''
  formMaster.aiSummary = ''
  uploadedFileName.value = ''
  selectedFile.value = null
  vAlert('초기화되었습니다.')
}

const fetchMaster = async () => {
  try {
    const res = await api.get('/api/hmet/master', { params: filter })
    if (masterTabulator) masterTabulator.setData(res.data || [])
  } catch (err: any) {
    vAlertError(err.response?.data?.message || '회의록 조회 중 오류가 발생했습니다.')
  }
}

const onFileSelected = (e: any) => {
  const files = e.target.files
  if (files && files.length > 0) {
    selectedFile.value = files[0]
    uploadedFileName.value = files[0].name
  }
}

const uploadAudioFile = async () => {
  if (!selectedFile.value) {
    return vAlertError('업로드할 녹음 파일을 먼저 선택해주세요.')
  }
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    await api.post('/comm/upload/company', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    vAlert('녹음 파일이 서버에 성공적으로 업로드되었습니다. AI STT 변환을 진행합니다...')
    if (!formMaster.title) {
      formMaster.title = uploadedFileName.value.replace(/\.[^/.]+$/, '')
    }
    runSttSimulation()
  } catch (err: any) {
    vAlertError(err.response?.data?.message || '파일 업로드 중 오류가 발생했습니다.')
  }
}

const runSpellCheck = async () => {
  if (!formMaster.sttText) {
    return vAlertError('교정할 STT 텍스트 내용이 없습니다.')
  }
  try {
    vAlert('AI가 녹취록 STT 텍스트의 맞춤법을 검사하고 보정 중입니다...')
    const res = await api.post('/api/hmet/stt-correct', { stt_text: formMaster.sttText })
    if (res.data && res.data.success) {
      formMaster.sttText = res.data.corrected_text
      vAlert('AI 맞춤법 교정 및 보정이 완료되었습니다.')
    }
  } catch (err: any) {
    vAlertError(err.message || '맞춤법 교정 중 오류가 발생했습니다.')
  }
}

const runSttSimulation = async () => {
  try {
    vAlert('AI STT 녹취 변환 및 자동 요약을 수행 중입니다...')
    const res = await api.post('/api/hmet/stt-simulate', { filename: uploadedFileName.value || formMaster.title || '회의녹음.mp3' })
    if (res.data && res.data.success) {
      formMaster.sttText = res.data.stt_text
      formMaster.aiSummary = res.data.summary
      vAlert('AI STT 변환 및 회의록 요약이 완료되었습니다. 내용을 검토 후 [저장]을 눌러주세요.')
    }
  } catch (err: any) {
    vAlertError(err.message || 'STT 변환 중 오류가 발생했습니다.')
  }
}

const saveData = async () => {
  try {
    const payload = {
      master: formMaster
    }
    await api.post('/api/hmet/save', payload)
    vAlert('회의록이 성공적으로 저장되었습니다.')
    fetchMaster()
  } catch (err: any) {
    vAlertError(err.response?.data?.message || '저장 중 오류가 발생했습니다.')
  }
}

const deleteData = async () => {
  if (!formMaster.meetno) {
    vAlert('삭제할 회의록을 선택해주세요.')
    return
  }
  try {
    await api.post('/api/hmet/delete', { meetno: formMaster.meetno })
    vAlert('삭제되었습니다.')
    initialize()
    fetchMaster()
  } catch (err: any) {
    vAlertError(err.response?.data?.message || '삭제 중 오류가 발생했습니다.')
  }
}

const exportExcel = () => {
  if (masterTabulator) {
    masterTabulator.download('xlsx', 'meeting_minutes.xlsx', { sheetName: '회의록관리' })
  }
}
</script>
