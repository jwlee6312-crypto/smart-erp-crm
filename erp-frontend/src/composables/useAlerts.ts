import { ref, nextTick } from 'vue'

let timer: ReturnType<typeof setTimeout> | null = null

// 변수명 유지
const showAlert = ref(false)
const showError = ref(false)
const alertMessage = ref('')

// 내부 헬퍼: 현재 알림을 즉시 종료하고, 새 알림을 강제로 재생성
async function showImmediately(message: string, error: boolean, duration: number) {
	// 1) 진행 중 타이머 정리
	if (timer) {
		clearTimeout(timer)
		timer = null
	}

	// 2) 현재 알림 강제 종료(leave 트랜지션 유도)
	showAlert.value = false
	// DOM이 업데이트되어 leave 클래스가 적용되도록 1틱 기다림
	await nextTick()
	// 짧은 지연으로 enter 재트리거 안정화 (필요시 0~50ms 조절)
	await new Promise((res) => setTimeout(res, 20))

	// 3) 새 알림 세팅 후 즉시 표시(enter 트랜지션 재생)
	showError.value = error
	alertMessage.value = message
	showAlert.value = true

	// 4) duration 후 자동 닫기
	timer = setTimeout(() => {
		showAlert.value = false
		timer = null
	}, duration)
}

// ✅ 알림 호출 함수 (성공)
function vAlert(message: string, duration = 4000) {
	showImmediately(message, false, duration)
}

// ✅ 알림 호출 함수 (에러)
function vAlertError(message: string, duration = 4000) {
	showImmediately(message, true, duration)
}

export function useAlerts() {
	return { vAlert, vAlertError, showAlert, showError, alertMessage }
}
