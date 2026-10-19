<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	<div>
		<input v-model="closeCheck" type="date" />
	</div>
	<br />
	<div>
		<h2>에러발생 테스트</h2>
		<button @click="err">눌러잇!</button>
	</div>
	<br />
	<br />
	<div style="width: 20%">
		<button @click="test">스피너 테스트</button>
	</div>
	<br />
	<div>
		<h2>메일 보내기 테스트</h2>
		<input v-model="mail" value="" />
		<button @click="send">눌러잇!</button>
	</div>
	<div>
		<h2>피니아 저장된 세션값 체크</h2>
		<button @click="showData">눌러잇!</button>
	</div>
	<div>
		<h2>피니아 전역값 체크</h2>
		<button @click="showAllPinia">눌러잇!</button>
	</div>
	<div>
		<h2>사이드메뉴 대제목 활성화</h2>
		<button @click="A">구매요청 활성화</button>
		<button @click="B">발주관리 활성화</button>
	</div>
	<div>
		<h2>세션체크</h2>
		<button @click="se">세션체크</button>
	</div>
	<div>
		<h2>버전</h2>
		<button @click="fetchLatestVersion">버전체크</button>
	</div>
	<div>
		<h2>암호화 테스트</h2>
		<input v-model="aes" value="" />
		<button @click="send_aes">암호화</button>
		<input v-model="aes2" value="" />
		<button @click="send_aes2">복호화</button>
	</div>
	<a :href="`${$api_url}/manual/${fileName}`" target="_blank" rel="noopener noreferrer">
		매뉴얼 열기
	</a>
</template>

<script setup lang="ts">
const fileName = 'HEPA010U.jpg'
import { useAlerts } from '@/composables/useAlerts'
import { nextTick, onActivated, ref } from 'vue'
import { useCloseYmAlert } from '@/composables/useCloseYmAlert'
import AppAlert from '@/components/AppAlert.vue'
import axios from 'axios'
import { API_URL } from '@/config/api'
import { useSearch } from '@/composables/useSearch'
import { useAuthStore } from '@/stores/authStore'
import { useSave } from '@/composables/useSave'
import { useDelete } from '@/composables/useDelete'
import { getAllPiniaState, logAllPiniaState } from '@/composables/usePiniaDump'
const { searchStart } = useSearch()

const authStore = useAuthStore()
const { vAlert, vAlertError, showAlert, showError, alertMessage } = useAlerts()
const closeCheck = ref<string>('')
const { saveBody } = useSave()
useCloseYmAlert(closeCheck) // 마감체크
const MAIL_URL = import.meta.env.VITE_MAIL_URL

const mail = ref<string>('phmok1@naver.com')
const aes = ref<string>('')
const aes2 = ref<string>('')

async function send() {
	const path = '/email/send-spec'

	const payload = [
		{
			scheduletype: 'N',
			subject: '테스트',
			docgb: '100',
			custcd: '0000006',
			custnm: '(주)북이십일',
			email: mail.value,
			name: '현목팍',
			no: '202511S00001',
			url: `${MAIL_URL}/billing-spec/A?billno=202511S00001`,
		},
	]

	try {
		const { message } = await saveBody(path, payload)
		return vAlert(message)
	} catch (error) {
		console.log(error)
		const message =
			error?.response?.data?.message || error?.message || '명세서 메일전송에 실패했습니다.'
		return vAlertError(message)
	}
}

const test = async () => {
	const res = await axios.get(`${API_URL + '/test-api/10m'}`, {
		params: { test: '테스트 요청' },
		withCredentials: true, // 세션 사용 시 필요
		timeout: 8000,
	})
	console.log(res)
}

const send_aes = async () => {
	const res = await axios.get(`${API_URL + '/test-api/aes_in'}`, {
		params: { text: aes.value },
		withCredentials: true, // 세션 사용 시 필요
	})
	console.log(res.data.message)
	aes2.value = res.data.message
}

const send_aes2 = async () => {
	const res = await axios.get(`${API_URL + '/test-api/aes_out'}`, {
		params: { text: aes2.value },
		withCredentials: true, // 세션 사용 시 필요
	})
	console.log(res.data)
}

// const authStore = useAuthStore()

function showData() {
	const msg = [
		`isAuthenticated: ${authStore.isAuthenticated}`,
		`cmpycd        : ${authStore.cmpycd}`,
		`nacd          : ${authStore.nacd}`,
		`userId        : ${authStore.userId}`,
		`userName      : ${authStore.userName}`,
		`deptCode      : ${authStore.deptCode}`,
		`deptName      : ${authStore.deptName}`,
		`userGrp       : ${authStore.userGrp}`,
	].join('\n')

	alert(msg) // 팝업으로 보기
	console.table({
		// 콘솔에서도 표로 보기
		isAuthenticated: authStore.isAuthenticated,
		cmpycd: authStore.cmpycd,
		nacd: authStore.nacd,
		userId: authStore.userId,
		userName: authStore.userName,
		deptCode: authStore.deptCode,
		deptName: authStore.deptName,
		userGrp: authStore.userGrp,
	})
}

function showAllPinia() {
	const dump = getAllPiniaState()
	alert(JSON.stringify(dump, null, 2)) // 간단 확인
	logAllPiniaState() // 콘솔 표 형태
}

import { useSearchMenu } from '@/composables/useSearchMenu'
import { api } from '@/utils/axios'

const { selectMenu } = useSearchMenu()

async function A() {
	await selectMenu('010')
	document.getElementById('010')?.setAttribute('class', 'nav-link active') // 상단메뉴
	document.getElementById('title010')?.setAttribute('aria-expanded', 'true') // 대제목
	document.getElementById('open010')?.setAttribute('class', 'collapse show') // 펼침
	document
		.getElementById('HEPA010S')
		?.setAttribute('class', 'router-link-active router-link-exact-active sb-nav-link') // 소제목
}

async function B() {
	await selectMenu('020')
	document.getElementById('020')?.setAttribute('class', 'nav-link active') // 상단메뉴
	document.getElementById('title010')?.setAttribute('aria-expanded', 'true')
	document.getElementById('open010')?.setAttribute('class', 'collapse show')
	document
		.getElementById('HEAA010S')
		?.setAttribute('class', 'router-link-active router-link-exact-active sb-nav-link') // 소제목
}

// 최초 진입에서는 실행하지 않도록 가드
let first = true

onActivated(async () => {
	if (first) {
		first = false
		console.log('첫 로딩임')
		return
	}
	console.log('두번째 로딩임')
	await nextTick() // DOM 안정화 후
	await A()
	console.log('mounted')
})

async function se() {
	// const res = await axios.get(`${API_URL}/session-check`)
	const res = await api.get(`/session-check`)
	console.log(res)
}

async function fetchLatestVersion(): Promise<string | null> {
	try {
		const res = await fetch('/version.json', { cache: 'no-store' })
		const data = await res.json()
		console.log(data)
		return data.version
	} catch {
		return null
	}
}

async function err() {
	const path = '/test-api/error'
	const payload = {}
	try {
		const res = await searchStart(path, payload)
		console.log(res)
	} catch (error) {
		console.error(error)
	}
}
</script>
