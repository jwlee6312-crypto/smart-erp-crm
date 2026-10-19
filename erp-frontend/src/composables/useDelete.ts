import { api } from '@/utils/axios'
import { useLoadingStore } from '@/stores/loadingStore'

// Program     : -
// Program ID  : -
// Create-Date : 25.05.02
// Creator     : 이현준
// Modify-Date : 26.01.09
// Modifier    : 이현준
// History     : import 주소 변경

export function useDelete() {
	const loadingStore = useLoadingStore()
	// java 단 @RequestParam 로 받을때
	async function deleteParam(path: string, params: Record<string, any>) {
		try {
			loadingStore.startLoading()
			console.log('delete 요청보낼 데이터:', params)
			const res = await api.post(path, null, { params })
			await new Promise((resolve) => setTimeout(resolve, 100))
			console.log(res)
			return res.data
		} catch (error) {
			console.error('삭제 실패:', error)
			throw error
		} finally {
			loadingStore.stopLoading()
		}
	}

	// java 단 @RequestBody 로 받을때
	async function deleteBody(path: string, params: any) {
		const loadingStore = useLoadingStore()
		try {
			loadingStore.startLoading()
			console.log('delete 요청보낼 데이터:', params)
			const res = await api.post(path, params)
			await new Promise((resolve) => setTimeout(resolve, 100))
			console.log(res)
			return res.data
		} catch (error) {
			console.error('삭제 실패:', error)
			throw error
		} finally {
			loadingStore.stopLoading()
		}
	}

	// java 단 delete 로 받을때
	async function deleteDelete(path: string, params: Record<string, any>) {
		const loadingStore = useLoadingStore()
		try {
			loadingStore.startLoading()
			console.log('보낼데이터:  ', params)
			const res = await api.delete(path, { params })
			await new Promise((resolve) => setTimeout(resolve, 100))
			console.log(res)
			return res.data
		} catch (error) {
			console.error('삭제 실패:', error)
			throw error
		} finally {
			loadingStore.stopLoading()
		}
	}
	return { deleteParam, deleteBody, deleteDelete }
}
