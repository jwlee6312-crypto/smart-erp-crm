import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

export interface Tab {
  pgmId: string      // 실제 프로그램 ID (컴포넌트명)
  pgmNm: string      // 탭 표시 이름
  path: string       // 유니크 경로 (쿼리스트링 포함)
}

export const useTabStore = defineStore('tab', () => {
  const router = useRouter()
  const tabs = ref<Tab[]>([])
  const activeTabId = ref<string>('') // 현재 활성화된 탭의 'path'

  // 💡 현재 활성 탭의 객체 반환
  const activeTab = computed(() => tabs.value.find(t => t.path === activeTabId.value))

  // 탭 추가
  const addTab = (tab: Tab) => {
    try {
      // 💡 path 기준 중복 체크 (동일 프로그램이라도 파라미터가 다르면 새 탭)
      const index = tabs.value.findIndex(t => t.path === tab.path)
      if (index === -1) {
        tabs.value.push(tab)
      }
      activeTabId.value = tab.path
      router.push(tab.path).catch(err => {
        if (err.name !== 'NavigationDuplicated') console.error('탭 이동 실패:', err)
      })
    } catch (e) {
      console.error('탭 추가 중 오류:', e)
    }
  }

  // 탭 닫기
  const closeTab = (path: string) => {
    const index = tabs.value.findIndex(t => t.path === path)
    if (index === -1) return

    const isRemovingActive = activeTabId.value === path
    tabs.value.splice(index, 1)

    if (isRemovingActive) {
      if (tabs.value.length > 0) {
        const nextTab = tabs.value[index] || tabs.value[index - 1]
        activeTabId.value = nextTab.path
        router.replace(nextTab.path)
      } else {
        activeTabId.value = ''
        router.replace('/')
      }
    }
  }

  // 탭 전체 닫기
  const closeAllTabs = () => {
    tabs.value = []
    activeTabId.value = ''
    router.replace('/')
  }

  // 다른 탭 닫기
  const closeOtherTabs = (path: string) => {
    const targetTab = tabs.value.find(t => t.path === path)
    if (targetTab) {
      tabs.value = [targetTab]
      activeTabId.value = path
      router.push(targetTab.path)
    }
  }

  // 탭 선택
  const selectTab = (tab: Tab) => {
    activeTabId.value = tab.path
    router.push(tab.path).catch(err => {
      if (err.name !== 'NavigationDuplicated') console.error('탭 선택 실패:', err)
    })
  }

  return { tabs, activeTabId, activeTab, addTab, closeTab, selectTab, closeAllTabs, closeOtherTabs }
})
