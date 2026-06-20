import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 组件库 demo 调试服务：直接引用 src 源码，代理 /client 到 client-api(:8082)。
export default defineConfig({
  plugins: [vue()],
  root: 'demo',
  server: {
    port: 5174,
    proxy: {
      '/client': { target: 'http://localhost:8082', changeOrigin: true },
    },
  },
})
