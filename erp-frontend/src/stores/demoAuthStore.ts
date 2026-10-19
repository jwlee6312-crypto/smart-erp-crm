import { defineStore } from 'pinia'
import { api } from '@/utils/axios'

/**
 * 📱 모바일 데모 전용 독립 인증 저장소
 * 웹 세션과 별개로 데모 내에서만 유지되는 사용자 정보를 관리합니다.
 */
export const useDemoAuthStore = defineStore('demoAuth', {
  state: () => ({
    cmpycd: '',
    userid: '',
    usernm: '',
    isLoggedIn: false,
    token: null as string | null
  }),

  actions: {
    /** 🚀 모바일 전용 로그인 시뮬레이션 */
    async login(cmpycd: string, userid: string, passwd: string) {
      try {
        const res = await api.post('/comm/login', { cmpycd, userid, passwd })
        if (res.data) {
          const data = res.data
          this.cmpycd = data.cmpycd
          this.userid = data.userid
          this.usernm = data.usernm
          this.isLoggedIn = true
          return true
        }
        return false
      } catch (e) {
        console.error('Demo Login Error:', e)
        throw e
      }
    },

    /** 🚀 데모 상태 초기화 */
    reset() {
      this.cmpycd = ''
      this.userid = ''
      this.usernm = ''
      this.isLoggedIn = false
    }
  }
})
