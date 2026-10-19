import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig(({ command }) => ({
	plugins: [
		vue(),
		command === 'serve' ? vueDevTools() : null,
		VitePWA({
			registerType: 'autoUpdate',
			manifest: {
				name: 'SmartCore',
				short_name: 'ERP',
				start_url: '/',
				display: 'standalone',
				theme_color: '#ffffff',
				background_color: '#ffffff',
				icons: [],
			},
			workbox: {
				cleanupOutdatedCaches: true,
				clientsClaim: true,
				skipWaiting: true,
			},
		}),
	].filter(Boolean),
	resolve: {
		alias: {
			'@': fileURLToPath(new URL('./src', import.meta.url)),
		},
	},
	server: {
		host: '0.0.0.0',
		port: 80,
		strictPort: true,
		allowedHosts: true,
		fs: {
			strict: false,
		},
		proxy: {
			'/Upload_Images': {
				target: 'http://127.0.0.1:8090',
				changeOrigin: true,
				secure: false,
				rewrite: (path) => path.replace(/^\/Upload_Images/, '/api/Upload_Images'),
			},
			'/storage': {
				target: 'http://127.0.0.1:8090',
				changeOrigin: true,
				secure: false,
			},
			'/api': {
				target: 'http://127.0.0.1:8090',
				changeOrigin: true,
				secure: false,
				ws: true,
			},
			'/sounds': {
				target: 'http://127.0.0.1:8090',
				changeOrigin: true,
				secure: false,
				rewrite: (path) => path.replace(/^\/sounds/, '/api/crm/inbound/play-recording?file='),
			},
		},
	},
	logLevel: 'error',
}))
