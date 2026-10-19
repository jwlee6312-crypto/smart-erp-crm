/** 날짜 포맷이 필요한 필드 목록 */
export const dateFields = ['ordymd', 'outymd', 'fromdt', 'todt', 'ioymd']

/** 8자리 문자열을 yyyy-MM-dd 형식으로 변환 */
export function formatDateString(value: string): string {
	if (typeof value === 'string' && value.length === 8) {
		return `${value.slice(0, 4)}-${value.slice(4, 6)}-${value.slice(6, 8)}`
	}
	return value
}

/** yyyy-MM-dd 형식을 8자리 문자열로 변환 */
export function unformatDateString(value: string): string {
	if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(value)) {
		return value.replace(/-/g, '')
	}
	return value
}
