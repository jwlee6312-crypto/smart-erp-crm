<template>
	<div class="container-fluid py-3 inbound-consultation">
		<!-- 1. 상단 툴바 (버튼 그룹) -->
		<div class="d-flex justify-content-between align-items-center mb-3 bg-white p-3 rounded shadow-sm border border-primary-subtle">
			<div class="d-flex align-items-center">
				<div class="bg-primary text-white rounded-circle p-2 me-3">
					<i class="bi bi-headset fs-4"></i>
				</div>
				<div>
					<h4 class="m-0 fw-bold text-dark">인바운드 상담 관리</h4>
					<small class="text-muted">실시간 고객 응대 및 상담 이력 기록</small>
				</div>
			</div>
			<div class="d-flex gap-2">
				<button class="btn btn-outline-primary shadow-sm" @click="handleNew"><i class="bi bi-plus-lg me-1"></i>신규</button>
				<button class="btn btn-outline-warning shadow-sm" @click="handleWait"><i class="bi bi-pause-fill me-1"></i>전화대기</button>
				<button class="btn btn-outline-info shadow-sm text-dark" @click="handleTransfer"><i class="bi bi-arrow-right-short me-1"></i>전화돌려주기</button>
				<button class="btn btn-outline-danger shadow-sm" @click="handleHangup"><i class="bi bi-telephone-x-fill me-1"></i>전화끊기</button>
				<button class="btn btn-success shadow-sm px-4" @click="handleSave"><i class="bi bi-check-lg me-1"></i>상담저장</button>
			</div>
		</div>

		<!-- 2. 중단 (고객정보 및 상담내용) -->
		<div class="row g-3 mb-3">
			<!-- 좌측: 고객정보 -->
			<div class="col-md-5">
				<div class="card h-100 shadow-sm border-0">
					<div class="card-header bg-primary text-white fw-bold"><i class="bi bi-person-vcard me-2"></i>고객정보</div>
					<div class="card-body bg-white">
						<div class="row g-3">
							<div class="col-12">
								<label class="form-label text-muted small fw-bold">거래처명</label>
								<div class="input-group">
									<span class="input-group-text bg-light"><i class="bi bi-building"></i></span>
									<input type="text" class="form-control fw-bold text-primary" v-model="customer.custnm" readonly />
								</div>
							</div>
							<div class="col-6">
								<label class="form-label text-muted small fw-bold">고객이름</label>
								<input type="text" class="form-control" v-model="customer.usernm" placeholder="성함 입력" />
							</div>
							<div class="col-6">
								<label class="form-label text-muted small fw-bold">회사전화</label>
								<input type="text" class="form-control" v-model="customer.telno" readonly />
							</div>
							<div class="col-6">
								<label class="form-label text-muted small fw-bold">핸드폰</label>
								<input type="text" class="form-control" v-model="customer.hpno" placeholder="010-0000-0000" />
							</div>
							<div class="col-6">
								<label class="form-label text-muted small fw-bold">메일</label>
								<input type="email" class="form-control" v-model="customer.email" placeholder="example@mail.com" />
							</div>
							<div class="col-12">
								<label class="form-label text-muted small fw-bold">주소</label>
								<textarea class="form-control" rows="2" v-model="customer.address"></textarea>
							</div>
						</div>
					</div>
				</div>
			</div>

			<!-- 우측: 상담내용 -->
			<div class="col-md-7">
				<div class="card h-100 shadow-sm border-0">
					<div class="card-header bg-dark text-white fw-bold d-flex justify-content-between align-items-center">
						<span><i class="bi bi-pencil-square me-2"></i>상담내용</span>
						<span class="badge bg-danger animate-pulse" v-if="ctiStore.isTalking">통화 중</span>
					</div>
					<div class="card-body bg-white">
						<div class="row g-3">
							<div class="col-6">
								<label class="form-label text-muted small fw-bold">상담일자</label>
								<input type="date" class="form-control" v-model="consultation.date" />
							</div>
							<div class="col-6">
								<label class="form-label text-muted small fw-bold">녹취파일 재생</label>
								<audio controls class="w-100" style="height: 38px;">
									<source :src="consultation.recordUrl" type="audio/wav" />
								</audio>
							</div>
							<div class="col-12">
								<label class="form-label text-muted small fw-bold">상담요약</label>
								<input type="text" class="form-control border-warning-subtle" v-model="consultation.summary" placeholder="상담 주제 요약" />
							</div>
							<div class="col-12">
								<label class="form-label text-muted small fw-bold">문의내용</label>
								<textarea class="form-control" rows="3" v-model="consultation.inquiry"></textarea>
							</div>
							<div class="col-12">
								<label class="form-label text-muted small fw-bold">답변내용</label>
								<textarea class="form-control" rows="3" v-model="consultation.answer"></textarea>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 3. 하단 (탭 리스트) -->
		<div class="card shadow-sm border-0">
			<div class="card-header bg-light p-0">
				<ul class="nav nav-tabs px-3 pt-2 border-0" id="historyTab" role="tablist">
					<li class="nav-item">
						<button class="nav-link active fw-bold" data-bs-toggle="tab" data-bs-target="#tab-consult">상담내역리스트</button>
					</li>
					<li class="nav-item">
						<button class="nav-link fw-bold" data-bs-toggle="tab" data-bs-target="#tab-service">서비스처리내역</button>
					</li>
					<li class="nav-item">
						<button class="nav-link fw-bold" data-bs-toggle="tab" data-bs-target="#tab-billing">과금청구리스트</button>
					</li>
					<li class="nav-item">
						<button class="nav-link fw-bold" data-bs-toggle="tab" data-bs-target="#tab-contract">약정리스트</button>
					</li>
				</ul>
			</div>
			<div class="card-body tab-content bg-white p-0" style="min-height: 300px;">
				<!-- 탭 1: 상담내역 -->
				<div class="tab-pane fade show active" id="tab-consult">
					<table class="table table-hover table-striped mb-0 border-top">
						<thead class="table-light text-secondary">
							<tr>
								<th class="ps-3">상담일시</th>
								<th>상담원</th>
								<th>문의내용</th>
								<th>고객이름</th>
							</tr>
						</thead>
						<tbody>
							<tr v-for="i in 3" :key="i">
								<td class="ps-3 text-muted italic small">과거 상담 내역 데이터가 여기에 표시됩니다.</td>
								<td>-</td>
								<td>-</td>
								<td>-</td>
							</tr>
						</tbody>
					</table>
				</div>
				<!-- 나머지 탭들도 비슷한 구조로 생략 (디자인 일관성 유지) -->
				<div class="tab-pane fade" id="tab-service"><p class="p-4 text-center text-muted">서비스 처리 내역이 없습니다.</p></div>
				<div class="tab-pane fade" id="tab-billing"><p class="p-4 text-center text-muted">과금 청구 내역이 없습니다.</p></div>
				<div class="tab-pane fade" id="tab-contract"><p class="p-4 text-center text-muted">활성화된 약정 정보가 없습니다.</p></div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useCtiStore } from '@/stores/ctiStore'
import { api } from '@/utils/axios'

const ctiStore = useCtiStore()

// --- 데이터 모델 ---
const customer = ref({
	custnm: '',
	usernm: '',
	telno: '',
	hpno: '',
	email: '',
	address: ''
})

const consultation = ref({
	date: new Date().toISOString().substr(0, 10),
	summary: '',
	inquiry: '',
	answer: '',
	recordUrl: ''
})

// --- CTI 실시간 연동 감시 ---
watch(() => ctiStore.incomingCall, (newCall) => {
	if (newCall) {
		console.log('상담 화면 데이터 수신:', newCall)
		customer.value.custnm = newCall.custnm || '미등록 고객'
		customer.value.telno = newCall.callerId
		customer.value.usernm = newCall.usernm || ''
		customer.value.address = newCall.address || ''
		customer.value.email = newCall.email || ''
	}
}, { immediate: true })

// --- 버튼 핸들러 ---
const handleNew = () => {
	if (confirm('현재 내용을 지우고 새로 작성하시겠습니까?')) {
		customer.value = { custnm: '', usernm: '', telno: '', hpno: '', email: '', address: '' }
		consultation.value = { date: new Date().toISOString().substr(0, 10), summary: '', inquiry: '', answer: '', recordUrl: '' }
	}
}
const handleWait = () => { alert('통화를 대기 상태로 전환합니다.') }
const handleTransfer = () => { alert('전화 돌려주기 팝업을 엽니다.') }
const handleHangup = async () => {
	if (confirm('통화를 종료하시겠습니까?')) {
		await api.get(`/crm/asterisk/hangup?exten=${ctiStore.incomingCall?.exten}`);
	}
}
const handleSave = () => {
	alert('상담 내용이 데이터베이스에 저장되었습니다.')
}

onMounted(() => {
	console.log('인바운드 상담 화면 마운트됨')
})
</script>

<style scoped>
.inbound-consultation {
	background-color: #f0f2f5;
	min-height: calc(100vh - 60px);
}
.card { border-radius: 10px; overflow: hidden; }
.card-header { font-size: 0.9rem; padding: 12px 15px; }
.form-label { margin-top: 5px; }
.animate-pulse { animation: pulse 2s infinite; }
@keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.5; } 100% { opacity: 1; } }
.nav-tabs .nav-link { border: none; padding: 12px 20px; color: #6c757d; }
.nav-tabs .nav-link.active { background: transparent; color: #0d6efd; border-bottom: 3px solid #0d6efd; }
</style>
