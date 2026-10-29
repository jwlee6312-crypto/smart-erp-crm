<!--
	=============================================================
	프로그램명	: 사용자 그룹 프로그램 권한관리 (HABA935U)
	작성일자	: 2025.03.14
	설명        : 사용자 그룹별 프로그램 사용 및 즐겨찾기 권한 관리 (HSOD100U 디자인 표준 준수)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-shield-lock-fill me-2 text-primary" style="font-size: 18px;"></i>
        시스템 관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">사용자 그룹 권한관리 (HABA935U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-search" @click="fetchCategories">조회</button>
        <button class="btn-erp btn-save" @click="saveAll">일괄 저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [상단] 조회 필터 영역 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
                <col style="width: 10%" /><col style="width: 25%" />
                <col style="width: 10%" /><col style="width: 25%" />
                <col style="width: 30%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light">사용자그룹</th>
                <td>
                  <select v-model="form_01.usergrp" class="form-select form-select-sm">
                    <option v-for="opt in userGrpData" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="text-center bg-light">업무분류</th>
                <td>
                  <select v-model="form_01.upmucd" class="form-select form-select-sm" @change="fetchCategories">
                    <option v-for="opt in upmuData" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <td class="small text-muted ps-3">
                  <i class="bi bi-info-circle me-1"></i> 분류 선택 시 우측에 리스트가 표시됩니다.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 투-그리드 레이아웃 영역 -->
      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">

        <!-- ⬅️ 좌측: 메뉴 그룹 분류 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden" style="width: 350px; min-width: 350px;">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
            <i class="bi bi-list-task me-2 text-secondary"></i>
            <span class="fw-bold small text-dark">메뉴 그룹 분류</span>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="tableRef1" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <!-- ➡️ 우측: 상세 프로그램 권한 설정 -->
        <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between flex-shrink-0">
            <div class="fw-bold small text-dark">
              <i class="bi bi-check2-circle me-2 text-primary"></i> 상세 프로그램 권한 설정
              <span v-if="selectedGroupName" class="badge bg-primary-subtle text-primary border border-primary-subtle ms-2">{{ selectedGroupName }}</span>
            </div>
            <button class="btn btn-sm btn-outline-primary py-0 px-2 fw-bold" @click="toggleSelectAll" style="font-size: 11px;">전체선택 / 해제</button>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="tableRef2" class="tabulator-instance flex-grow-1"></div>
          </div>
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
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { fetchSelectData } from '@/composables/useFetchSelectData'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

// 1. 상태 관리
const form_01 = reactive({ usergrp: '', upmucd: '', grpcd: '' })
const userGrpData = ref<any[]>([])
const upmuData = ref<any[]>([])
const selectedGroupName = ref('')

const tableRef1 = ref<HTMLElement | null>(null)
const tableRef2 = ref<HTMLElement | null>(null)
let grid1: Tabulator | null = null
let grid2: Tabulator | null = null

// 2. 기초 데이터 로드
async function fetchGrp() {
  try {
    const res = await api.post('/hs00/HS00_000S_STR', { cmpycd: authStore.cmpycd , gbncd: '600' })
    if (res.data) {
      userGrpData.value = res.data.map((i: any) => ({
        codecd: (i.code || i.codecd || '').toString().trim(),
        codenm: (i.cdnm || i.codenm || '').toString().trim()
      }))
      if (userGrpData.value.length > 0) {
        form_01.usergrp = userGrpData.value[0].codecd;
        await fetchUpmu();
      }
    }
  } catch (e) { console.error('사용자그룹 로드 실패') }
}

async function fetchUpmu() {
  try {
    const res = await api.get('/crm/outbound/code', { params: { cdtype: '320' } })
    if (res.data) {
      upmuData.value = res.data.map((i: any) => ({
        codecd: (i.codecd || i.code || '').toString().trim(),
        codenm: (i.codenm || i.cdnm || '').toString().trim()
      }))
      if (upmuData.value.length > 0) {
        form_01.upmucd = upmuData.value[0].codecd;
        await fetchCategories();
      }
    }
  } catch (e) { console.error('업무코드 로드 실패') }
}

// 3. 그리드 데이터 조회
async function fetchCategories() {
  if (!form_01.usergrp || !form_01.upmucd) return
  try {
    const res = await api.post('/haba/HABA_935U_STR', { actkind: 'S1', usergrp: form_01.usergrp, upmucd: form_01.upmucd, cmpycd: authStore.cmpycd })
    grid1?.setData(res.data || [])
    grid2?.clearData()
    selectedGroupName.value = ''
  } catch (e) { vAlertError('분류 조회 실패') }
}

async function fetchPermissions(grpcd: string, grpnm: string) {
  form_01.grpcd = grpcd
  selectedGroupName.value = grpnm
  try {
    const res = await api.post('/haba/HABA_935U_STR', { actkind: 'S0', usergrp: form_01.usergrp, upmucd: form_01.upmucd, grpcd: grpcd, cmpycd: authStore.cmpycd })
    grid2?.setData(res.data || [])
  } catch (e) { vAlertError('권한 로드 실패') }
}

// 4. 저장 및 기능
async function saveAll() {
  const rows = grid2?.getData()
  if (!rows || rows.length === 0) return vAlertError('저장할 데이터가 없습니다.')
  if (!confirm('설정된 권한을 일괄 저장하시겠습니까?')) return
  try {
    for (const row of rows) {
      try {
        await api.post('/haba/HABA_935U_STR', {
          actkind: 'U0',
          cmpycd: authStore.cmpycd,
          usergrp: form_01.usergrp,
          upmucd: form_01.upmucd,
          grpcd: form_01.grpcd,
          pgmid: row.pgmid,
          pgmnm: row.pgmnm,
          useyn: String(row.useyn || 'N').trim().toUpperCase(),
          mypgm: String(row.mypgm || 'N').trim().toUpperCase(),
          dspord: Number(row.dspord || 0),
          userid: authStore.userid
        })
      } catch (err: any) {
        // 💡 백엔드 컨트롤러 변경 없이 프론트엔드 레벨 예외 보완
        const msg = err.response?.data?.message || err.message || ''
        if (!msg.includes('마스터 처리 결과가 없습니다')) {
          throw err
        }
      }
    }
    vAlert('성공적으로 저장되었습니다.')
    fetchPermissions(form_01.grpcd, selectedGroupName.value)
  } catch (e: any) {
    console.error(e)
    vAlertError(e.message || '저장 중 오류가 발생했습니다.')
  }
}

const toggleSelectAll = () => {
  const rows = grid2?.getRows(); if (!rows) return
  const allChecked = rows.every(r => {
    const d = r.getData()
    return String(d.useyn || 'N').trim().toUpperCase() === 'Y'
  })
  rows.forEach(r => r.update({ useyn: allChecked ? 'N' : 'Y' }))
}

function initialize() {
  form_01.grpcd = ''
  selectedGroupName.value = ''
  grid1?.clearData()
  grid2?.clearData()
  fetchGrp()
}

// 5. 그리드 초기화
const initGrids = () => {
  if (tableRef1.value) {
    grid1 = new Tabulator(tableRef1.value, {
      layout: 'fitColumns', height: '100%', selectable: 1,
      placeholder: '데이터 없음',
      columnDefaults: { headerSort: false, headerHozAlign: 'center', vertAlign: 'middle' },
      columns: [
        { title: '코드', field: 'grpcd', width: 70, hozAlign: 'center' },
        { title: '분류 명칭', field: 'grpnm', widthGrow: 1, cssClass: 'fw-bold text-primary', hozAlign: 'left' }
      ]
    })
    grid1.on('rowClick', (e, row) => {
      const d = row.getData();
      fetchPermissions(d.grpcd, d.grpnm)
    })
  }

  if (tableRef2.value) {
    grid2 = new Tabulator(tableRef2.value, {
      layout: 'fitColumns', height: '100%',
      placeholder: '분류를 선택하세요',
      columnDefaults: { headerSort: false, headerHozAlign: 'center', vertAlign: 'middle' },
      columns: [
        { title: '프로그램id', field: 'pgmid', width: 130, hozAlign: 'center', cssClass: 'fw-bold border-end' },
        { title: '프로그램 명칭', field: 'pgmnm', minWidth: 200, widthGrow: 1, cssClass: 'fw-bold', hozAlign: 'left' },
        {
          title: '사용', field: 'useyn', hozAlign: 'center', width: 80,
          formatter: (c) => {
            const val = String(c.getValue() || 'N').toUpperCase();
            return val === 'Y' ? '<span class="text-primary fw-bold">O</span>' : '<span class="text-danger fw-bold">X</span>';
          },
          cellClick: (e, cell) => {
            const current = String(cell.getValue() || 'N').toUpperCase();
            cell.setValue(current === 'Y' ? 'N' : 'Y');
          }
        },
        {
          title: '즐겨찾기', field: 'mypgm', hozAlign: 'center', width: 90,
          formatter: (c) => {
            const val = String(c.getValue() || 'N').toUpperCase();
            return val === 'Y' ? '<span class="text-warning">★</span>' : '<span class="text-secondary">☆</span>';
          },
          cellClick: (e, cell) => {
            const current = String(cell.getValue() || 'N').toUpperCase();
            cell.setValue(current === 'Y' ? 'N' : 'Y');
          }
        }
      ]
    })
  }
}

onMounted(async () => {
  await nextTick();
  initGrids();
  await fetchGrp();
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
:deep(.tabulator-row-selected) { background-color: #e7f1ff !important; }
</style>
