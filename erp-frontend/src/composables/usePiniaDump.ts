// utils/piniaDump.ts
import { pinia } from '@/main'

export function getAllPiniaState() {
	// 깊은 복사해서 순수 객체로 (콘솔/alert 보기 좋게)
	return JSON.parse(JSON.stringify(pinia.state.value))
}

export function logAllPiniaState() {
	const dump = getAllPiniaState()
	console.table(dump)
	console.log('Pinia dump:', dump)
}
