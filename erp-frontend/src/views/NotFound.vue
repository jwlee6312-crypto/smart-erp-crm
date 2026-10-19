<template>
	<main
		class="d-flex flex-column min-vh-100 align-items-center justify-content-center text-center p-4 bg-light"
	>
		<div class="container" style="max-width: 560px">
			<div class="rounded-4 shadow-sm bg-white p-4 p-sm-5">
				<div class="mb-2 text-uppercase text-secondary small fw-semibold">Error</div>
				<h1 class="display-3 fw-bold mb-1">404</h1>
				<p class="text-secondary mb-4">요청하신 페이지를 찾을 수 없습니다.</p>

				<div class="d-flex gap-2 justify-content-center">
					<button class="btn btn-outline-dark" @click="goHome">홈으로 가기</button>
				</div>
			</div>

			<div class="mt-3 text-muted small">
				요청 경로: <code class="text-body-secondary">{{ route.fullPath }}</code>
			</div>
		</div>
	</main>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const goHome = () => router.replace('/')
</script>
