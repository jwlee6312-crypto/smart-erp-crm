// form 값 초기화
export function useFormReset<T extends Record<string, any>>() {
	function resetForm(form: T) {
		for (const key in form) {
			if (Object.prototype.hasOwnProperty.call(form, key)) {
				const value = form[key]
				if (typeof value === 'string') {
					form[key] = '' as T[typeof key]
				}
				if (typeof value === 'number') {
					form[key] = 0 as T[typeof key]
				}
				if (typeof value === 'boolean') {
					form[key] = false as T[typeof key]
				}
			}
		}
	}

	return {
		resetForm,
	}
}
