// 프로그램명	  : -
// 프로그램 ID	  : -
// 작성일자	    : 25.07.31
// 작성자	      : 이현준
// 수정일자      : 25.08.26
// 수정자	      : 이현준
// 수정 내용     : 주석 추가

import { computed } from 'vue'

// 값을 서버에서 받아올 때만 사용
export function formatNumber(value: number): string {
	if (!value) return '0'
	return Number(value).toLocaleString('ko-KR')
}

// 값을 서버에서 받아올 때와 직접 값을 입력해야할 때 사용
export function formatInputNumber<T extends Record<string, string | number>>(
	reactiveObj: T,
	fieldName: keyof T
) {
	return computed({
		get: () => formatNumber(reactiveObj[fieldName] as number),
		set: (val: string) => {
			const raw = val.replace(/,/g, '')
			reactiveObj[fieldName] = (raw === '' ? 0 : Number(raw)) as T[keyof T]
		},
	})
}
