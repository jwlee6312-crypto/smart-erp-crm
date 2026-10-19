import axios from 'axios'
import { API_URL } from '@/config/api'
import router from '@/router'
import { useAuthStore } from '@/stores/authStore'
import { checkVer } from '@/composables/useVersionCheck'

/**
 * 💡 [데이터 무결성 및 소문자 정규화]
 * 1. 모든 키를 소문자로 변환
 * 2. 'A.ITEMCD'와 같은 DB 테이블 별칭 제거
 * 3. 문자열 내의 Null(\u0000) 및 특수 공백(NBSP) 제거
 */
const standardizeKeys = (obj: any): any => {
	// 기초 자료형 및 특수 객체(FormData, Blob 등)는 그대로 반환
	if (obj === null || obj === undefined || obj instanceof FormData || obj instanceof Blob || obj instanceof Date) {
		return obj
	}

	// 🚀 [문자열 정제] 데이터베이스 유령 문자 및 특수 공백 차단
	if (typeof obj === 'string') {
		return obj.replace(/\u0000/g, '')     // Null 문자 제거
				  .replace(/\u00a0/g, ' ')     // NBSP를 일반 공백으로
				  .trim()
	}

	// 배열 처리 (조회 결과 리스트)
	if (Array.isArray(obj)) {
		return obj.map(standardizeKeys)
	}

	// 객체 처리 (결과 Row 또는 Wrapped Response)
	if (typeof obj === 'object') {
		const newObj: any = {}
		for (const key in obj) {
			if (Object.prototype.hasOwnProperty.call(obj, key)) {
				// 키 소문자 변환
				let newKey = key.toLowerCase()
				// DB 별칭 제거 (A.ITEMCD -> itemcd)
				const dotIdx = newKey.lastIndexOf('.')
				if (dotIdx !== -1) {
					newKey = newKey.substring(dotIdx + 1)
				}
				// 값에 대해서도 재귀적으로 수행 (중첩 구조 완벽 대응)
				newObj[newKey] = standardizeKeys(obj[key])
			}
		}
		return newObj
	}
	return obj
}

export const api = axios.create({
	baseURL: API_URL,
	withCredentials: true,
	timeout: 1200000,
})

// 🚀 요청 인터셉터: 보낼 때도 데이터를 소문자로 정규화
api.interceptors.request.use(
	async (config) => {
		if (config.url?.includes('version.json')) return config

		// 1. 앱 버전 체크
		const ok = await checkVer()
		if (!ok) {
			const authStore = useAuthStore()
			alert('버전이 업데이트 되었습니다. 로그인 페이지로 이동합니다.')
			await authStore.logout()
			await router.push('/auth/login')
			return Promise.reject(new Error('APP_VERSION_CHANGED'))
		}

		// 2. 전송 데이터(payload) 정규화
		if (config.data && !(config.data instanceof FormData)) {
			config.data = standardizeKeys(config.data)
		}
		if (config.params) {
			config.params = standardizeKeys(config.params)
		}
		return config
	},
	(error) => Promise.reject(error)
)

// 🚀 응답 인터셉터: 모든 API 응답의 '입구'에서 표준화 강제 실행
let isAlerting = false

api.interceptors.response.use(
	(response) => {
		// [Step 1] 어떤 형태든 들어오는 모든 데이터의 키를 소문자로 강제 정규화
		const clean = standardizeKeys(response.data)

		// [Step 2] 백엔드 공통 껍데기({ status, data, message })가 있는지 확인
		if (clean && typeof clean === 'object' && 'status' in clean && 'data' in clean) {
			// 💡 무조건 'data(알맹이)'만 추출하여 Vue 컴포넌트에 전달
			// 이제 모든 화면에서 res.data는 무조건 "진짜 데이터" 리스트나 객체임
			return { ...response, data: clean.data ?? clean }
		}

		// [Step 3] 껍데기가 없는 순수 결과(배열 등)인 경우 정규화된 값 그대로 반환
		return { ...response, data: clean }
	},
	async (error) => {
		// 🚀 세션 유실(401/403) 발생 시 강제 로그아웃 정책 적용
		if (error.response?.status === 401 || error.response?.status === 403) {
			const authStore = useAuthStore()

			// 🚀 [해결] 로그인 페이지 자체이거나, 로그인/세션 체크 API 요청인 경우엔
			// 절대로 '세션 유실' 알림을 띄우지 않고 조용히 에러를 반환함
			const isAuthPage = window.location.pathname.includes('/auth/login');
			const isAuthApi = error.config.url.includes('/comm/login') || error.config.url.includes('/comm/session');

			if (isAuthPage || isAuthApi || isAlerting) {
				return Promise.reject(error)
			}

			isAlerting = true
			alert('세션이 만료되었습니다. 다시 로그인해 주세요.')

			authStore.resetState()
			sessionStorage.clear()
			localStorage.clear()

			// 💡 무조건 로그인 페이지로 강제 이동 (가장 확실한 방법)
			window.location.href = '/auth/login'
			return new Promise(() => {})
		}

		// 에러 응답 데이터도 소문자로 정규화하여 일관성 유지
		if (error.response?.data) {
			error.response.data = standardizeKeys(error.response.data)
		}
		return Promise.reject(error)
	}
)
