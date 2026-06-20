import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  build: {
    lib: {
      entry: fileURLToPath(new URL('./src/index.ts', import.meta.url)),
      name: 'NotifyUI',
      fileName: 'notify-ui',
    },
    rollupOptions: {
      // 不打包 vue，由宿主提供
      external: ['vue'],
      output: {
        globals: { vue: 'Vue' },
      },
    },
  },
})
