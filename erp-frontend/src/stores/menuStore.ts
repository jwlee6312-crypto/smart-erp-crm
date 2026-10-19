import { defineStore } from 'pinia'
import { api } from '@/utils/axios'

interface MenuState {
	topMenuItems: any[]
	sidebarItems: any[]
	activeCodecd: string
}

export const useMenuStore = defineStore('menu', {
	state: (): MenuState => ({
		topMenuItems: [],
		sidebarItems: [],
		activeCodecd: '',
	}),
	actions: {
		/**
		 * 💡 상단 메뉴(대분류) 조회
		 */
		async fetchTopMenus() {
			try {
				const res = await api.get('/comm/top-menus')
				this.topMenuItems = res.data
				if (this.topMenuItems.length > 0 && !this.activeCodecd) {
					await this.selectTopMenu(this.topMenuItems[0].codecd)
				}
			} catch (e) { console.error('❌ [상단 메뉴 로드 실패]:', e) }
		},

		/**
		 * 💡 좌측 메뉴 조회 및 ID 정규화 (언더바/하이픈 제거)
		 */
		async selectTopMenu(upmucd: string) {
			this.activeCodecd = upmucd
			try {
				// 🚀 HA00_200S_STR 프로시저를 호출하여 좌측 메뉴 로드
				const res = await api.post('/comm/HA00_200S_STR', { upmucd: upmucd })

				// DB의 'HAAA_010U.asp' 형식을 'HAAA010U'로 변환하여 Vue와 일치시킴
				this.sidebarItems = res.data.map((item: any) => ({
					...item,
					pgmid: item.pgmid ? item.pgmid.replace(/[_\-\.]|asp/gi, '').toUpperCase() : ''
				}))
			} catch (e) { console.error('❌ [좌측 메뉴 로드 실패]:', e) }
		},

		/**
		 * 💡 [복구] authStore에서 호출하는 메뉴 초기화 함수
		 */
		async fetchMenus() {
			await this.fetchTopMenus()
		},

		setActiveCodecd(codecd: string) { this.activeCodecd = codecd },
		setSidebarItems(items: any[]) { this.sidebarItems = items },
		setHeaderMenus(items: any[]) { this.topMenuItems = items },

		clearMenus() {
			this.topMenuItems = []; this.sidebarItems = []; this.activeCodecd = ''
		}
	},
	getters: {
		groupedSidebarItems: (state) => {
			const map = new Map()
			for (const item of state.sidebarItems) {
				const grpcd = item.grpcd
				if (!map.has(grpcd)) {
					map.set(grpcd, { grpcd: grpcd, grpnm: item.grpnm, items: [] })
				}
				map.get(grpcd).items.push(item)
			}
			return Array.from(map.values())
		}
	},
})
