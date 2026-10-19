<template>
	<div class="container-fluid py-3 inbound-page">
		<!-- 1. 우측 상단 버튼 배치 -->
		<div class="d-flex justify-content-end gap-2 mb-3 bg-white p-2 rounded shadow-sm border">
			<button class="btn btn-outline-primary px-3" @click="handleNew">신규</button>
			<button class="btn btn-outline-warning px-3" @click="handleWait">전화대기</button>
			<button class="btn btn-outline-info text-white px-3" @click="handleTransfer">전화돌려주기</button>
			<button class="btn btn-outline-danger px-3" @click="handleHangup">전화끊기</button>
			<button class="btn btn-success px-4" @click="handleSave">상담저장</button>
		</div>

		<!-- 2. 중단 섹션 (상단 좌/우 분할) -->
		<div class="row g-3 mb-4">
			<!-- [좌측 상단] 고객정보 -->
			<div class="col-md-5">
				<div class="card h-100 border-secondary-subtle">
					<div class="card-header bg-secondary bg-opacity-10 fw-bold text-dark border-bottom-0">고객정보</div>
					<div class="card-body">
						<div class="row g-2 text-start">
							<div class="col-12 mb-2">
								<label class="form-label small fw-bold text-muted">거래처명</label>
								<input type="text" class="form-control form-control-sm border-primary-subtle" v-model="customer.custnm" readonly />
							</div>
							<div class="col-6 mb-2">
								<label class="form-label small fw-bold text-muted">고객이름</label>
								<input type="text" class="form-control form-control-sm" v-model="customer.usernm" />
							</div>
							<div class="col-6 mb-2">
								<label class="form-label small fw-bold text-muted">회사전화</label>
								<input type="text" class="form-control form-control-sm" v-model="customer.telno" />
							</div>
							<div class="col-6 mb-2">
								<label class="form-label small fw-bold text-muted">핸드폰</label>
								<input type="text" class="form-control form-control-sm" v-model="customer.hpno" />
							</div>
							<div class="col-6 mb-2">
								<label class="form-label small fw-bold text-muted">메일</label>
								<input type="email" class="form-control form-control-sm" v-model="customer.email" />
							</div>
							<div class="col-12">
								<label class="form-label small fw-bold text-muted">주소</label>
								<textarea class="form-control form-control-sm" rows="2" v-model="customer.address"></textarea>
							</div>
						</div>
					</div>
				</div>
			</div>

			<!-- [우측 상단] 상담내용 -->
			<div class="col-md-7">
				<div class="card h-100 border-primary-subtle">
					<div class="card-header bg-primary bg-opacity-10 fw-bold text-dark border-bottom-0">상담내용</div>
					<div class="card-body">
						<div class="row g-2 text-start">
							<div class="col-md-6 mb-2">
								<label class="form-label small fw-bold text-muted">상담일자</label>
								<input type="date" class="form-control form-control-sm" v-model="consult.date" />
							</div>
							<div class="col-md-6 mb-2">
								<label class="form-label small fw-bold text-muted">녹취정보(오디오파일)</label>
								<audio controls class="w-100" style="height: 31px;">
									<source :src="consult.recordUrl" type="audio/wav" />
								</audio>
							</div>
							<div class="col-12 mb-2">
								<label class="form-label small fw-bold text-muted">상담요약</label>
								<input type="text" class="form-control form-control-sm" v-model="consult.summary" />
							</div>
							<div class="col-12 mb-2">
								<label class="form-label small fw-bold text-muted">문의내용</label>
								<textarea class="form-control form-control-sm" rows="3" v-model="consult.inquiry"></textarea>
							</div>
							<div class="col-12">
								<label class="form-label small fw-bold text-muted">답변내용</label>
								<textarea class="form-control form-control-sm" rows="3" v-model="consult.answer"></textarea>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 3. 하단 탭 영역 -->
		<div class="card shadow-sm border-0">
			<ul class="nav nav-tabs px-3 pt-2 bg-light border-bottom-0" id="crmTabs" role="tablist">
				<li class="nav-item"><button class="nav-link active fw-bold" data-bs-toggle="tab" data-bs-target="#tab-history">상담내역리스트</button></li>
				<li class="nav-item"><button class="nav-link fw-bold" data-bs-toggle="tab" data-bs-target="#tab-service">서비스처리내역</button></li>
				<li class="nav-item"><button class="nav-link fw-bold" data-bs-toggle="tab" data-bs-target="#tab-billing">과금청구리스트</button></li>
				<li class="nav-item"><button class="nav-link fw-bold" data-bs-toggle="tab" data-bs-target="#tab-contract">약정리스트</button></li>
			</ul>
			<div class="tab-content border border-top-0 p-0 bg-white" style="min-height: 300px;">
				<!-- 탭 1: 상담내역 -->
				<div class="tab-pane fade show active" id="tab-history">
					<table class="table table-sm table-hover mb-0 text-center">
						<thead class="table-light">
							<tr><th>상담일시</th><th>상담원</th><th>문의내용</th><th>고객이름</th></tr>
						</thead>
						<tbody><tr v-for="i in 3" :key="i"><td>-</td><td>-</td><td class="text-start">-</td><td>-</td></tr></tbody>
					</table>
				</div>
				<!-- 탭 2: 서비스처리 -->
				<div class="tab-pane fade" id="tab-service">
					<table class="table table-sm table-hover mb-0 text-center">
						<thead class="table-light">
							<tr><th>접수일자</th><th>서비스담당자</th><th>처리일자</th><th>처리결과</th><th>처리내용</th></tr>
						</thead>
						<tbody><tr v-for="i in 3" :key="i"><td>-</td><td>-</td><td>-</td><td>-</td><td class="text-start">-</td></tr></tbody>
					</table>
				</div>
				<!-- 탭 3: 과금청구 -->
				<div class="tab-pane fade" id="tab-billing">
					<table class="table table-sm table-hover mb-0 text-end">
						<thead class="table-light text-center">
							<tr><th>청구일자</th><th>정산번호</th><th>사용기간</th><th>서비스명</th><th>약정가</th><th>공급가</th><th>부가세</th><th>합계</th></tr>
						</thead>
						<tbody><tr v-for="i in 3" :key="i"><td>-</td><td>-</td><td>-</td><td class="text-start">-</td><td>0</td><td>0</td><td>0</td><td>0</td></tr></tbody>
					</table>
				</div>
				<!-- 탭 4: 약정리스트 -->
				<div class="tab-pane fade" id="tab-contract">
					<table class="table table-sm table-hover mb-0 text-center">
						<thead class="table-light text-secondary">
							<tr><th>약정명</th><th>약정기간</th><th>품목명</th><th>약정주기</th><th>약정가</th></tr>
						</thead>
						<tbody><tr v-for="i in 3" :key="i"><td>-</td><td>-</td><td>-</td><td>-</td><td>0</td></tr></tbody>
					</table>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useCtiStore } from '@/stores/ctiStore'

const ctiStore = useCtiStore()

const customer = ref({ custnm: '', usernm: '', telno: '', hpno: '', email: '', address: '' })
const consult = ref({ date: new Date().toISOString().substring(0, 10), inquiry: '', answer: '', summary: '', recordUrl: '' })

watch(() => ctiStore.incomingCall, (newCall) => {
	if (newCall) {
		customer.value.custnm = newCall.custnm || ''
		customer.value.telno = newCall.callerId || ''
		customer.value.address = newCall.address || ''
		customer.value.hpno = newCall.hpno || ''
		customer.value.email = newCall.email || ''
		customer.value.usernm = newCall.usernm || ''
	}
}, { immediate: true })

const handleNew = () => { if(confirm('새 상담을 시작하시겠습니까?')) { location.reload() } }
const handleWait = () => { alert('대기 모드') }
const handleTransfer = () => { alert('전화 돌려주기') }
const handleHangup = () => { alert('통화 종료') }
const handleSave = () => { alert('상담 데이터가 저장되었습니다.') }
</script>

<style scoped>
.inbound-page { background-color: #f8f9fa; min-height: 100vh; }
.card { border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
.card-header { font-size: 0.95rem; padding: 10px 15px; }
.table th { font-size: 0.85rem; vertical-align: middle; white-space: nowrap; }
.table td { font-size: 0.85rem; vertical-align: middle; }
.nav-tabs .nav-link { border: none; padding: 12px 25px; color: #6c757d; font-weight: 500; }
.nav-tabs .nav-link.active { background: #fff; color: #0d6efd; border-top: 3px solid #0d6efd; font-weight: bold; }
</style>
