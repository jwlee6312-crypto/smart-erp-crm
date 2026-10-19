export function useEmailCheck() {
	function emailCheck(emailString: string) {
		if (!emailString) {
			alert('메일이 없습니다.')
			return false
		}

		const trimmed = emailString.trim()

		// 공백으로 구분 한 경우 경우
		// 예: abc@naver.com abc@gmail.com → @뒤에 도메인 끝과 바로 다음 이메일이 붙은 형태
		if (/\s/.test(trimmed)) {
			alert('이메일 사이에는 반드시 콤마(,)로 구분해주세요. 공백은 허용되지 않습니다.')
			return false
		}

		// 콤마 없이 이메일이 연속된 경우 감지
		// 예: abc@naver.comabc@gmail.com → @뒤에 도메인 끝과 바로 다음 이메일이 붙은 형태
		const consecutiveEmailPattern = /@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}@/
		if (consecutiveEmailPattern.test(trimmed)) {
			alert('이메일 주소 사이에는 반드시 콤마(,)로 구분해주세요.')
			return false
		}
	}

	return {
		emailCheck,
	}
}
