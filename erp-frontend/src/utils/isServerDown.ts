// axios 오류 타입 가드 & 분류 유틸
type AxiosLikeError = {
	response?: { status: number; data?: any }
	request?: any
	code?: string // 'ERR_NETWORK' | 'ECONNABORTED' 등
	message?: string
}

export function isServerDown(err: AxiosLikeError) {
	// 응답이 전혀 없고 요청만 존재 → 서버 꺼짐/네트워크 단절 가능성 높음
	if (err.request && !err.response) return true

	// 타임아웃(서버가 오래 응답 안 함)
	if (err.code === 'ECONNABORTED') return true

	// 브라우저 네트워크 오프라인
	if (typeof navigator !== 'undefined' && navigator.onLine === false) return true

	// Axios가 네트워크 자체 실패로 판단한 경우
	if (err.code === 'ERR_NETWORK') return true

	// 일부 환경에서 메시지로만 전달되는 "Network Error"
	if (err.message && /network error/i.test(err.message)) return true

	return false
}
