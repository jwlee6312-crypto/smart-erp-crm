<template>
	<div class="spinner-container">
		<svg viewBox="0 0 100 100" class="spinner-svg">
			<!-- 바깥 선 원 -->
			<circle
				cx="50"
				cy="50"
				r="40"
				fill="none"
				stroke="#1976d2"
				stroke-width="6"
				stroke-dasharray="200"
				stroke-dashoffset="50"
				class="outer-circle"
			/>
		</svg>

		<!-- 중앙 이미지 -->
		<img src="/spinner.png" alt="spinner" class="inner-image" />
	</div>
</template>

<style scoped>
.spinner-container {
	position: relative;
	width: 70px;
	height: 70px;
	display: flex;
	align-items: center;
	justify-content: center;
}

.spinner-svg {
	width: 100%;
	height: 100%;
	transform-origin: 50% 50%;
}

.outer-circle {
	transform-origin: 50% 50%;
	animation: rotateOuter 1s linear infinite;
}

/* 중앙 이미지 */
.inner-image {
	position: absolute;
	width: 30px;
	height: 30px;
	animation: rotateInner 2s ease-in-out infinite;
}

@keyframes rotateOuter {
	0% {
		transform: rotate(0deg);
	}
	100% {
		transform: rotate(360deg);
	}
}

@keyframes rotateInner {
	0% {
		transform: rotate(0deg);
	}
	30% {
		transform: rotate(120deg);
	}
	70% {
		transform: rotate(360deg);
	}
}
</style>
<script setup lang="ts"></script>
