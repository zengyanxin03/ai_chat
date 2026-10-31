import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8123',
        changeOrigin: true,
        timeout: 0,
        proxyTimeout: 0,
        configure: (proxy) => {
          proxy.on('proxyRes', (proxyRes) => {
            const contentType = proxyRes.headers['content-type'] || ''
            if (contentType.includes('text/event-stream')) {
              proxyRes.headers['cache-control'] = 'no-cache, no-transform'
              proxyRes.headers['x-accel-buffering'] = 'no'
              proxyRes.headers['connection'] = 'keep-alive'
            }
          })
        },
      },
    },
  },
})
