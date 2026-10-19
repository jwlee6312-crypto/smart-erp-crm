// dateStr이 undefined일 수 있다.
export function formatDateToInput(dateStr?: string) {
	// null, undefined, 빈 문자열 체크
	if (!dateStr) return ''

	// 이미 변환된 형식이면 그대로 반환하는 로직 추가
	if (dateStr.includes('-')) return dateStr

	// dateStr가 8자면 변환한 형식 return
	if (dateStr.length === 8) {
		return `${dateStr.slice(0, 4)}-${dateStr.slice(4, 6)}-${dateStr.slice(6, 8)}`
	}

	// 그 외 기본은 빈 문자열 반환
	return ''
}

export function getDate(): { firstDay: string; today: string; currentMonth: string } {
	const today = new Date()
	const yyyy = today.getFullYear()
	const mm = String(today.getMonth() + 1).padStart(2, '0')
	const dd = String(today.getDate()).padStart(2, '0')

	return {
		firstDay: `${yyyy}-${mm}-01`,
		today: `${yyyy}-${mm}-${dd}`,
		currentMonth: `${yyyy}-${mm}`,
	}
}

// - 제거함수 : deformattingDate : 짝대기 빼는거
// - 추가함수 : formattingDate : 짝대기 넣는거
