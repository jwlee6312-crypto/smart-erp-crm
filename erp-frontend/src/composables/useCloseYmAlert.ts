import { type Ref, watch } from 'vue'
import { useAlerts } from '@/composables/useAlerts'
import { useCloseCheck } from '@/composables/useCloseCheck'

export function useCloseYmAlert(target: Ref<string>) {
	const { vAlertError } = useAlerts()

	watch(target, async (inputDate) => {
		console.log('체크: ', inputDate)
		if (inputDate.length === 7) {
			inputDate = inputDate + '-01'
		}
		if (!inputDate) return

		const changedInputDate = inputDate.replace(/-/g, '')

		const res = await useCloseCheck()
		console.log('마감년월: ', res.closeYM)
		if (changedInputDate < res.closeYM) {
			vAlertError(`현재 마감년월은 ${res.year}년 ${res.month}월 입니다. 마감취소 후 변경해주세요.`) // ✅ alert 대신 호출
			target.value = ''
		}
	})
}
