import 'core-js/es/promise'
import 'regenerator-runtime/runtime'
import { createApp, nextTick } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import App from './App.vue'
import router from './router'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap'
import 'bootstrap-icons/font/bootstrap-icons.css'
import 'tabulator-tables/dist/css/tabulator_bootstrap3.min.css'
import './assets/global.css'
import './assets/table.css'
import './assets/tabulator.css'
import { registerSW } from 'virtual:pwa-register'

import.meta.hot?.on('vite:beforeUpdate', () => {
	window.location.reload()
})

registerSW({
	immediate: true, // 🔥 첫 로드시 SW 바로 등록 (옵션)
})

const app = createApp(App)
app.config.globalProperties.$api_url = import.meta.env.VITE_API_URL
export const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
app.use(pinia)

nextTick().then(() => {
	app.use(router)
	app.mount('#app')
})