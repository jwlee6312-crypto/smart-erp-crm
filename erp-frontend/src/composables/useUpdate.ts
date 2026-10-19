import { api } from '@/utils/axios'
import { useLoadingStore } from '@/stores/loadingStore'

// Program     : -
// Program ID  : -
// Create-Date : 25.05.15
// Creator     : 이현준
// Modify-Date : 26.01.09
// Modifier    : 이현준
// History     : import 주소 변경

export function useUpdate() {
	const loadingStore = useLoadingStore()
	async function updateStart(path: string, updateData: Record<string, any>) {
		console.log('보낼 데이터:', updateData)
		try {
			loadingStore.startLoading()
			const res = await api.post(path, updateData)
			console.log(res.data)
			await new Promise((resolve) => setTimeout(resolve, 100))
			return res.data
		} catch (err) {
			console.error('수정 실패:', err)
			throw err
		} finally {
			loadingStore.stopLoading()
		}
	}

	return { updateStart }
}

export function useUpdate2() {
	const loadingStore = useLoadingStore()
	async function updateStart2(path: string, updateData: Record<string, any>) {
		console.log('보낼 데이터:', updateData)
		try {
			loadingStore.startLoading()
			const res = await api.put(path, updateData)
			console.log(res.data)
			await new Promise((resolve) => setTimeout(resolve, 100))
			return res.data
		} catch (err) {
			console.error('수정 실패:', err)
			throw err
		} finally {
			loadingStore.stopLoading()
		}
	}

	return { updateStart2 }
}
