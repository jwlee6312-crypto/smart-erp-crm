<template>
	<div class="manual-sidebar">
		<!-- 헤더: 초슬림 다크 디자인 (더 세련된 느낌) -->
		<div class="sidebar-header">
			<div class="header-content">
				<i class="bi bi-lightbulb-fill text-warning me-2"></i>
				<span class="title">HELP GUIDE</span>
				<span class="pgm-id ms-auto">{{ fileName }}</span>
				<button class="close-x" @click="close"><i class="bi bi-x"></i></button>
			</div>
		</div>

		<!-- 본문 컨텐츠 -->
		<div class="sidebar-body">
			<!-- 텍스트 매뉴얼 -->
			<div v-if="dbContent" class="content-render">
				<div class="rich-text" v-html="formattedContent"></div>
			</div>

			<!-- 이미지 매뉴얼 -->
			<div v-else-if="images.length > 0" class="image-stack">
				<img v-for="img in images" :key="img" :src="img" alt="manual" />
			</div>

			<!-- 데이터 없음 -->
			<div v-else class="no-data">
				<i class="bi bi-slash-circle"></i>
				<p>등록된 매뉴얼이 없습니다.</p>
			</div>
		</div>

		<!-- 하단 바 -->
		<div class="sidebar-footer">
			<span>SYSTEM ASSISTANT</span>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { api as axiosApi } from '@/utils/axios'
import { API_URL } from '@/config/api'

const props = defineProps<{ fileName: string }>()
const emit = defineEmits(['close'])

const images = ref<string[]>([])
const dbContent = ref('')

// "투박함"을 완전히 제거한 세련된 렌더링 규칙
const formattedContent = computed(() => {
	if (!dbContent.value) return ''
	const lines = dbContent.value.split('\n')
	return lines.map(line => {
		let trimmed = line.trim()
		if (!trimmed) return '<div class="spacer"></div>'

		// ■ 제목 -> 아주 얇고 세련된 라인형 제목
		if (trimmed.startsWith('■')) {
			return `<div class="h1-style">${trimmed.substring(1)}</div>`
		}
		// ● 소제목 -> 세련된 도트형 제목
		if (trimmed.startsWith('●')) {
			return `<div class="h2-style"><span></span>${trimmed.substring(1)}</div>`
		}
		// [강조] -> 미니멀한 알림 박스
		if (trimmed.startsWith('[') && trimmed.includes(']')) {
			const endIdx = trimmed.indexOf(']')
			const tag = trimmed.substring(1, endIdx)
			const content = trimmed.substring(endIdx + 1)
			return `<div class="alert-box"><b>${tag}</b>${content}</div>`
		}

		return `<div class="p-style">${line}</div>`
	}).join('')
})

onMounted(async () => {
	if (!props.fileName) return
	const cleanId = props.fileName.replace(/_/g, '')
	try {
		const res = await axiosApi.get(`/manual/db/${cleanId}`)
		if (res.data?.content) { dbContent.value = res.data.content; return }
	} catch (e) {}

	const apiBase = API_URL
	for (let i = 1; i <= 3; i++) {
		const url = `${apiBase}/manual/${props.fileName}_${String(i).padStart(2, '0')}`
		const res = await fetch(url, { credentials: 'include' })
		if (res.ok) images.value.push(url); else break
	}
})
const close = () => emit('close')
</script>

<style scoped>
/* 초슬림 사이드바 스타일링 */
.manual-sidebar {
	height: 100vh;
	display: flex;
	flex-direction: column;
	background: #fff;
	font-family: 'Segoe UI', 'Pretendard', sans-serif;
}

.sidebar-header {
	background: #2c3e50; /* 전문적인 다크 블루 그레이 */
	height: 40px; /* 더 작게 */
	padding: 0 12px;
	display: flex;
	align-items: center;
	flex-shrink: 0;
}
.header-content {
	width: 100%;
	display: flex;
	align-items: center;
	color: #fff;
}
.title { font-size: 11px; font-weight: 700; letter-spacing: 0.5px; color: #ecf0f1; }
.pgm-id { font-size: 9px; color: #95a5a6; margin-left: 8px; }
.close-x {
	background: none; border: none; color: #bdc3c7; font-size: 18px;
	cursor: pointer; padding: 0; margin-left: auto;
}
.close-x:hover { color: #fff; }

.sidebar-body { flex: 1; overflow-y: auto; background: #fff; }
.sidebar-body::-webkit-scrollbar { width: 3px; }
.sidebar-body::-webkit-scrollbar-thumb { background: #ced4da; }

.content-render { padding: 15px; }
.rich-text { font-size: 12px; color: #333; line-height: 1.6; }

/* 콤팩트 렌더링 스타일 */
:deep(.h1-style) {
	font-size: 13px; font-weight: 800; color: #000;
	border-bottom: 2px solid #3498db;
	padding-bottom: 3px; margin: 15px 0 8px 0;
}
:deep(.h2-style) {
	font-size: 12px; font-weight: 700; color: #2c3e50;
	margin: 10px 0 5px 0; display: flex; align-items: center;
}
:deep(.h2-style span) {
	width: 3px; height: 3px; background: #3498db; border-radius: 50%; margin-right: 6px;
}
:deep(.alert-box) {
	background: #f1f4f6; border-radius: 4px;
	padding: 8px 10px; margin: 8px 0; font-size: 11.5px;
	border-left: 3px solid #3498db;
}
:deep(.alert-box b) { color: #2980b9; margin-right: 5px; }
:deep(.spacer) { height: 8px; }
:deep(.p-style) { margin-bottom: 3px; }

.sidebar-footer {
	height: 24px; background: #f8f9fa; border-top: 1px solid #eee;
	display: flex; align-items: center; justify-content: center;
}
.sidebar-footer span { font-size: 8px; color: #95a5a6; font-weight: 600; }
</style>
