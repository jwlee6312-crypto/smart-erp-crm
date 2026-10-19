import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/utils/axios'
import { useTabStore } from '@/stores/tabStore'
import { useMenuStore } from '@/stores/menuStore'

export const useAuthStore = defineStore(
	'auth',
	() => {
		const isAuthenticated = ref<boolean>(false)

		const cmpycd = ref<string>('')
		const cmpynm = ref<string>('') // 🚀 회사명 필드 추가
		const userid = ref<string>('')
		const usernm = ref<string>('')
		const inner_no = ref<string>('')
		const deptcd = ref<string>('')
		const deptnm = ref<string>('')
		const usergrp = ref<string>('')
		const salsyn = ref<string>('')
		const email = ref<string>('')
		const photo_path = ref<string>('') // 🚀 사진 경로 필드 추가

		const tabStore = useTabStore()
		const menuStore = useMenuStore()

		function setUserInfo(data: any) {
			if (!data) return;
			console.log('👤 로그인 사용자 정보:', data); // 🚀 데이터 구조 확인용
			cmpycd.value = data.cmpycd || ''
			cmpynm.value = data.cmpynm || '' // 🚀 회사명 매핑
			userid.value = String(data.userid || '').trim()
			usernm.value = String(data.usernm || '').trim()
			inner_no.value = String(data.inner_no || '').trim()
			deptcd.value = data.deptcd || ''
			deptnm.value = data.deptnm || ''
			usergrp.value = data.usergrp || ''
			salsyn.value = data.salsyn || ''
			email.value = data.email || ''
			// 🚀 [보정] photo_path 가 없으면 photopath, PHOTO_PATH 등 유사 키도 검색
			photo_path.value = String(data.photo_path || data.photopath || data.PHOTO_PATH || '').trim()
			isAuthenticated.value = !!userid.value
		}

		async function checkSession() {
			try {
				const res = await api.get('/comm/session')
				if (res.data) {
					setUserInfo(res.data)
					await menuStore.fetchMenus()
					return true
				}
			} catch (e) {
				console.warn("세션 정보 없음");
				return false
			}
			return false
		}

		async function login(loginData: any) {
			try {
				const res = await api.post('/comm/login', {
					cmpycd: loginData.cmpycd,
					userid: loginData.userid,
					passwd: loginData.passwd
				})

				if (res.status === 200 && res.data) {
					setUserInfo(res.data)
					await menuStore.fetchMenus()
					return res.data
				}
			} catch (e: any) {
				console.error('로그인 실패:', e)
				// 💡 백엔드에서 문자열("비밀번호가 틀립니다")을 보내므로 data를 통째로 가져옴
				let errorMsg = '로그인 중 오류가 발생했습니다.'
				if (e.response?.data) {
					errorMsg = typeof e.response.data === 'string' ? e.response.data : (e.response.data.message || errorMsg)
				} else if (e.message) {
					errorMsg = e.message
				}
				throw new Error(errorMsg)
			}
		}

		async function logout() {
			try {
				await api.post('/comm/logout')
			} catch (e) {
				console.warn('로그아웃 API 호출 실패:', e)
			} finally {
				resetState()
				tabStore.tabs = []
				menuStore.clearMenus()
				sessionStorage.clear()
				localStorage.clear()
				window.location.href = '/auth/login'
			}
		}

		function resetState() {
			isAuthenticated.value = false
			cmpycd.value = ''
			cmpynm.value = ''
			userid.value = ''
			usernm.value = ''
			inner_no.value = ''
			deptcd.value = ''
			deptnm.value = ''
			usergrp.value = ''
			salsyn.value = ''
			email.value = ''
			photo_path.value = ''
		}

		return {
			isAuthenticated,
			cmpycd, cmpynm, userid, usernm, inner_no, deptcd, deptnm, usergrp, salsyn, email, photo_path,
			login, logout, resetState, checkSession, setUserInfo
		}
	},
	{
		persist: { storage: sessionStorage },
	}
)
