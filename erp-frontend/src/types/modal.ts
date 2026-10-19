import type { ColumnDefinition } from 'tabulator-tables'

export interface ModalProps {
	type: 'table' | 'grid' | ''
	checked?: boolean
	large?: boolean
	searchDate?: string | null
	defaultField: string
	hasData?: boolean
	title: string
	columns: ColumnDefinition[]
	path: string
	data: Record<string, any>
	onConfirm: (picked: any) => void // ✅ 추가
}