<template>
  <div v-if="isVisible" class="mobile-simulator-overlay" @click.self="close">
    <div class="phone-mockup">
      <!-- 📱 폰 물리 버튼 모사 -->
      <div class="phone-speaker"></div>
      <div class="phone-screen">
        <div class="status-bar d-flex justify-content-between px-3 py-1">
          <span class="time">{{ currentTime }}</span>
          <div class="icons gap-1 d-flex">
            <i class="bi bi-wifi"></i>
            <i class="bi bi-reception-4"></i>
            <i class="bi bi-battery-full"></i>
          </div>
        </div>

        <!-- 🚀 내부 컨텐츠 영역 (라우터 뷰) -->
        <div class="content-viewport">
          <router-view v-slot="{ Component }">
            <transition name="slide">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </div>
      <div class="phone-home-btn" @click="goHome"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps<{ isVisible: boolean }>()
const emit = defineEmits(['close'])
const router = useRouter()

const currentTime = ref('')

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.getHours().toString().padStart(2, '0') + ':' + now.getMinutes().toString().padStart(2, '0')
}

let timer: any = null
onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 60000)
})
onUnmounted(() => clearInterval(timer))

const close = () => emit('close')
const goHome = () => router.push({ name: 'MobileDemoMain' })

</script>

<style scoped>
.mobile-simulator-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.7); z-index: 9999;
  display: flex; align-items: center; justify-content: center;
}

.phone-mockup {
  width: 340px; height: 680px;
  background: #111; border: 8px solid #444; border-radius: 40px;
  position: relative; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.5);
  display: flex; flex-direction: column; padding: 10px;
}

.phone-speaker {
  width: 60px; height: 4px; background: #333; border-radius: 2px;
  margin: 15px auto 10px;
}

.phone-screen {
  flex-grow: 1; background: #fff; border-radius: 20px;
  overflow: hidden; position: relative; display: flex; flex-direction: column;
}

.status-bar { background: #fff; font-size: 11px; font-weight: bold; color: #333; z-index: 100; }

.content-viewport { flex-grow: 1; position: relative; overflow-y: auto; background: #f8f9fa; }

.phone-home-btn {
  width: 45px; height: 45px; border: 2px solid #333; border-radius: 50%;
  margin: 10px auto; cursor: pointer; transition: background 0.2s;
}
.phone-home-btn:hover { background: rgba(255,255,255,0.1); }

/* 애니메이션 */
.slide-enter-active, .slide-leave-active { transition: all 0.3s ease-out; position: absolute; width: 100%; }
.slide-enter-from { transform: translateX(100%); opacity: 0; }
.slide-leave-to { transform: translateX(-100%); opacity: 0; }
</style>
