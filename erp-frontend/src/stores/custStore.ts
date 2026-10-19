// stores/custStore.ts
import { defineStore } from 'pinia'

export const useCustStore = defineStore('cust', {
	state: () => ({
		custcd: '',
	}),
	actions: {
		setCustcd(code: string) {
			this.custcd = code
		},
		reset() {
			this.custcd = ''
		},
	},
})
