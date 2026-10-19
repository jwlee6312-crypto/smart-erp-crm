<!--
	=============================================================
	프로그램명	: 로그인 (LoginPage)
	작성일자	: 2025.02.24
	설명        : 시스템 로그인 (소문자 표준화 적용)
	=============================================================
-->

<template>
	<div class="login-page">
		<form class="login-card" @submit.prevent="login">
			<div class="login-header">
				<div class="logo-wrapper">
					<div class="smart-core-brand">
						<span class="text-smart">Smart</span><span class="text-core">Core</span>
					</div>
					<span class="ai-badge"><i class="bi bi-stars me-1"></i>AI Powered</span>
				</div>
				<h2 class="login-title">Integrated Business Solution</h2>
				<p class="login-subtitle">Intelligence-Driven ERP & CRM Integration</p>
			</div>

			<!-- 💡 회사코드 입력 추가 및 라벨 수정 -->
			<div class="form-group">
				<div class="label"><i class="bi bi-building me-1"></i>Company Code</div>
				<div class="form-item-wrap">
					<input
						id="cmpycd"
						v-model="form.cmpycd"
						type="text"
						class="inp"
						name="cmpycd"
						placeholder="Enter your Company Code"
						required
					/>
				</div>
			</div>

			<div class="form-group">
				<div class="label"><i class="bi bi-person-circle me-1"></i>User ID</div>
				<div class="form-item-wrap">
					<input
						id="userid"
						v-model="form.userid"
						type="text"
						class="inp"
						name="userid"
						placeholder="Enter your ID"
						required
					/>
				</div>
			</div>

			<div class="form-group">
				<div class="label"><i class="bi bi-shield-lock-fill me-1"></i>Password</div>
				<div class="form-item-wrap">
					<input
						id="pw"
						v-model="form.passwd"
						type="password"
						class="inp"
						name="passwd"
						placeholder="Enter your password"
						required
					/>
				</div>
			</div>

			<button type="submit" class="login-btn">
				<span>Log In</span>
				<i class="bi bi-arrow-right-short ms-1 fs-5"></i>
			</button>

			<div class="login-footer">
				© 2024 SmartCore. Enhanced by Business AI.
			</div>
		</form>
	</div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/authStore'
import router from '@/router'
import { nextTick, reactive } from 'vue'
import { isServerDown } from '@/utils/isServerDown'

const authStore = useAuthStore()

// 💡 소문자 표준화 적용
interface FormData {
	cmpycd: string
	userid: string
	passwd: string
}

const form = reactive<FormData>({
	cmpycd: 'smart', // 💡 하드코딩 제거 및 초기값 설정
	userid: '',
	passwd: '',
})

const login = async () => {
	try {
		// 💡 소문자 필드 데이터(cmpycd, userid, passwd) 전달
		await authStore.login(form)
		await nextTick()

		if (authStore.isAuthenticated) {
			await router.push('/')
		} else {
			alert('아이디 또는 비밀번호가 올바르지 않습니다.')
		}
	} catch (e: any) {
		console.error('로그인 실패:', e)
		if (isServerDown(e)) {
			alert('서버가 꺼져있거나 응답하지 않습니다.')
			return
		}
		// 💡 실제 에러 메시지(예: "비밀번호가 틀립니다")를 그대로 표시
		const errorMsg = e.message || '로그인에 실패했습니다.';
		alert(errorMsg)
	}
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
@import url('https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css');

.login-page {
	width: 100vw;
	height: 100vh;
	background: radial-gradient(circle at top right, #e3f2fd, #f0f4f8);
	display: flex;
	justify-content: center;
	align-items: center;
	font-family: 'Inter', sans-serif;
}

.login-card {
	width: 440px;
	padding: 50px 40px;
	background: rgba(255, 255, 255, 0.95);
	border-radius: 24px;
	box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.1);
	backdrop-filter: blur(10px);
	border: 1px solid rgba(255, 255, 255, 0.3);
	display: flex;
	flex-direction: column;
	position: relative;
}

.login-header {
	text-align: center;
	margin-bottom: 35px;
}

.logo-wrapper {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 12px;
	margin-bottom: 15px;
}

.smart-core-brand {
	font-size: 2.8rem;
	font-weight: 800;
	letter-spacing: -1.5px;
	line-height: 1;
}

.text-smart { color: #005a9f; }
.text-core { color: #ffc107; }

.ai-badge {
	background: linear-gradient(135deg, #eef6ff, #e3f2fd);
	color: #005a9f;
	font-size: 11px;
	font-weight: 700;
	padding: 4px 10px;
	border-radius: 20px;
	text-transform: uppercase;
	letter-spacing: 0.5px;
	border: 1px solid rgba(0, 90, 159, 0.1);
	display: inline-flex;
	align-items: center;
}

.login-title {
	font-size: 22px;
	font-weight: 700;
	color: #1a202c;
	margin-bottom: 6px;
	letter-spacing: -0.5px;
}

.login-subtitle { font-size: 14px; color: #718096; font-weight: 400; }

.form-group {
	margin-bottom: 18px;
}

.label {
    margin-bottom: 6px;
    font-size: 13px;
    color: #4a5568;
    font-weight: 600;
    display: flex;
    align-items: center;
}

.form-item-wrap { margin-bottom: 0px; }

.inp {
	width: 100%;
	padding: 12px 16px;
	font-size: 14px;
	border: 1.5px solid #e2e8f0;
	border-radius: 12px;
	transition: all 0.2s;
	background-color: #f8fafc;
}

.inp:focus {
	border-color: #005a9f;
	background-color: #fff;
	box-shadow: 0 0 0 4px rgba(0, 90, 159, 0.1);
	outline: none;
}

.login-btn {
	width: 100%;
	padding: 16px;
	font-size: 16px;
	font-weight: 700;
	color: #fff;
	background: #005a9f;
	border: none;
	border-radius: 14px;
	cursor: pointer;
	transition: all 0.3s;
	display: flex;
	justify-content: center;
	align-items: center;
	box-shadow: 0 10px 15px -3px rgba(0, 90, 159, 0.3);
    margin-top: 10px;
}

.login-btn:hover {
	background: #004a85;
	transform: translateY(-2px);
	box-shadow: 0 20px 25px -5px rgba(0, 90, 159, 0.4);
}

.login-footer { margin-top: 30px; text-align: center; font-size: 11px; color: #a0aec0; }
</style>
