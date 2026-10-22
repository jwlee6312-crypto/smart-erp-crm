<template>
  <div class="side-bar shadow-sm" :class="{ 'is-collapsed': isCollapsed }">
    <!-- 👤 유저 프로필 카드 (사진 표시 및 세션 정보) -->
    <div v-if="!isCollapsed" class="profile-card">
      <div class="user-details text-center">
        <div class="avatar-area mb-2">
          <div class="avatar-placeholder cursor-pointer" @click="goToProfile" title="개인정보 관리로 이동">
            <img
              v-if="profileImageSrc && !imageLoadError"
              :src="profileImageSrc"
              class="profile-img"
              alt="Profile"
              @error="handleImageError"
            />
            <i v-else class="bi bi-person-fill"></i>
          </div>
        </div>
        <div class="user-info-text">
          <div class="user-name-info fw-bolder text-dark">
            {{ authStore.usernm }}
            <span class="badge bg-primary text-white ms-1" style="font-size: 11px;">내선:{{ authStore.inner_no || '-' }}</span>
          </div>
          <div v-if="authStore.email" class="user-email-text fw-bold text-secondary mt-1">
            {{ authStore.email }}
          </div>
        </div>
      </div>
    </div>

    <!-- 📋 메뉴 리스트 (우측 pgmid 제거, 마우스 오버 :title 툴팁만 보존) -->
    <div id="accordionMenu" class="menu-list">
      <div v-for="group in groupedItems" :key="group.grpcd" class="w-100">
        <a
          class="nav-link group-title"
          :class="{ 'collapsed': !isGroupOpen(group.grpcd) }"
          href="javascript:void(0)"
          @click="toggleGroup(group.grpcd)"
        >
          <i class="bi bi-folder2-open me-2 text-primary"></i>
          <span v-if="!isCollapsed">{{ group.grpnm }}</span>
        </a>

        <div class="collapse-content" v-show="isGroupOpen(group.grpcd) && !isCollapsed">
          <nav class="nav d-flex flex-column">
            <a
              v-for="item in group.items"
              :key="item.pgmid"
              class="sb-nav-link"
              :class="{ 'is-active': tabStore.activeTab?.pgmId === item.pgmid }"
              href="javascript:void(0)"
              :title="`[${item.pgmid}] ${item.pgmnm}`"
              @click="goPage(item.pgmid, item.pgmnm, item.grpcd)"
            >
              <i class="bi bi-chevron-right sub-icon"></i>
              <span>{{ item.pgmnm }}</span>
            </a>
          </nav>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useMenuStore } from '@/stores/menuStore'
import { useTabStore } from '@/stores/tabStore'
import { useAuthStore } from '@/stores/authStore'
import { addDynamicRoute } from '@/router/dynamicRoute'

const props = defineProps({
  isCollapsed: Boolean
})

const authStore = useAuthStore()
const menuStore = useMenuStore()
const tabStore = useTabStore()
const openGroupId = ref<string | null>(null)
const imageLoadError = ref(false)

// 🚀 프로필 이미지 경로 계산
const profileImageSrc = computed(() => {
  if (!authStore.photo_path) return ''

  const path = authStore.photo_path.trim()

  // 절대 경로 형태(http, data:)는 그대로 반환
  if (path.startsWith('http') || path.startsWith('data:')) {
    return path
  }

  // DB에 이미 회사코드나 하위 폴더 구조가 포함되어 저장된 경우 (예: 'COIT/profile/image.jpg')
  if (path.includes('/')) {
    const cleanPath = path.replace(/^\/?(api\/)?(storage\/)?/, '')
    return `/api/storage/${cleanPath}`
  }

  // DB에 오직 순수 파일명만 저장되어 있는 경우 (예: 'image.jpg')
  const cmpycd = (authStore.cmpycd || 'COIT').toUpperCase()
  return `/api/storage/${cmpycd}/profile/${path}`
})

watch(() => authStore.photo_path, () => {
  imageLoadError.value = false
})

function handleImageError() {
  imageLoadError.value = true
}

const isGroupOpen = (grpcd: string) => openGroupId.value === grpcd
const toggleGroup = (grpcd: string) => {
  openGroupId.value = openGroupId.value === grpcd ? null : grpcd
}

const groupedItems = computed(() => menuStore.groupedSidebarItems)

function goToProfile() {
  const pgmId = 'HABA910U';
  const pgmNm = '개인정보 관리';
  const grpCd = '900';
  addDynamicRoute(pgmId, pgmNm, grpCd);
  tabStore.addTab({ pgmId: pgmId, pgmNm: pgmNm, path: `/${pgmId}` });
}

function goPage(pgmid: string, pgmnm: string, grpcd: string) {
  addDynamicRoute(pgmid, pgmnm, grpcd)
  tabStore.addTab({ pgmId: pgmid, pgmNm: pgmnm, path: `/${pgmid}` })
}
</script>

<style scoped>
.side-bar { background-color: #fff; border-right: 1px solid #dcdfe6; display: flex; flex-direction: column; height: 100%; overflow: hidden; }
.profile-card { padding: 20px 10px; border-bottom: 1px solid #ebeef5; flex-shrink: 0; background: linear-gradient(180deg, #f8f9fa 0%, #ffffff 100%); }

.avatar-placeholder {
  width: 70px;
  height: 70px;
  background-color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 38px;
  color: #005a9f;
  margin: 0 auto;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  border: 2px solid #fff;
  overflow: hidden;
}

.profile-img { width: 100%; height: 100%; object-fit: cover; }
.user-name-info { font-weight: 800; color: #1a202c; font-size: 15px; margin-top: 12px; line-height: 1.2; }
.user-email-text { font-size: 11px; color: #718096; margin-top: 4px; word-break: break-all; }

.menu-list { flex: 1; overflow-y: auto; }
.group-title { display: flex; align-items: center; padding: 9px 15px; font-size: 13px; font-weight: 700; color: #2d3748; text-decoration: none; }
.sb-nav-link { padding: 6px 15px 6px 35px; font-size: 12.5px; color: #4a5568; text-decoration: none; display: flex; align-items: center; border-left: 3px solid transparent; }
.sb-nav-link:hover { background-color: #f7fafc; color: #3182ce; }
.sb-nav-link.is-active { background-color: #ebf8ff; color: #2b6cb0; font-weight: bold; border-left-color: #3182ce; }
.sub-icon { font-size: 8px; margin-right: 10px; opacity: 0.5; }
</style>
