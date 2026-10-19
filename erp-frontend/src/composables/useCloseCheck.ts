import { useSearch } from '@/composables/useSearch'

// 마감년월 조회하는 컴포저블 함수
export async function useCloseCheck() {
	const { searchCloseYm } = useSearch()

	const res = await searchCloseYm()
	console.log('마감년월: ', res)

	const closeYM = String(res) // "202506"

	const year = closeYM.substring(0, 4)
	const month = closeYM.substring(4, 6)

	return {
		closeYM,
		year,
		month,
	}
}
