<template>
  <nav class="navbar navbar-expand bg-color shadow-sm px-4">
    <!-- 로고 영역 -->
    <div class="logo-container" @click="handleLogoClick">
      <div class="smart-core-logo">
        <span class="text-smart">Smart</span><span class="text-core">Core</span>
      </div>
      <div class="logo-subtitle">AI + ERP + CRM Integration</div>
    </div>

    <!-- 상단 대메뉴 -->
    <ul class="navbar-nav ms-5 me-auto custom-menu">
      <li v-for="menu in headerMenus" :key="menu.codecd" class="nav-item">
        <a
          class="nav-link"
          :class="{ active: menuStore.activeCodecd === menu.codecd }"
          href="javascript:void(0)"
          @click="selectTopMenu(menu.codecd)"
        >
          {{ menu.codenm }}
        </a>
      </li>
    </ul>

    <!-- 🚀 상단 액션 영역 (도움말 패널, 전체닫기) -->
    <div class="d-flex align-items-center gap-2 pe-3">
      <!-- 1. 전체닫기 버튼 -->
      <button class="btn-navbar-tool" @click="closeAllTabs" title="모든 탭 닫기">
        <i class="bi bi-x-square me-1"></i>전체닫기
      </button>

      <!-- 2. 도움말 버튼 (우측 패널 열기 방식) -->
      <button class="btn-navbar-tool btn-help-highlight" @click="openManualPanel" title="현재 화면 도움말 열기">
        <i class="bi bi-question-circle me-1"></i>도움말
      </button>

      <div class="vr mx-2 bg-white opacity-25" style="height: 20px;"></div>

      <!-- 3. 로그아웃 버튼 -->
      <button class="btn-logout-custom" @click="logout">
        <i class="bi bi-power"></i>
        <span>로그아웃</span>
      </button>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useMenuStore } from '@/stores/menuStore'
import { useAuthStore } from '@/stores/authStore'
import { useTabStore } from '@/stores/tabStore'
import { useManualStore } from '@/stores/manualStore'

const menuStore = useMenuStore()
const authStore = useAuthStore()
const tabStore = useTabStore()
const manualStore = useManualStore()
const router = useRouter()

const headerMenus = computed(() => {
  if (!menuStore.topMenuItems) return []
  return menuStore.topMenuItems.map((item: any) => ({
    codecd: item.codecd,
    codenm: item.codenm
  }))
})

const selectTopMenu = async (codecd: string) => { await menuStore.selectTopMenu(codecd) }
const handleLogoClick = () => { router.push('/') }
const logout = async () => { await authStore.logout() }

/** 🚀 모든 탭 닫기 (상단 및 좌측 메뉴 상태 유지) */
const closeAllTabs = () => {
  if (confirm('열려있는 모든 탭을 닫으시겠습니까?')) {
    // 💡 window.location.href 리로드 대신 tabStore.closeAllTabs()만 호출하여 메뉴 상태를 기온 상태로 유지
    tabStore.closeAllTabs()
  }
}

/** 🚀 우측 패널 도움말 열기 */
const openManualPanel = () => {
  const activeTab = tabStore.activeTab
  if (!activeTab) return alert('도움말을 보려면 프로그램을 먼저 선택하세요.')
  manualStore.open(activeTab.pgmId)
}
</script>

<style scoped>
.bg-color { background: #005a9f; height: 65px; border-bottom: 2px solid #000000; }
.logo-container { width: 180px; display: flex; flex-direction: column; cursor: pointer; }
.smart-core-logo { font-size: 1.6rem; font-weight: 800; line-height: 1; }
.text-smart { color: #ffffff; }
.text-core { color: #ffc107; }
.logo-subtitle { font-size: 0.55rem; color: rgba(255, 255, 255, 0.7); margin-top: 2px; font-weight: 600; }
.custom-menu .nav-link { color: rgba(255, 255, 255, 0.8) !important; font-size: 1rem; font-weight: 600; padding: 0.5rem 1.5rem !important; }
.custom-menu .nav-link:hover, .custom-menu .nav-link.active { color: #ffffff !important; }

.btn-navbar-tool {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  font-size: 0.8rem;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-navbar-tool:hover { background: rgba(255, 255, 255, 0.2); border-color: #ffc107; }

.btn-help-highlight { color: #ffc107; border-color: rgba(255, 193, 7, 0.3); }
.btn-help-highlight:hover { border-color: #ffc107; background: rgba(255, 193, 7, 0.1); }

.btn-logout-custom { display: flex; align-items: center; gap: 8px; background: transparent; border: 1px solid #ffc107; color: #ffc107; padding: 6px 15px; border-radius: 20px; font-size: 0.85rem; font-weight: 700; cursor: pointer; }
.btn-logout-custom:hover { background: #ffc107; color: #005a9f; }
</style>
