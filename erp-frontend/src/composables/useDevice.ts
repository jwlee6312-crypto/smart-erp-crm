import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 💡 기기 감지 유틸리티
 * 화면 너비를 기준으로 모바일 여부를 판별합니다. (768px 미만)
 */
export function useDevice() {
    // 💡 모바일 여부 판단 기준: 화면 너비 1024px 미만 (태블릿 포함)
    const isMobile = ref(window.innerWidth < 1024)

    const updateSize = () => {
        isMobile.value = window.innerWidth < 1024
    }

    onMounted(() => {
        window.addEventListener('resize', updateSize)
        window.addEventListener('orientationchange', updateSize)
    })

    onUnmounted(() => {
        window.removeEventListener('resize', updateSize)
        window.removeEventListener('orientationchange', updateSize)
    })

    return { isMobile }
}

