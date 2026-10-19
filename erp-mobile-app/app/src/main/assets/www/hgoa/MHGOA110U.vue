<!--
	=============================================================
	프로그램명	  : 모바일 콜백 통합 관리 (MHGOA110U)
    프로그램 ID	: MHGOA110U
	작성일자	    : 25.03.14
	작성자	      : AI Assistant
	설명         : 모바일 전용 콜백 리스트 및 원클릭 발신 (Card UI)
	=============================================================
-->

<template>
    <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

    <div class="mobile-container bg-light min-vh-100 p-2 text-start">
        <!-- 🚀 상단 헤더 -->
        <div class="d-flex justify-content-between align-items-center bg-white p-3 rounded-3 shadow-sm mb-3">
            <div>
                <h5 class="fw-bold mb-0 text-primary small">모바일 콜백 관리</h5>
                <span class="text-muted" style="font-size: 0.65rem;">오늘의 미처리 콜백</span>
            </div>
            <button class="btn btn-primary btn-sm rounded-pill px-3 fw-bold" @click="fetchList">
                <i class="bi bi-arrow-clockwise me-1"></i>새로고침
            </button>
        </div>

        <!-- 💡 콜백 카드 리스트 -->
        <div v-if="callbackList.length === 0" class="text-center py-5 text-muted">
            <i class="bi bi-patch-check fs-1 opacity-25"></i>
            <p class="mt-2 small">처리할 콜백 내역이 없습니다.</p>
        </div>

        <div v-else class="d-flex flex-column gap-3">
            <div v-for="item in callbackList" :key="item.interaction_id"
                 class="card border-0 shadow-sm rounded-3 overflow-hidden"
                 :class="{'border-start border-4 border-danger': item.callback_status !== '030'}">
                <div class="card-body p-3">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <span class="badge bg-light text-dark border small" style="font-size: 0.6rem;">{{ item.start_time?.replace('T', ' ') }}</span>
                        <span class="badge" :class="item.callback_status === '030' ? 'bg-success' : 'bg-danger'" style="font-size: 0.6rem;">
                            {{ item.callback_status === '030' ? '완료' : '대기' }}
                        </span>
                    </div>

                    <div class="mb-2">
                        <div class="fw-bold text-dark fs-6">{{ item.custnm || '알 수 없는 고객' }}</div>
                        <div class="text-primary fw-bold" style="font-size: 0.9rem;">{{ item.callback_no }}</div>
                    </div>

                    <div class="bg-light p-2 rounded small mb-3" style="font-size: 0.75rem; color: #666;">
                        <i class="bi bi-info-circle me-1"></i> {{ item.call_memo || '메모 없음' }}
                    </div>

                    <div class="d-flex gap-2">
                        <button class="btn btn-outline-dark flex-grow-1 btn-sm fw-bold" @click="playAudio(item.rec_file)">
                            <i class="bi bi-play-fill me-1"></i>고객음성
                        </button>
                        <a :href="'tel:' + item.callback_no" class="btn btn-primary flex-grow-1 btn-sm fw-bold d-flex align-items-center justify-content-center">
                            <i class="bi bi-telephone-fill me-1"></i>즉시전화
                        </a>
                    </div>
                </div>
            </div>
        </div>

        <!-- 🎙️ 숨겨진 오디오 플레이어 -->
        <audio ref="mobileAudio" hidden></audio>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { api } from '@/utils/axios'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

const callbackList = ref<any[]>([])
const mobileAudio = ref<HTMLAudioElement | null>(null)

async function fetchList() {
    try {
        const { data } = await api.get('/crm/inbound/callback-list', {
            params: { fromdt: new Date().toISOString().split('T')[0] }
        });
        callbackList.value = data || [];
        vAlert('최신 리스트를 로드했습니다.');
    } catch (e) { vAlertError('조회 실패'); }
}

function playAudio(filename: string) {
    if (!filename || !mobileAudio.value) return;
    mobileAudio.value.src = `/crm/inbound/play-recording?file=${filename}`;
    mobileAudio.value.play().catch(() => vAlertError('재생 불가'));
}

onMounted(() => {
    fetchList();
});
</script>

<style scoped>
.mobile-container { font-family: 'Pretendard', sans-serif; padding-bottom: 50px; }
.card { transition: transform 0.1s; }
.card:active { transform: scale(0.98); }
</style>
