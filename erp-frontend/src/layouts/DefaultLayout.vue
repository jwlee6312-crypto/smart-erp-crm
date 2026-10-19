<template>
  <div class="erp-layout-root">
    <!-- 1. 상단 네비바 -->
    <header class="erp-header">
      <TopNavbar />
    </header>

    <div class="erp-main-wrapper">
      <!-- 2. 좌측 사이드바 -->
      <aside class="erp-sidebar" :class="{ 'is-collapsed': isSidebarCollapsed }">
        <div class="sidebar-scroll-area">
          <SideMenu :is-collapsed="isSidebarCollapsed" @toggle="toggleSidebar" />
        </div>
      </aside>

      <!-- 3. 본문 영역 (탭 + 컨텐츠) -->
      <main class="erp-content" style="position: relative;">
        <!-- 💡 탭 바 (전체닫기 및 도움말 버튼 제거로 탭 공간 최대 확보) -->
        <nav class="erp-tab-bar custom-scrollbar">
          <div
            v-for="tab in tabStore.tabs"
            :key="tab.path"
            class="erp-tab-item"
            :class="{ 'is-active': tabStore.activeTabId === tab.path }"
            @click="tabStore.selectTab(tab)"
            @contextmenu.prevent="tabStore.closeOtherTabs(tab.path)"
          >
            <i class="bi bi-file-earmark-text me-2 tab-icon"></i>
            <span class="tab-title">{{ tab.pgmNm }}</span>
            <span class="tab-close" @click.stop="tabStore.closeTab(tab.path)">
              <i class="bi bi-x-lg"></i>
            </span>
          </div>
        </nav>

        <!-- 3-2. 프로그램 컨텐츠 영역 -->
        <section class="erp-page-container">
          <router-view v-slot="{ Component, route }">
            <keep-alive :include="tabStore.tabs.map(t => t.pgmId)">
              <component :is="Component" :key="route.fullPath" />
            </keep-alive>
          </router-view>
        </section>
      </main>
    </div>

    <!-- 로딩 오버레이 -->
    <div v-if="loadingStore.isLoading" class="erp-loading">
      <AppSpinner />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import TopNavbar from '@/components/TopNavbar.vue'
import SideMenu from '@/components/SideMenu.vue'
import { useTabStore } from '@/stores/tabStore'
import { useLoadingStore } from '@/stores/loadingStore'
import AppSpinner from '@/components/AppSpinner.vue'

const tabStore = useTabStore()
const loadingStore = useLoadingStore()
const isSidebarCollapsed = ref(false)

const toggleSidebar = () => { isSidebarCollapsed.value = !isSidebarCollapsed.value }
</script>

<style scoped>
.erp-layout-root { display: flex; flex-direction: column; height: 100vh; width: 100vw; overflow: hidden; background-color: #f0f2f5; }
.erp-header { height: 65px; flex-shrink: 0; z-index: 10001; position: relative; }
.erp-main-wrapper { display: flex; flex: 1; height: calc(100vh - 65px); overflow: hidden; }
.erp-sidebar { width: 240px; background: #fff; border-right: 1px solid #dcdfe6; transition: width 0.3s; flex-shrink: 0; display: flex; flex-direction: column; height: 100%; overflow: hidden; }
.erp-sidebar.is-collapsed { width: 64px; }
.sidebar-scroll-area { flex: 1; overflow-y: auto; overflow-x: hidden; }
.erp-content { flex: 1; display: flex; flex-direction: column; overflow: hidden; min-width: 0; }
.erp-tab-bar { height: 40px; background: #fff; border-bottom: 1px solid #e4e7ed; display: flex; align-items: flex-end; padding-left: 10px; flex-shrink: 0; gap: 2px; overflow-x: auto; overflow-y: hidden; white-space: nowrap; }
.erp-page-container { flex: 1; overflow: hidden; background-color: #fff; display: flex; flex-direction: column; }
.erp-tab-item { height: 34px; padding: 0 15px; background: #f5f7fa; border: 1px solid #e4e7ed; border-bottom: none; border-top-left-radius: 6px; border-top-right-radius: 6px; display: flex; align-items: center; font-size: 13px; cursor: pointer; color: #606266; font-weight: 700; flex-shrink: 0; }
.erp-tab-item.is-active { background: #fff; color: #005a9f; font-weight: 800; height: 36px; border-top: 3px solid #005a9f; }
.erp-loading { position: fixed; inset: 0; background: rgba(255,255,255,0.7); display: flex; justify-content: center; align-items: center; z-index: 9999; }
</style>
