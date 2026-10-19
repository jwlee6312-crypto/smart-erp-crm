<!--	=============================================================
	프로그램명 : 모바일 전용 메인 런처 (아코디언 기능 추가)
	작성일자	: 2025.02.24
	작성자    : AI Assistant
	설명        : 메뉴판은 세로로, 프로그램은 가로로 - 그룹별 접기/펴기 기능 포함
	=============================================================
-->

<template>
  <div class="mobile-launcher-portrait bg-light min-vh-100">
    <!-- [1] 상단 고정 헤더 -->
    <header class="d-flex justify-content-between align-items-center bg-primary text-white p-3 sticky-top shadow">
      <div class="d-flex align-items-center gap-2">
        <button v-if="viewMode === 'SUB'" class="btn btn-link text-white p-0" @click="goBack">
          <i class="bi bi-chevron-left fs-3"></i>
        </button>
        <span class="fw-bolder fs-5">{{ viewMode === 'TOP' ? 'SmartCore' : selectedTopMenuName }}</span>
      </div>
      <button class="btn btn-link text-white p-0" @click="handleLogout">
        <i class="bi bi-power fs-3"></i>
      </button>
    </header>

    <div class="p-3 pb-5">
      <!-- [2] 1단계: 대메뉴 (세로 리스트) -->
      <template v-if="viewMode === 'TOP'">
        <div class="mb-4 ps-1">
          <div class="fw-bold text-dark fs-5">{{ authStore.usernm }} 님, 반갑습니다.</div>
          <div class="text-muted small">진행하실 업무 분류를 선택하세요.</div>
        </div>

        <div class="row g-3">
          <div v-for="menu in menuStore.topMenuItems" :key="menu.codecd" class="col-12">
            <div
              class="menu-bar-card bg-white p-3 rounded-4 shadow-sm d-flex align-items-center justify-content-between"
              @click="selectTopMenu(menu)"
            >
              <div class="d-flex align-items-center gap-3">
                <div class="icon-box bg-primary bg-opacity-10 text-primary rounded-circle">
                  <i class="bi bi-folder-fill fs-4"></i>
                </div>
                <div class="fw-bold text-dark">{{ menu.codenm }}</div>
              </div>
              <i class="bi bi-chevron-right text-muted"></i>
            </div>
          </div>
        </div>
      </template>

      <!-- [3] 2단계: 하위 프로그램 리스트 (아코디언 적용) -->
      <template v-else>
        <div v-for="group in menuStore.groupedSidebarItems" :key="group.grpcd" class="mb-2">
          <!-- 그룹 헤더 (터치 시 접기/펴기) -->
          <div
            class="group-header-accordion bg-white p-3 rounded-4 shadow-sm d-flex align-items-center justify-content-between mb-1"
            @click="toggleGroup(group.grpcd)"
          >
            <div class="fw-bold text-primary small">
              <i class="bi bi-collection-play me-2"></i>{{ group.grpnm }}
            </div>
            <i class="bi" :class="isExpanded(group.grpcd) ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
          </div>

          <!-- 하위 리스트 (전환 애니메이션 적용) -->
          <transition name="expand">
            <div v-if="isExpanded(group.grpcd)" class="list-group shadow-sm border-0 rounded-4 overflow-hidden mb-3">
              <button
                v-for="prog in group.items"
                :key="prog.pgmid"
                class="list-group-item list-group-item-action p-3 d-flex justify-content-between align-items-center border-0 border-bottom"
                @click="runProgram(prog)"
              >
                <div class="d-flex align-items-center">
                  <i class="bi bi-play-circle text-secondary me-3 fs-5"></i>
                  <span class="fw-bold text-dark">{{ prog.pgmnm }}</span>
                </div>
                <i class="bi bi-chevron-right text-muted small"></i>
              </button>
            </div>
          </transition>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useMenuStore } from '@/stores/menuStore'
import { useAuthStore } from '@/stores/authStore'
import { useTabStore } from '@/stores/tabStore'
import { addDynamicRoute } from '@/router/dynamicRoute'

const menuStore = useMenuStore()
const authStore = useAuthStore()
const tabStore = useTabStore()

const viewMode = ref<'TOP' | 'SUB'>('TOP')
const selectedTopMenuName = ref('')
const expandedGroups = ref<Set<string>>(new Set())

/** 💡 대메뉴 선택 */
const selectTopMenu = async (menu: any) => {
  selectedTopMenuName.value = menu.codenm
  await menuStore.selectTopMenu(menu.codecd)

  // 첫 진입 시 모든 그룹을 펼침 또는 첫 번째 그룹만 펼침 선택 가능
  expandedGroups.value.clear()
  if (menuStore.groupedSidebarItems.length > 0) {
    expandedGroups.value.add(menuStore.groupedSidebarItems[0].grpcd)
  }

  viewMode.value = 'SUB'
}

const goBack = () => { viewMode.value = 'TOP' }

/** 💡 그룹 접기/펴기 토글 */
const toggleGroup = (grpcd: string) => {
  if (expandedGroups.value.has(grpcd)) {
    expandedGroups.value.delete(grpcd)
  } else {
    expandedGroups.value.add(grpcd)
  }
}

const isExpanded = (grpcd: string) => expandedGroups.value.has(grpcd)

const runProgram = (prog: any) => {
  addDynamicRoute(prog.pgmid, prog.pgmnm, prog.grpcd)
  tabStore.addTab({ pgmId: prog.pgmid, pgmNm: prog.pgmnm, path: `/${prog.pgmid}` })
}

const handleLogout = async () => {
  if (confirm('로그아웃 하시겠습니까?')) { await authStore.logout() }
}
</script>

<style scoped>
.mobile-launcher-portrait { font-family: 'Pretendard', sans-serif; letter-spacing: -0.02rem; }
.menu-bar-card { cursor: pointer; transition: background 0.1s; border: 1px solid rgba(0,0,0,0.05); }
.menu-bar-card:active { background-color: #f8f9fa; transform: scale(0.98); }

.icon-box { width: 45px; height: 45px; display: flex; align-items: center; justify-content: center; }

/* 🚀 아코디언 헤더 스타일 */
.group-header-accordion { cursor: pointer; border: 1px solid rgba(0,0,0,0.05); }
.group-header-accordion:active { background-color: #f8f9fa; }

.list-group-item:active { background-color: #f1f3f5; }
.sticky-top { z-index: 1040; }

/* 🚀 펼치기/접기 애니메이션 */
.expand-enter-active, .expand-leave-active {
  transition: all 0.25s ease-out;
  max-height: 500px;
  overflow: hidden;
}
.expand-enter-from, .expand-leave-to {
  max-height: 0;
  opacity: 0;
  margin-bottom: 0;
}
</style>
