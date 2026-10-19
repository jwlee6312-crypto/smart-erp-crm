import { defineStore } from 'pinia'

export const useSearchStore = defineStore('search', {
	state: () => ({
		savedTabs: {} as Record<string, { path: string; data: Record<string, any> | null }>,
	}),
	actions: {
		saveTab(tabId: string, path: string, data: Record<string, any> | null) {
			this.savedTabs[tabId] = { path, data }
		},
		getTabData(tabId: string) {
			return this.savedTabs[tabId] || null
		},
		removeTab(tabId: string) {
			delete this.savedTabs[tabId]
		},
		resetAll() {
			this.savedTabs = {}
		},
	},
})
