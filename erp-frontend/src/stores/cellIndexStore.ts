import { defineStore } from 'pinia'

export const useCellIndexStore = defineStore('cellIndex', {
	state: () => ({
		rowIndex: false as number | boolean,
		colIndex: false as number | boolean, // ✅ 열 인덱스 추가
	}),
	actions: {
		setRowIndex(row: number) {
			this.rowIndex = row
		},
		setColIndex(col: number) {
			this.colIndex = col
		},
		reset() {
			this.rowIndex = false
			this.colIndex = false
		},
	},
})
