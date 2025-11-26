<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/userStore'
import { useNotificationStore } from '../stores/notificationStore' // 1. 引入
import AuthModal from './AuthModal.vue'
import NotificationDropdown from './NotificationDropdown.vue' // 2. 引入

const emit = defineEmits(['open-login'])
const router = useRouter()
const userStore = useUserStore()
const notificationStore = useNotificationStore() // 3. 初始化

const authModalRef = ref<InstanceType<typeof AuthModal> | null>(null)
const isNotificationOpen = ref(false) // 4. 控制下拉框

const handleOpenLogin = () => {
  authModalRef.value?.open('login')
}

const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    userStore.logout()
    router.push('/')
  }
}

// (新增) 切换通知面板
const toggleNotifications = (event: Event) => {
   event.stopPropagation() // 阻止冒泡
   isNotificationOpen.value = !isNotificationOpen.value
}
// (新增) 点击外部关闭
const closeNotifications = () => {
   isNotificationOpen.value = false
}
</script>

<template>
  <header class="sticky top-0 z-50 flex h-16 w-full items-center justify-between border-b border-slate-200/80 bg-white/80 px-4 lg:px-8 backdrop-blur-xl">
    <div class="flex items-center gap-2.5 cursor-pointer" @click="router.push('/')">
      <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-white shadow-sm">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m18 16 4-4-4-4"/><path d="m6 8-4 4 4 4"/><path d="m14.5 4-5 16"/></svg>
      </div>
      <div class="text-xl font-bold tracking-tight font-mono text-clean-text">
        CodeArena<span class="text-primary">_</span>
      </div>
    </div>

    <div class="hidden max-w-md flex-1 mx-12 md:flex">
      <div class="relative w-full group">
        <span class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-brand-blue">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
        </span>
        <input type="text" placeholder="搜索技术栈、错误码..." class="w-full h-10 pl-10 pr-4 rounded-xl bg-slate-100/80 border-transparent focus:bg-white focus:border-brand-blue/30 focus:ring-4 focus:ring-brand-blue/10 transition-all text-sm"/>
      </div>
    </div>

    <div class="flex items-center gap-4">
      <button class="hidden md:flex items-center gap-2 rounded-lg border border-clean-border bg-white px-3 py-2 text-sm font-medium text-clean-text shadow-sm-soft hover:bg-clean-sub/50 transition-all active:scale-[0.98]">
        <span>⚔️</span> 模拟面试
      </button>
      
      <button class="hidden lg:flex items-center gap-2 px-4 py-2 rounded-lg font-bold text-sm text-white bg-gradient-to-r from-functional-red to-orange-500 hover:shadow-lg hover:shadow-functional-red/20 transition-all active:scale-95">
        <span>🚨</span> 紧急作战室
      </button>
      
      <div class="h-6 w-px bg-slate-200"></div>

      <template v-if="!userStore.isLoggedIn">
        <button 
          @click="handleOpenLogin"
          class="px-5 py-2 rounded-xl bg-brand-blue/10 text-brand-blue font-bold text-sm hover:bg-brand-blue hover:text-white transition-all active:scale-95"
        >
          登录 / 注册
        </button>
      </template>

      <template v-else>
        <div class="relative">
          <button @click.stop="toggleNotifications" class="relative p-2 text-clean-muted hover:bg-clean-sub rounded-md transition-all">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/></svg>
            <span v-if="notificationStore.unreadCount > 0" class="absolute top-1.5 right-1.5 h-2 w-2 rounded-full bg-danger ring-2 ring-white"></span>
          </button>
          
          <NotificationDropdown :show="isNotificationOpen" @close="closeNotifications" />
        </div>
        
        <div class="flex items-center gap-3 group cursor-pointer relative">
           <div class="text-right hidden md:block">
             <div class="text-sm font-bold text-vibrant-main">{{ userStore.displayName }}</div>
             <div class="text-xs text-vibrant-muted">Lv.5 架构师</div>
           </div>
           <img :src="userStore.userAvatar" class="h-10 w-10 rounded-full bg-slate-100 border-2 border-white shadow-sm transition-transform group-hover:scale-105 object-cover" />
           
           <div class="absolute top-full right-0 mt-2 w-48 bg-white rounded-xl shadow-xl border border-slate-100 p-2 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all z-50">
              <router-link to="/profile" class="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-slate-100 text-sm font-medium text-vibrant-main">个人中心</router-link>
              <a href="#" class="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-slate-100 text-sm font-medium text-vibrant-main">设置</a>
              <div class="h-px bg-slate-100 my-1"></div>
              <button @click="handleLogout" class="w-full flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-red-50 text-sm font-medium text-functional-red">退出登录</button>
           </div>
        </div>
      </template>
    </div>
  </header>
  
  <AuthModal ref="authModalRef" />
</template>