import { defineStore } from 'pinia'

export const useManualStore = defineStore('manualModal', {
	state: () => ({
		isOpen: false as boolean,
		fileName: null as string | null,
	}),

	actions: {
		open(fileName: string | null) {
			this.fileName = fileName
			this.isOpen = true
		},
		close() {
			this.fileName = null
			this.isOpen = false
		},
	},
})
