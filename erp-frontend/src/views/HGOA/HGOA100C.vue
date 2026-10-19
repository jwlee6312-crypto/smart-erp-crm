<!--
	=============================================================
	프로그램명	  : 캠페인 상담 워크스페이스 (고객용 모바일)
    프로그램 ID	: HGOA100C
	작성일자	    : 25.03.05
	작성자	      : AI Assistant
	=============================================================
-->

<template>
	<div class="mobile-lobby-wrapper d-flex align-items-center justify-content-center min-vh-100 p-2 p-md-3 bg-light">
		<div class="phone-frame shadow-2xl border border-dark rounded-5 overflow-hidden d-flex flex-column bg-black position-relative mx-auto">
			<div class="phone-notch"></div>
			<div class="phone-status-bar d-flex justify-content-between align-items-center px-4 pt-3 pb-1 bg-white flex-shrink-0">
				<div class="small fw-bold text-dark">{{ currentTime }}</div>
				<div class="d-flex gap-1 small text-dark"><i class="bi bi-reception-4"></i><i class="bi bi-wifi"></i><i class="bi bi-battery-full"></i></div>
			</div>

			<div class="phone-app-content d-flex flex-column bg-white flex-grow-1 overflow-hidden">
				<header class="app-header p-3 text-white d-flex align-items-center flex-shrink-0">
					<div class="avatar-circle bg-white text-primary rounded-circle me-3 d-flex align-items-center justify-content-center shadow-sm" style="width: 40px; height: 40px;">
						<i class="bi bi-headset fs-4"></i>
					</div>
					<div class="flex-grow-1 text-start">
						<h6 class="fw-bold mb-0">상담센터</h6>
						<div class="d-flex align-items-center"><span class="online-dot me-1"></span><span style="font-size: 0.65rem; opacity: 0.9;">실시간 상담원 연결 중</span></div>
					</div>
				</header>

				<main class="chat-timeline flex-grow-1 p-3 overflow-auto bg-light d-flex flex-column" ref="scrollRef">
					<div v-for="(msg, idx) in chatHistory" :key="idx" class="message-wrapper d-flex mb-3 animate-fade-in" :class="msg.isMe ? 'justify-content-end' : 'justify-content-start'">
						<div class="message-group" :class="msg.isMe ? 'text-end' : 'text-start'">
							<div class="message-bubble p-2 px-3 shadow-sm" :class="msg.isMe ? 'bg-primary text-white user-msg' : 'bg-white text-dark agent-msg'">{{ msg.text }}</div>
							<div class="message-time mt-1 text-muted" style="font-size: 0.6rem;">{{ msg.time }}</div>
						</div>
					</div>
				</main>

				<footer class="app-input-area p-3 border-top bg-white flex-shrink-0">
					<div class="input-pill-container d-flex align-items-center bg-light rounded-pill px-2 py-1 border border-secondary border-opacity-25">
						<input type="text" v-model="messageInput" class="form-control border-0 bg-transparent shadow-none px-2 small" placeholder="메시지 입력..." @keyup.enter="handleSend">
						<button class="btn btn-primary rounded-circle d-flex align-items-center justify-content-center shadow-sm" style="width: 34px; height: 34px;" @click="handleSend" :disabled="isSending">
							<i v-if="!isSending" class="bi bi-arrow-up fs-5"></i>
							<span v-else class="spinner-border spinner-border-sm text-white"></span>
						</button>
					</div>
				</footer>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '@/utils/axios'

const route = useRoute(); const scrollRef = ref<HTMLElement | null>(null)
const customerName = ref(route.query.name as string || '고객')
const customerEmail = ref(route.query.email as string || '')
const messageInput = ref(''); const isSending = ref(false); const chatHistory = ref<any[]>([])
let pollInterval: any = null; const currentTime = ref(new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit', hour12: false}))

const fetchMessages = async () => {
    if (!customerEmail.value) return;
    try {
        const res = await api.get('/common/chat/messages', { params: { email: customerEmail.value } });
        const list = res.data?.data || [];
        const reversedList = [...list].reverse();
        const newHistory = reversedList.filter((m: any) => !String(m.message_type).includes('2')).map((m: any) => {
            const t = String(m.message_type).toLowerCase();
            return {
                text: m.content,
                isMe: t.includes('incoming') || t.includes('0'),
                time: new Date(m.created_at).toLocaleTimeString([], {hour:'2-digit', minute:'2-digit', hour12: false})
            };
        });
        if (JSON.stringify(newHistory) !== JSON.stringify(chatHistory.value.filter(h => !h.isOptimistic))) {
            chatHistory.value = newHistory;
            nextTick(scrollToBottom);
        }
    } catch (e) {}
}

const handleSend = async () => {
    const content = messageInput.value.trim();
    if (!content || isSending.value || !customerEmail.value) return;
    const optimisticMsg = { text: content, isMe: true, isOptimistic: true, time: new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit', hour12: false}) };
    chatHistory.value.push(optimisticMsg); messageInput.value = ''; nextTick(scrollToBottom);
    isSending.value = true;
    try {
        await api.post('/common/chat/inquiry', { email: customerEmail.value, name: customerName.value, content: content });
        await fetchMessages();
    } catch (e) {} finally { isSending.value = false; }
}

const scrollToBottom = () => { if (scrollRef.value) scrollRef.value.scrollTop = scrollRef.value.scrollHeight; }
onMounted(() => { if (customerEmail.value) { fetchMessages(); pollInterval = setInterval(fetchMessages, 3000); } });
onUnmounted(() => { if (pollInterval) clearInterval(pollInterval); });
</script>

<style scoped>
.mobile-lobby-wrapper { font-family: -apple-system, sans-serif; overflow: hidden; }
.phone-frame { width: 100%; max-width: 380px; height: 85vh; max-height: 750px; border: 10px solid #2c2c2c !important; border-radius: 3rem !important; }
.phone-notch { position: absolute; top: 0; left: 50%; transform: translateX(-50%); width: 150px; height: 25px; background: #2c2c2c; border-bottom-left-radius: 15px; border-bottom-right-radius: 15px; z-index: 10; }
.app-header { background: linear-gradient(135deg, #0d6efd 0%, #0046b5 100%); }
.online-dot { width: 8px; height: 8px; background-color: #00ff88; border-radius: 50%; display: inline-block; }
.message-bubble { max-width: 85%; font-size: 0.85rem; border-radius: 1rem; word-break: break-all; }
.user-msg { border-bottom-right-radius: 2px; }
.agent-msg { border-bottom-left-radius: 2px; border: 1px solid #dee2e6; }
.chat-timeline { scrollbar-width: none; }
.chat-timeline::-webkit-scrollbar { display: none; }
.animate-fade-in { animation: fadeIn 0.2s ease-in-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(5px); } to { opacity: 1; transform: translateY(0); } }
</style>
