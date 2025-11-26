import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5174, // 你的前端端口
    proxy: {
      // 核心规则：凡是以 /api 开头的请求，都走这个代理
      '/api': {
        // 👇👇👇 重点：这里一定要写你真实的后端地址和端口
        target: 'http://localhost:8081', 
        changeOrigin: true,
        // 👇👇👇 重点：后端接口没有 /api 前缀，所以要把它去掉
        rewrite: (path) => path.replace(/^\/api/, '') 
      }
    }
  }
})