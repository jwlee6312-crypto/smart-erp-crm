import { onActivated, nextTick, ref } from 'vue'
import { useRoute } from 'vue-router'
import { activation } from '@/utils/menuActivation'

// keep-alive 된 컴포넌트 재활성화 시 메뉴 자동 업데이트
export function useMenuActivation() {
	const route = useRoute()
	const first = ref(true)

	onActivated(async () => {
		if (first.value) {
			first.value = false
			console.log('첫 로딩 - 스킵')
			return
		}
		console.log('재활성화 - 메뉴 갱신')
		await nextTick()

		const { topMenuCode, sideMenuCode, title } = route.meta
		if (!topMenuCode || !sideMenuCode || !title) {
			console.warn('meta 정보 없음:', route.fullPath)
			return
		}

		await activation(topMenuCode as string, sideMenuCode as string, title as string)
	})
}