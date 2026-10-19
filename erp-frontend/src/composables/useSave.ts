// 작성일자	: 25.04.22
// 작성자		: 이현준
// 수정일자	: 25.12.10
// 수정자		: 이현준
// 수정 내용	: import alias 수정

import { api } from '@/utils/axios'
import { useLoadingStore } from '@/stores/loadingStore'

export function useSave() {
	const loadingStore = useLoadingStore()

	async function saveBody(
		path: string,
		body?: Record<string, any> | null,
		options?: { timeout?: number }
	) {
		console.log('body 요청보낼 데이터:', body)

		try {
			loadingStore.startLoading()
			const res = await api.post(path, body, { timeout: options?.timeout })
			console.log(res.data)
			await new Promise((resolve) => setTimeout(resolve, 100))
			return res.data
		} catch (err) {
			console.log('저장 실패', err)
			throw err
		} finally {
			loadingStore.stopLoading()
		}
	}

	async function saveParams(path: string, params?: Record<string, any> | null) {
		console.log('param 요청보낼 데이터:', params)

		try {
			loadingStore.startLoading()
			const res = await api.post(path, { params })
			console.log(res.data)
			await new Promise((resolve) => setTimeout(resolve, 100))
			return res.data
		} catch (err) {
			console.log('저장 실패', err)
			throw err
		} finally {
			loadingStore.stopLoading()
		}
	}

	async function downloadStart(path: string, body?: Record<string, any> | null) {
		try {
			loadingStore.stopLoading()
			console.log('body 요청보낼 데이터:', body)

			const res = await api.post(path, body, {
				responseType: 'blob',
			})
			console.log(res.data)
			await new Promise((resolve) => setTimeout(resolve, 100))
			return res
		} catch (err) {
			console.log('다운로드 실패: ', err)
			throw err
		} finally {
			loadingStore.stopLoading()
		}
	}

	async function uploadFile(path: string, formData: FormData) {
		console.log('업로드 요청 데이터: ', formData)

		try {
			loadingStore.startLoading()
			const res = await api.post(path, formData)
			await new Promise((resolve) => setTimeout(resolve, 100))
			return res.data
		} catch (err) {
			console.log(err)
			throw err
		} finally {
			loadingStore.stopLoading()
		}
	}

	async function startBatch(path: string, body?: Record<string, any> | null) {
		console.log('body 요청보낼 데이터:', body)
		try {
			const res = await api.post(path, body)
			console.log(res.data)
			return res.data
		} catch (err) {
			console.log('저장 실패', err)
			throw err
		} finally {
		}
	}

	return { downloadStart, saveBody, saveParams, uploadFile, startBatch }
}
