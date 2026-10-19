import { useAuthStore } from '@/stores/authStore'
import { useMenuStore } from '@/stores/menuStore'
import { api } from '@/utils/axios'

export function useSession() {
	const authStore = useAuthStore()
	const menuStore = useMenuStore()

	/**
	 * 💡 서버 세션 유효성 체크 및 데이터 복구 (통합 API 대응)
	 */
	async function checkSession(): Promise<boolean> {
		try {
			// 통합 세션 경로로 변경
			    const res = await api.get('/comm/session')

			if (res.status === 200 && res.data) {
				const data = res.data

				// 1. 사용자 정보 복구
				authStore.isAuthenticated = true
				authStore.CMPYCD = data.CMPYCD
				authStore.USER_ID = data.USER_ID
				authStore.USER_NAME = data.USER_NAME
				authStore.DEPTCD = data.DEPTCD
				authStore.DEPTNM = data.DEPTNM
				authStore.USERGRP = data.USERGRP
				authStore.SALSYN = data.SALSYN

				// 💡 2. 자동 로그인 시에도 상단 메뉴 및 좌측 메뉴 정보를 가져옵니다.
				if (menuStore.topMenuItems.length === 0) {
					await menuStore.fetchTopMenus()
				}

				return true
			}
			return false
		} catch (e) {
			authStore.isAuthenticated = false
			return false
		}
	}

	async function fetchSession() {
		return await checkSession()
	}

	return { checkSession, fetchSession }
}
