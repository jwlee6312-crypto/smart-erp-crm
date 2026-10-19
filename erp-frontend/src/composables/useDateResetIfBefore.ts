import { type Ref, watch } from 'vue'
import { useAlerts } from '@/composables/useAlerts'
// baseDate: 바뀌지 않는 비교되는 값이 들어갑니다.
// targetDate: baseDate를 기준으로 날짜를 체크하는 값이 들어갑니다.
export function useDateResetIfBefore(
	baseDate: Ref<string>,
	targetDate: Ref<string>,
	inputName: string
) {
	const { vAlertError } = useAlerts()
	watch(targetDate, async (inputDate) => {
		console.log('기준일 : ', baseDate.value, '변경일 : ', targetDate.value)
		if (!inputDate) return
		if (targetDate.value < baseDate.value) {
			vAlertError(`${inputName} 이전 날짜는 입력할 수 없습니다.`)
			targetDate.value = ''
		}
	})
}
