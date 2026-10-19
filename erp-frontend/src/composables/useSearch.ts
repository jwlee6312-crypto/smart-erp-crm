// 작성일자  : 25.04.22
// 작성자    : 이현준
// 수정일자  : 25.11.07
// 수정자    : 이현준
// 수정 내용 : 배치 조회 로그 삭제

import { api } from '@/utils/axios' // ✅ axios 인스턴스 사용
import type { Ref } from 'vue'
import { type AxiosResponse } from 'axios'
import { useLoadingStore } from '@/stores/loadingStore'

// ✅ 검색
export function useSearch() {
	const loadingStore = useLoadingStore()
	// 기본적인 get요청 조회함수
	async function searchStart(path: string, params: { [key: string]: string } | null) {
		try {
			loadingStore.startLoading() // 스피너 시작
			console.log('🔍 axios 인풋:', params)
			const res = await api.get(path, { params })
			console.log('🔍 axios 결과:', res.data)
			await new Promise((resolve) => setTimeout(resolve, 80))
			return res.data
		} catch (err: any) {
			throw err
		} finally {
			loadingStore.stopLoading() // 스피너 종료
		}
	}

	async function searchNoSpinner(path: string, params: { [key: string]: string } | null) {
		try {
			const res = await api.get(path, { params })
			return res.data
		} catch (err: any) {
			throw err
		}
	}

	// 마감월 조회
	async function searchCloseYm() {
		try {
			const res = await api.get('/common/getCloseYm')
			return res.data
		} catch (err: any) {
			throw err
		}
	}

	async function searchStartImage(path: string): Promise<Blob | undefined> {
		try {
			const res: AxiosResponse<Blob> = await api.get(path, {
				responseType: 'blob',
			})
			return res.data
		} catch (err) {
			console.error('이미지 조회 실패:', err)
			return undefined
		}
	}

	async function searchBatch(path: string, params: { [key: string]: string } | null) {
		try {
			const res = await api.get(path, { params })
			return res.data
		} catch (err: any) {
			throw err
		} finally {
		}
	}

	return { searchStart, searchStartImage, searchCloseYm, searchBatch, searchNoSpinner }
}

// ✅ 팝업 전용 검색
export function popupSearch<T extends Record<string, any>>(
	path: Ref<string>,
	vAlert: (message: string) => void,
	vAlertError: (message: string) => void
) {
	async function searchStart(params: Record<string, string>): Promise<T[] | undefined> {
		try {
			console.log('🔍 axios 인풋:', params)
			console.log('🔍 axios 경로:', path.value)

			const res = await api.get<T[]>(path.value, { params })
			console.log('팝업조회결과: ', res)
			vAlert('조회 성공')
			return res.data
		} catch (err) {
			vAlertError('조회 실패')
			console.error('조회 실패:', err)
			return undefined
		}
	}

	return { searchStart}
}
