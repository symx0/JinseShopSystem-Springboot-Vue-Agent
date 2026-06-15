import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 3001,
    open: true,
    allowedHosts: ['4f1dfee9.r7.cpolar.top', '.cpolar.top'],
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      '/agent': {
        target: 'http://localhost:8000',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/agent/, ''),
        // SSE 需要禁用代理缓冲
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            if (req.url.includes('/chat/stream')) {
              proxyReq.setHeader('Accept', 'text/event-stream')
            }
          })
        }
      }
    }
  }
})