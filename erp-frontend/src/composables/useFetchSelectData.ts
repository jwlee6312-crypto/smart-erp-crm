import { api } from '@/utils/axios'
import type { AxiosResponse } from 'axios'
import { useAuthStore } from '@/stores/authStore'

export interface SelectData {
	CODECD: string
	CODENM: string
}

/**
 * 🅰️ ERP (MSSQL) 공통코드 조회
 */
export async function fetchSelectData(CDTYPE: string): Promise<SelectData[]> {
	return fetchSelectDataList<SelectData>('/code', { CDTYPE })
}

/**
 * 🅱️ CRM (MSSQL 통합) 공통코드 조회
 */
export async function fetchCrmSelectData(CDTYPE: string): Promise<SelectData[]> {
	return fetchSelectDataList<SelectData>('/crm/outbound/code', { CDTYPE })
}

/**
 * 범용 셀렉트 데이터 조회
 */
export async function fetchSelectDataList<T>(url: string, params?: any): Promise<T[]> {
	try {
		const authStore = useAuthStore()

		// 🚀 Mybatis #{cmpycd} 바인딩을 위해 회사코드 필수 포함
		const requestParams = {
			...params,
			cmpycd: authStore.cmpycd
		}

		const res: AxiosResponse<any> = await api.get(url, {
			params: requestParams,
		})

		// 💡 axios.ts 인터셉터에서 이미 'data' 껍데기를 벗기고 키를 소문자로 변환함
		// 💡 따라서 res.data는 이미 순수 데이터 배열임
		return Array.isArray(res.data) ? res.data : []
	} catch (e) {
		console.error(`[fetchSelectDataList] ${url} 조회 실패:`, e)
		return []
	}
}

/**
 * 유저 정보 조회
 */
export async function fetchUserData(): Promise<any[]> {
	return fetchSelectDataList<any>('/user', {})
}

/**
 * 🚀 생산라인 정보 조회 (HP00_000S_STR 연동)
 */
export interface SelectPdLineData {
	linecd: string;
	linenm: string;
}

export async function fetchLineData(): Promise<SelectPdLineData[]> {
	return fetchSelectDataList<SelectPdLineData>('/hp00/HP00_000S_STR', { gubun: 'L0' })
}

/**
 * 🚀 생산공정 정보 조회 (HP00_000S_STR 연동)
 */
export interface SelectPdProgData {
	progcd: string;
	prognm: string;
}

export async function fetchProgData(linecd: string): Promise<SelectPdProgData[]> {
	return fetchSelectDataList<SelectPdProgData>('/hp00/HP00_000S_STR', { gubun: 'G0', gbncd: linecd })
}
