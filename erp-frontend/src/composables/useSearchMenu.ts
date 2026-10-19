import { useMenuStore } from '@/stores/menuStore'

export function useSearchMenu() {
	/**
	 * 💡 [확인사살] 과거의 /main/menus 호출을 제거하고
	 * 통합된 menuStore.selectTopMenu를 호출하도록 수정
	 */
	async function selectMenu(codecd: string) {
		const menuStore = useMenuStore()

		// 1. 선택한 탑메뉴 활성화 및 해당 좌측 메뉴 로드 (/comm/left-menus 호출)
		try {
			await menuStore.selectTopMenu(codecd)
		} catch (e) {
			console.error('메뉴 전환 실패:', e)
		}
	}

	return { selectMenu }
}
