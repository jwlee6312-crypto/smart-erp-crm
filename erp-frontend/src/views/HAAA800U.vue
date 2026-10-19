<!--
	=============================================================
	프로그램명	: 프로그램관리 (HAAA800U)
	작성일자	: 2025.02.24
	설명        : 시스템 프로그램 관리 (HSOD100U 디자인 패턴 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showalert" :error="showerror" :message="alertmessage" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-window-sidebar me-2 text-primary" style="font-size: 18px;"></i>
        시스템 관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">프로그램 관리 (HAAA800U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
        <button v-if="formdata.actkind === 'U0'" class="btn-erp btn-delete" @click="deletedata">삭제</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [상단] 조회 필터 영역 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
                <col style="width: 10%" /><col style="width: 40%" />
                <col style="width: 10%" /><col style="width: 40%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light">업무분류</th>
                <td>
                  <select v-model="searchform.upmucd" class="form-select form-select-sm" @change="fetchsearchgrpcd">
                    <option v-for="opt in upmuoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="text-center bg-light">메뉴그룹</th>
                <td>
                  <select v-model="searchform.grpcd" class="form-select form-select-sm" @change="search">
                    <option value="">전체보기</option>
                    <option v-for="item in searchgrpcdoptions" :key="item.grpcd" :value="item.grpcd">{{ item.grpnm }}</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [중간] 상세 입수정 영역 -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-primary"></i>프로그램 정보 관리</div>
          <div class="d-flex gap-1">
            <div v-if="formdata.actkind === 'U0'" class="badge bg-warning text-dark px-2">수정 중</div>
            <div v-else class="badge bg-primary text-white px-2">신규 등록</div>
          </div>
        </div>
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 80px;" /><col />
              <col style="width: 80px;" /><col />
              <col style="width: 80px;" /><col />
              <col style="width: 80px;" /><col />
              <col style="width: 80px;" /><col style="width: 100px;" />
              <col style="width: 80px;" /><col style="width: 120px;" />
            </colgroup>
            <tbody>
              <tr>
                <th class="required bg-light text-center">ID</th>
                <td><input v-model="formdata.pgmid" type="text" class="form-control form-control-sm fw-bold text-primary text-center" maxlength="20" placeholder="pgmid" :disabled="formdata.actkind === 'U0'"/></td>
                <th class="required bg-light text-center">명칭</th>
                <td><input v-model="formdata.pgmnm" type="text" class="form-control form-control-sm" maxlength="30" /></td>
                <th class="required bg-light text-center">업무</th>
                <td>
                  <select v-model="formdata.upmucd" class="form-select form-select-sm">
                    <option v-for="opt in upmuoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="required bg-light text-center">그룹</th>
                <td>
                  <select v-model="formdata.grpcd" class="form-select form-select-sm">
                    <option value="">-- 선택 --</option>
                    <option v-for="item in grpcdoptions" :key="item.grpcd" :value="item.grpcd">{{ item.grpnm }}</option>
                  </select>
                </td>
                <th class="required bg-light text-center">순서</th>
                <td><input v-model="formdata.dspord" type="text" class="form-control form-control-sm text-end" /></td>
                <th class="bg-light text-center">사용</th>
                <td>
                  <div class="form-check form-switch m-0 d-flex align-items-center justify-content-center h-100">
                    <input v-model="formdata.useyn" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="useyn800">
                    <label class="form-check-label ms-2 small fw-bold" for="useyn800">{{ formdata.useyn === 'Y' ? '사용' : '중지' }}</label>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column grid-container-right">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center flex-shrink-0">
          <i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i>
          <span class="fw-bold small text-dark">프로그램 리스트</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="maingridelement" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authstore = useAuthStore()
const { showAlert: showalert, showError: showerror, alertMessage: alertmessage, vAlert: valert, vAlertError: valerterror } = useAlerts()
const { resetForm: resetform } = useFormReset()

const searchform = reactive({ upmucd: '', grpcd: '' })
const formdata = reactive({
	actkind: 'S0', pgmid: '', pgmnm: '', upmucd: '', grpcd: '', grpnm: '', dspord: '1', useyn: 'Y',
	cmpycd: authstore.cmpycd, userid: authstore.userid
})

const upmuoptions = ref<any[]>([])
const searchgrpcdoptions = ref<any[]>([])
const grpcdoptions = ref<any[]>([])
const maingridelement = ref<HTMLDivElement | null>(null)
let maingrid: Tabulator | null = null

async function fetchsearchgrpcd() {
	if (!searchform.upmucd) { searchgrpcdoptions.value = []; return; }
	try {
		const res = await api.post('/ha00/HA00_00P_STR', { gubun: 'sc', gbncd: searchform.upmucd, cmpycd: authstore.cmpycd })
		const processed = res.data || [];
		searchgrpcdoptions.value = processed.map((n: any) => ({ grpcd: n.codecd || n.grpcd, grpnm: n.codenm || n.grpnm }))
		search()
	} catch (e) { console.error('분류 로드 실패') }
}

watch(() => formdata.upmucd, async (newval) => {
	if (!newval) { grpcdoptions.value = []; return; }
	try {
		const res = await api.post('/ha00/HA00_00P_STR', { gubun: 'sc', gbncd: newval, cmpycd: authstore.cmpycd })
		const processed = res.data || [];
		grpcdoptions.value = processed.map((n: any) => ({ grpcd: n.codecd || n.grpcd, grpnm: n.codenm || n.grpnm }))
	} catch (e) { console.error('분류 로드 실패') }
})

async function fetchupmu() {
	try {
		const res = await api.post('/ha00/HA00_00P_STR', { gubun: 'e0', gbncd: '320', cmpycd: authstore.cmpycd })
		if (res.data) {
			const processed = res.data || [];
			upmuoptions.value = processed.map((n: any) => ({ codecd: n.codecd || n.code, codenm: n.codenm || n.cdnm }))
			if (upmuoptions.value.length > 0) {
				searchform.upmucd = upmuoptions.value[0].codecd;
				formdata.upmucd = upmuoptions.value[0].codecd;
				await fetchsearchgrpcd();
			}
		}
	} catch (e) { console.error('업무코드 로드 실패') }
}

async function search() {
	try {
		const res = await api.post('/haaa/HAAA_800U_STR', { actkind: 'S0', upmucd: searchform.upmucd, grpcd: searchform.grpcd, cmpycd: authstore.cmpycd })
		const processed = res.data || [];
		maingrid?.setData(processed)
		valert('프로그램 리스트를 불러왔습니다.')
	} catch (e) { valerterror('조회 실패') }
}

async function save() {
	if (!formdata.pgmid || !formdata.pgmnm) return valerterror('아이디와 명칭은 필수입니다.')
	if (!confirm('저장하시겠습니까?')) return
	try {
		const act = formdata.actkind === 'S0' ? 'A0' : 'U0';
		const res = await api.post('/haaa/HAAA_800U_STR', { ...formdata, actkind: act })
		const resdata = res.data?.[0] || {};

		if (resdata.result === 'OK' || resdata.res === 'OK') {
			valert('저장이 완료되었습니다.');
			search();
		} else {
			valerterror(resdata.msg || '저장 중 오류가 발생했습니다.');
		}
	} catch (e) { valerterror('저장 중 통신 오류가 발생했습니다.') }
}

async function deletedata() {
	if (!confirm('정말로 삭제하시겠습니까?')) return
	try {
		const res = await api.post('/haaa/HAAA_800U_STR', { ...formdata, actkind: 'D0' })
		const resdata = res.data?.[0] || {};

		if (resdata.result === 'OK' || resdata.res === 'OK') {
			valert('삭제되었습니다.');
			search(); initialize();
		} else {
			valerterror(resdata.msg || '삭제 중 오류가 발생했습니다.');
		}
	} catch (e) { valerterror('삭제 중 통신 오류가 발생했습니다.') }
}

function initialize() {
	resetform(formdata); Object.assign(formdata, { actkind: 'S0', useyn: 'Y', dspord: '1', cmpycd: authstore.cmpycd, userid: authstore.userid });
}

onMounted(async () => {
	if (maingridelement.value) {
		maingrid = new Tabulator(maingridelement.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			placeholder: '데이터가 없습니다.',
			columnDefaults: { headerSort: false, headerHozAlign: 'center', vertAlign: "middle" },
			columns: [
				{ title: '프로그램id', field: 'pgmid', hozAlign: 'center', width: 140, cssClass: 'fw-bold text-primary border-end' },
				{ title: '프로그램 명칭', field: 'pgmnm', minWidth: 250, widthGrow: 1, cssClass: 'fw-bold', hozAlign: 'left' },
				{ title: '업무', field: 'upmunm', hozAlign: 'center', width: 120 },
				{ title: '그룹', field: 'grpnm', hozAlign: 'center', width: 150 },
				{ title: '순서', field: 'dspord', hozAlign: 'center', width: 80 },
				{ title: '사용', field: 'useyn', hozAlign: 'center', width: 80, formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
			]
		})
		maingrid.on('rowClick', (e, row) => {
			const d = row.getData();
			Object.assign(formdata, d);
			formdata.actkind = 'U0'
		})
	}
	await fetchupmu()
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
