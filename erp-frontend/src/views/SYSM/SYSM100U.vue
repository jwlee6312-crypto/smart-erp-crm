<template>
  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 헤더 -->
    <div class="erp-header d-flex justify-content-between align-items-center border-bottom shadow-sm bg-white py-2 sticky-top flex-shrink-0">
      <div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-excel-fill me-2 text-success" style="font-size: 18px;"></i>
        시스템관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        초기화관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">시스템 초기화 및 엑셀 일괄 등록 (SYSM100U)</span>
      </div>
    </div>

    <!-- 💡 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-auto p-4 bg-light">
      <p class="text-muted small mb-4">신규 사이트 구축 시 필요한 기준 정보 및 초기 데이터를 엑셀 파일로 일괄 등록할 수 있습니다.</p>

      <div class="row">
        <div class="col-md-4 mb-3" v-for="target in targets" :key="target.id">
          <div class="card h-100 shadow-sm border-0">
            <div class="card-body d-flex flex-column">
              <div class="d-flex align-items-center mb-3">
                <div class="icon-box bg-light-primary text-primary rounded p-3 me-3">
                  <i :class="target.icon" style="font-size: 1.5rem;"></i>
                </div>
                <div>
                  <h5 class="card-title fw-bold mb-1" style="font-size: 1.1rem;">{{ target.title }}</h5>
                  <span class="badge bg-secondary" style="font-size: 0.75rem;">{{ target.code }}</span>
                </div>
              </div>
              <p class="card-text text-secondary small flex-grow-1">{{ target.desc }}</p>

              <div class="mt-3">
                <button class="btn btn-outline-primary btn-sm w-100 mb-2" @click="downloadTemplate(target.id)">
                  <i class="bi bi-download me-1"></i> 템플릿 다운로드
                </button>

                <div class="input-group input-group-sm">
                  <input type="file" class="form-control" :id="'file_' + target.id" accept=".xlsx, .xls" @change="handleFileChange(target.id, $event)">
                  <button class="btn btn-success" type="button" @click="uploadExcel(target.id)" :disabled="!selectedFiles[target.id] || uploading[target.id]">
                    <span v-if="uploading[target.id]" class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
                    <i v-else class="bi bi-upload me-1"></i> 업로드
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { api } from '@/utils/axios'
import * as XLSX from 'xlsx'

const targets = [
  { id: 'org', code: 'HABA060U', title: '조직도 관리', icon: 'bi bi-diagram-3', desc: '부서코드, 부서명, 상위부서코드 및 부서 체계를 일괄 등록합니다.' },
  { id: 'user', code: 'HABA920U', title: '사용자 마스터 관리', icon: 'bi bi-people', desc: '사원번호, 사용자ID, 성명, 부서, 이메일, 연락처 및 내선번호를 일괄 등록합니다.' },
  { id: 'item', code: 'HSBA010U', title: '품목 등록', icon: 'bi bi-box-seam', desc: '품목코드, 품목명, 규격, 단가 및 자산구분 정보를 일괄 등록합니다.' },
  { id: 'cust', code: 'HSBA070U', title: '거래처 등록', icon: 'bi bi-building', desc: '거래처코드, 명칭, 사업자번호, 대표자, 업태, 종목 및 주소 정보를 일괄 등록합니다.' },
  { id: 'phone', code: 'phone_number_map', title: '통합전화번호부 정보', icon: 'bi bi-journal-text', desc: '통합전화번호부(phone_number_map) 테이블에 연동될 연락처 정보를 일괄 등록합니다.' }
]

const selectedFiles = reactive<{ [key: string]: File | null }>({})
const uploading = reactive<{ [key: string]: boolean }>({})

function handleFileChange(targetId: string, event: Event) {
  const target = event.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    selectedFiles[targetId] = target.files[0]
  } else {
    selectedFiles[targetId] = null
  }
}

function downloadTemplate(type: string) {
  try {
    let headers: string[] = []
    let filenamePrefix = type

    switch (type) {
      case 'org':
        headers = ['부서코드', '부서명', '상위부서코드', '사용여부']
        filenamePrefix = 'org'
        break
      case 'user':
        headers = ['사원번호', '사용자ID', '성명', '부서코드', '이메일', '연락처', '내선번호']
        filenamePrefix = 'user'
        break
      case 'item':
        headers = ['품목코드', '품목명', '규격', '단가', '자산구분(astkind)', '사용여부']
        filenamePrefix = 'item'
        break
      case 'cust':
        headers = ['거래처코드', '사업번호', '거래처명', '대표자명', '업태', '종목', '우편번호', '주소-1', '주소-2', '메일', '핸드폰', 'ERP코드']
        filenamePrefix = 'cust'
        break
      case 'phone':
        headers = ['전화번호(phoneno)', '유형(phonetype)', '거래처코드(custcd)', '대표여부(repyn)', '이름(name)', '사용여부(useyn)']
        filenamePrefix = 'phone'
        break
      default:
        headers = ['코드', '명칭', '비고']
        filenamePrefix = 'template'
    }

    const worksheet = XLSX.utils.aoa_to_sheet([headers])
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Template')

    const now = new Date()
    const pad = (n: number) => String(n).padStart(2, '0')
    const timestamp = `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}_${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}`
    const filename = `${filenamePrefix}_${timestamp}.xlsx`

    XLSX.writeFile(workbook, filename)
  } catch (e: any) {
    alert('템플릿 생성에 실패했습니다: ' + e.message)
  }
}

async function uploadExcel(type: string) {
  const file = selectedFiles[type]
  if (!file) {
    alert('업로드할 엑셀 파일을 선택해주세요.')
    return
  }

  uploading[type] = true
  try {
    const data = await file.arrayBuffer()
    const workbook = XLSX.read(data, { type: 'array' })
    const firstSheetName = workbook.SheetNames[0]
    const worksheet = workbook.Sheets[firstSheetName]
    const rows: any[][] = XLSX.utils.sheet_to_json(worksheet, { header: 1 })

    const payloadRows = rows.slice(1).map((row: any[]) => {
      const getS = (idx: number) => (row[idx] !== undefined && row[idx] !== null && String(row[idx]).trim() !== '' ? String(row[idx]).trim() : '')
      const getN = (idx: number) => {
        const val = Number(row[idx])
        return isNaN(val) ? 0 : val
      }

      switch (type) {
        case 'org':
          return {
            deptcd: getS(0),
            deptnm: getS(1),
            updept: getS(2),
            useyn: getS(3) || 'Y'
          }
        case 'user':
          return {
            empno: getS(0),
            userid: getS(1),
            usernm: getS(2),
            deptcd: getS(3),
            email: getS(4),
            telno: getS(5),
            inner_no: getS(6)
          }
        case 'item':
          return {
            itemcd: getS(0),
            itemnm: getS(1),
            spec: getS(2),
            price: getN(3),
            astkind: getS(4),
            useyn: getS(5) || 'Y'
          }
        case 'cust':
          return {
            custcd: getS(0),
            bizno: getS(1),
            custnm: getS(2),
            ceonm: getS(3),
            bizcond: getS(4),
            item: getS(5),
            postno: getS(6),
            address: getS(7),
            address2: getS(8),
            email: getS(9),
            telno: getS(10),
            outcustcd: getS(11)
          }
        case 'phone':
          return {
            phoneno: getS(0),
            phonetype: getS(1),
            custcd: getS(2),
            repyn: getS(3) || 'N',
            name: getS(4),
            useyn: getS(5) || 'Y'
          }
        default:
          return {}
      }
    }).filter((r: any) => Object.values(r).some(v => v !== ''))

    const res = await api.post(`/sysm/init/json-upload/${type}`, payloadRows)
    alert(res.data.message || '업로드가 성공적으로 완료되었습니다.')
    selectedFiles[type] = null
    const fileInput = document.getElementById('file_' + type) as HTMLInputElement
    if (fileInput) fileInput.value = ''
  } catch (e: any) {
    alert('업로드 실패: ' + (e.response?.data?.message || e.message))
  } finally {
    uploading[type] = false
  }
}
</script>

<style scoped>
.icon-box {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.bg-light-primary {
  background-color: #e7f1ff;
}
</style>
