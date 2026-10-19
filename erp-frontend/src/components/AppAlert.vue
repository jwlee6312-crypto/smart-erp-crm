<template>
	<transition name="slide-down">
		<div
			v-if="show"
			:class="['app-alert', variant, error ? 'is-error' : 'is-success']"
			role="alert"
		>
			<span class="icon" aria-hidden="true">
				<!-- 성공 -->
				<svg v-if="!error" viewBox="0 0 24 24" fill="currentColor">
					<path d="M9.55 16.45 5.8 12.7l1.4-1.4 2.35 2.34 6.3-6.29 1.41 1.41z" />
				</svg>
				<!-- 실패 -->
				<svg v-else viewBox="0 0 24 24" fill="currentColor">
					<path
						d="M18.3 5.7 12 12l6.3 6.3-1.4 1.4L10.6 13.4 4.3 19.7 2.9 18.3 9.2 12 2.9 5.7 4.3 4.3 10.6 10.6 16.9 4.3z"
					/>
				</svg>
			</span>
			<span class="text">{{ message }}</span>
		</div>
	</transition>
</template>

<script setup lang="ts">
defineProps({
	show: { type: Boolean, default: false },
	error: { type: Boolean, default: false },
	message: { type: String, required: true },
	variant: { type: String, default: 'pop' },
})
</script>

<style scoped>
/* 공통 레이아웃 */
.app-alert {
	position: fixed;
	left: 50%;
	top: 20px;
	transform: translateX(-50%);
	display: flex;
	align-items: center;
	gap: 10px;
	z-index: 30000; /* 💡 상단 네비바(10001) 및 모달보다 위로 배치 */
	min-width: 280px;
	max-width: 90vw;
	padding: 12px 16px;
	font-weight: 600;
	line-height: 1.2;
	box-sizing: border-box;
}

.icon svg {
	width: 20px;
	height: 20px;
}

/* ===== 1) 글래스(유리) 토스트 ===== */
.pop {
	color: #fff;
	border-radius: 14px;
	border: 1px solid rgba(255, 255, 255, 0.25);
	backdrop-filter: blur(12px);
	-webkit-backdrop-filter: blur(12px);
	box-shadow: 0 8px 10px rgba(0, 0, 0, 0.15);
}

/* ✅ 성공 (흰 배경 + 파란 체크) */
.pop.is-success {
	background: rgba(255, 255, 255, 0.85);
	color: #111827; /* 글자 어두운 톤 */
}

.pop.is-success .icon {
	color: #3b82f6; /* blue-500 */
}

/* ❌ 에러 (빨간 배경 카드) */
.pop.is-error {
	background: rgba(232, 83, 83, 0.95); /* red-500 */
	color: #ffffff; /* 글자 흰색 */
	box-shadow: 0 6px 16px rgba(239, 68, 68, 0.45); /* 빨간 그림자 */
}

.pop.is-error .icon {
	color: #ffffff; /* 아이콘도 흰색 */
}

/* 슬라이드 다운(등장) + 자리에서 페이드아웃(퇴장) */
.slide-down-enter-active,
.slide-down-leave-active {
	transition: all 0.35s ease;
}

.slide-down-enter-from {
	opacity: 0;
	transform: translate(-50%, -100%);
}

.slide-down-enter-to {
	opacity: 1;
	transform: translate(-50%, 0);
}

.slide-down-leave-from {
	opacity: 1;
	transform: translate(-50%, 0);
}

.slide-down-leave-to {
	opacity: 0;
	transform: translate(-50%, 0);
}
.text {
	white-space: pre-line;
}
</style>
