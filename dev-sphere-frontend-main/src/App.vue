<script setup lang="ts">
import { ref, watch, onMounted } from 'vue' // onMounted 仍然保留，以防万一，但逻辑并入 watch
import { useRoute, useRouter } from 'vue-router'
// 👇 1. 引入所有需要的 store
import { useUserStore } from './stores/userStore'
import { useNotificationStore } from './stores/notificationStore' // 引入通知 store
import wsService from './services/WebSocketService'
import AppHeader from './components/AppHeader.vue'
import AuthModal from './components/AuthModal.vue'
import config from './utils/config'

import { authService } from './services/authService'

const route = useRoute()
const router = useRouter()
// 👇 2. 初始化所有 store
const userStore = useUserStore()
const notificationStore = useNotificationStore() // 初始化通知 store
const authModalRef = ref<InstanceType<typeof AuthModal> | null>(null)

// 👇 3. 核心修改：使用一个带 immediate:true 的 watch 统一处理登录/登出/刷新
watch(() => userStore.isLoggedIn, (isLoggedIn) => {
  if (isLoggedIn && userStore.token) {
    // 状态为“已登录”
    console.log('[App] 状态: 已登录。连接 WebSocket 和 SSE...')
    
    // (1) 连接 WebSocket (使用你的 config)
    wsService.connect(config.WEBSOCKET_URL, userStore.token)
    
    // (2) 连接 SSE 通知服务
    notificationStore.connectSSE()
    
    // (3) 拉取历史通知
    notificationStore.fetchNotifications()

  } else {
    // 状态为“未登录”
    console.log('[App] 状态: 未登录。断开所有服务...')
    wsService.close()
    notificationStore.disconnectSSE()
  }
}, { immediate: true }) // immediate: true 确保在页面加载时立即执行一次

// 校验 Token 有效性
onMounted(async () => {
  if (userStore.isLoggedIn) {
    try {
      // 主动请求一次用户信息，如果 Token 失效，request.ts 会自动处理 401 并登出
      const info = await authService.getUserInfo()
      // 如果成功，顺便更新一下本地用户信息
      if (info) {
        userStore.userInfo = info
        localStorage.setItem('userInfo', JSON.stringify(info))
      }
    } catch (error) {
      console.error('[App] Token 校验失败或网络错误', error)
      // 401 错误已经被 request.ts 拦截并处理（登出跳转），这里无需重复处理
    }
  }
})


// 4. (保留) 监听路由守卫的登录请求
watch(() => route.query.auth_redirect, (redirectPath) => {
  if (redirectPath) {
    console.log('[App] 捕获到路由守卫的登录请求, 弹出登录框')
    authModalRef.value?.open('login')
    const { auth_redirect, ...query } = route.query
    router.replace({ query })
  }
}, { immediate: true })

// 5. (保留) 打开登录框的方法
const openLogin = () => {
  authModalRef.value?.open('login')
}
</script>

<template>
  <div class="h-screen flex flex-col font-sans bg-vibrant-bg overflow-hidden">
    <AppHeader @open-login="openLogin" />

    <router-view v-slot="{ Component }" class="flex-1 overflow-hidden">
      <transition name="fade" mode="out-in">
        <component :is="Component" :key="route.path" />
      </transition>
    </router-view>

    
    <AuthModal ref="authModalRef" />
  </div>
</template>

<style>
/* (保留) 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>