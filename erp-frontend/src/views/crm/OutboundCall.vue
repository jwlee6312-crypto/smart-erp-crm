<template>
  <div class="container-fluid mt-4 text-start">
    <div class="row g-3">
      <!-- 1. 좌측 영역: 캠페인 및 고객 할당 리스트 -->
      <div class="col-md-4">
        <div class="card shadow-sm mb-3 border-0">
          <div class="card-header bg-dark text-white fw-bold p-3">
            <i class="bi bi-megaphone me-2"></i>아웃바운드 캠페인
          </div>
          <div class="card-body p-3 bg-light">
            <div class="mb-3">
              <label class="form-label small fw-bold">내 내선 정보</label>
              <div class="badge bg-primary d-block p-2">내선: {{ authStore.inner_no || '미지정' }} ({{ authStore.usernm }})</div>
            </div>

            <label class="form-label small fw-bold">캠페인 선택</label>
            <select v-model="selectedCampaign" class="form-select form-select-sm mb-3" @change="loadCustomers">
              <option value="">캠페인을 선택하세요</option>
              <option v-for="cp in campaigns" :key="cp.id" :value="cp.id">{{ cp.title }}</option>
            </select>

            <div class="d-flex justify-content-between align-items-center mb-2">
              <span class="small fw-bold text-muted">할당 고객 리스트</span>
              <div class="btn-group btn-group-sm">
                <button class="btn btn-outline-primary" :class="{active: filter === 'ALL'}" @click="filter = 'ALL'">전체</button>
                <button class="btn btn-outline-warning" :class="{active: filter === 'RESERVED'}" @click="filter = 'RESERVED'">예약</button>
                <button class="btn btn-outline-secondary" :class="{active: filter === 'PENDING'}" @click="filter = 'PENDING'">보류</button>
              </div>
            </div>

            <div class="list-group list-group-flush border rounded overflow-auto" style="max-height: calc(100vh - 350px);">
              <button v-for="cust in filteredCustomerList" :key="cust.id"
                class="list-group-item list-group-item-action p-3"
                :class="{ 'active': currentCust?.id === cust.id, 'border-start border-4 border-warning': cust.status === 'RESERVED' }"
                @click="selectCustomer(cust)">
                <div class="d-flex justify-content-between align-items-center">
                  <strong class="text-truncate">{{ cust.custnm }}</strong>
                  <span class="badge rounded-pill" :class="statusClass(cust.status)">{{ cust.statusnm }}</span>
                </div>
                <div class="d-flex justify-content-between mt-1 small">
                  <span class="text-muted">{{ cust.hpno }}</span>
                  <span v-if="cust.reserved_at" class="text-danger fw-bold"><i class="bi bi-clock me-1"></i>{{ cust.reserved_at }}</span>
                </div>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 2. 우측 영역: 상담 진행 (스크립트 및 결과 등록) -->
      <div class="col-md-8">
        <div v-if="currentCust" class="card shadow-sm h-100 border-0">
          <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center p-3">
            <h5 class="m-0"><i class="bi bi-telephone-outbound me-2"></i>{{ currentCust.custnm }}님 상담</h5>
            <div class="d-flex gap-2">
              <button class="btn btn-light text-primary fw-bold px-4" @click="makeCall(currentCust.hpno)" :disabled="ctiStore.isTalking || isCalling">
                <span v-if="isCalling" class="spinner-border spinner-border-sm me-2"></span>
                <i v-else class="bi bi-telephone-fill me-2"></i>
                {{ isCalling ? '연결 중...' : '전화걸기' }}
              </button>
              <button class="btn btn-danger fw-bold px-4 shadow-sm" @click="handleHangup" v-if="ctiStore.isTalking">
                전화끊기
              </button>
            </div>
          </div>

          <div class="card-body p-0 d-flex flex-column" style="min-height: 700px;">
            <div class="p-4 bg-info-subtle border-bottom">
              <h6 class="fw-bold text-primary mb-3"><i class="bi bi-file-earmark-text me-2"></i>판매 전략 스크립트</h6>
              <div class="p-3 bg-white rounded border shadow-sm script-box">
                {{ campaignScript }}
              </div>
            </div>

            <div class="p-4 flex-grow-1 bg-white">
              <!-- 💡 녹취 상태 표시 영역 (재추가됨) -->
              <div class="mb-4" v-if="ctiStore.recordingFile || ctiStore.isTalking">
                <div class="alert p-2 d-flex align-items-center shadow-sm border mb-0"
                     :class="ctiStore.isTalking ? 'alert-danger border-danger-subtle bg-danger-subtle' : 'alert-success border-success-subtle bg-success-subtle'">
                  <template v-if="ctiStore.isTalking">
                    <div class="spinner-grow spinner-grow-sm text-danger me-2"></div>
                    <span class="small fw-bold text-danger">실시간 아웃바운드 녹취 진행 중...</span>
                  </template>
                  <template v-else-if="ctiStore.recordingFile">
                    <i class="bi bi-check-circle-fill me-2 text-success"></i>
                    <span class="small fw-bold text-dark">아웃바운드 녹취 완료: </span>
                    <span class="small fw-bold text-primary ms-2">{{ ctiStore.recordingFile }}</span>
                  </template>
                </div>
              </div>

              <h6 class="fw-bold text-dark mb-3"><i class="bi bi-pencil-square me-2"></i>통화 결과 등록</h6>
              <div class="row g-3">
                <div class="col-md-4">
                  <label class="form-label small fw-bold text-muted">최종 결과</label>
                  <select v-model="callResult.status" class="form-select shadow-sm">
                    <option value="SUCCESS">판매완료 (성공)</option>
                    <option value="RESERVED">전화예약 (재통화)</option>
                    <option value="PENDING">전화보류 (고민중)</option>
                    <option value="REJECT">거절 (실패)</option>
                  </select>
                </div>
                <div class="col-md-8" v-if="callResult.status === 'RESERVED'">
                  <label class="form-label small fw-bold text-danger">재통화 예약 시간</label>
                  <input type="datetime-local" v-model="callResult.reserved_at" class="form-control" />
                </div>
                <div class="col-12">
                  <label class="form-label small fw-bold text-muted">상담 상세 메모</label>
                  <textarea class="form-control shadow-sm" rows="6" v-model="callResult.memo" placeholder="상담 내용을 입력하세요."></textarea>
                </div>
                <div class="col-12 text-end mt-4">
                  <button class="btn btn-lg btn-success fw-bold px-5 shadow" @click="saveResult">
                    <i class="bi bi-cloud-check me-2"></i>결과 저장 및 다음 고객
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="card shadow-sm h-100 d-flex justify-content-center align-items-center bg-light border-0">
          <div class="text-center text-muted p-5">
            <i class="bi bi-person-lines-fill display-1 mb-4 opacity-25"></i>
            <h4>고객을 선택하여 상담을 시작하세요.</h4>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useCtiStore } from '@/stores/ctiStore'
import { useAuthStore } from '@/stores/authStore'
import { api } from '@/utils/axios'

const ctiStore = useCtiStore()
const authStore = useAuthStore()

const isCalling = ref(false)
const selectedCampaign = ref('')
const filter = ref('ALL')
const customerList = ref<any[]>([])
const currentCust = ref<any>(null)
const callResult = ref({ status: 'SUCCESS', memo: '', reserved_at: '' })

const campaigns = ref([
  { id: 'C001', title: '신규 기가광랜 무상교체 이벤트', script: '[인사] 안녕하세요 고객님! SmartCore 아웃바운드 팀입니다.' }
])

const loadCustomers = () => {
  customerList.value = [
    { id: 1, custnm: '테스트고객(102)', hpno: '102', status: 'INIT', statusnm: '미접촉' }
  ]
}

const filteredCustomerList = computed(() => customerList.value)
const campaignScript = computed(() => campaigns.value[0].script)

const makeCall = async (num: string) => {
  const myExten = authStore.inner_no;
  if (!myExten) return alert('내선 정보가 없습니다.');

  // 💡 새 전화 시 녹취 파일 정보 초기화
  ctiStore.recordingFile = '';
  isCalling.value = true;

  try {
    await api.get('/crm/cti/make-call', {
      params: { exten: myExten, dest: num.replace(/-/g, ''), context: 'outbound-call' }
    });
  } catch (e) {
    isCalling.value = false;
    alert('발신 실패');
  }
}

// 💡 통화 연결 감지 시 로딩 상태 해제
watch(() => ctiStore.isTalking, (talking) => {
  if (talking) isCalling.value = false;
})

const handleHangup = async () => {
  isCalling.value = false;
  const myExten = authStore.inner_no;
  if (!myExten) return;
  try { await api.get(`/crm/cti/hangup?exten=${myExten}`) } catch (e) {}
}

const selectCustomer = (cust: any) => {
  currentCust.value = cust;
  callResult.value = { status: 'SUCCESS', memo: '', reserved_at: '' }
}

const statusClass = (status: string) => status === 'INIT' ? 'bg-primary' : 'bg-success'

const saveResult = async () => {
  alert(`${currentCust.value.custnm}님 결과 저장 완료. (녹취파일명: ${ctiStore.recordingFile})`);
  currentCust.value = null;
}
</script>

<style scoped>
.script-box { white-space: pre-wrap; min-height: 120px; line-height: 1.6; font-size: 1.05rem; }
.list-group-item.active { background-color: #e9ecef; border-color: #dee2e6; color: #000; }
</style>
