<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { authService } from '../../services/authService'
import { useUserStore } from '../../stores/userStore'
import wsService from '../../services/WebSocketService'
import config from '../../utils/config'

const emit = defineEmits(['switch-mode', 'success'])
const isLoading = ref(false)
const errorMsg = ref('')

const userStore = useUserStore()

// 验证码图片 URL
const captchaUrl = ref('')

const form = reactive({
  username: '',
  password: '',
  uuid: '',    // 存储当前的 UUID
  captcha: ''  // 存储用户输入的验证码
})

// 生成 UUID 的简易方法
const getUUID = () => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

// 刷新验证码
const refreshCaptcha = () => {
  form.uuid = getUUID()
  // 这里假设你的 Vite 代理配置了 /api -> 后端地址
  // 后端接口为 /auth/captcha，所以前端请求 /api/auth/captcha
  // 加上时间戳参数 t 防止浏览器缓存
  captchaUrl.value = `/api/auth/captcha?uuid=${form.uuid}&t=${new Date().getTime()}`
}

// 组件挂载时，先获取一次验证码
onMounted(() => {
  refreshCaptcha()
})

const handleLogin = async () => {
  // 基础校验
  if (!form.username || !form.password) {
    errorMsg.value = '请输入账号和密码'
    return
  }
  if (!form.captcha) {
    errorMsg.value = '请输入验证码'
    return
  }

  try {
    isLoading.value = true
    errorMsg.value = ''
    
    // 1. 调用登录接口
    const tokenData = await authService.login({
        username: form.username,
        password: form.password,
        uuid: form.uuid,
        captcha: form.captcha
    })
    
    // 临时保存 token 以便后续请求使用
    localStorage.setItem('token', tokenData.access_token)
    // FIX: 立即更新 store 中的 token，确保 getUserInfo 请求能带上 Authorization 头
    userStore.token = tokenData.access_token

    // 2. 获取用户信息
    const userInfo = await authService.getUserInfo()

    // 3. 更新 Pinia 状态
    userStore.login(tokenData.access_token, userInfo)
    
    // 4. 连接 WebSocket
    wsService.connect(config.WEBSOCKET_URL, tokenData.access_token)
    
    emit('success')
  } catch (e: any) {
    console.error('登录失败:', e)
    errorMsg.value = e.message || '登录失败，请检查账号密码或验证码'
    // 登录失败后，通常验证码会失效，需要自动刷新
    refreshCaptcha()
    // 清空已输入的验证码，体验更好
    form.captcha = ''
  } finally {
    isLoading.value = false
  }
}

const goToRegister = () => {
  emit('switch-mode', 'register')
}
</script>

<template>
  <div class="animate-fadeIn">
    <div class="mb-8">
      <h2 class="text-2xl font-extrabold text-vibrant-main tracking-tight">欢迎回到 CodeArena</h2>
      <p class="text-vibrant-muted text-sm mt-2">登录以连接全球开发者网络</p>
    </div>

    <div v-if="errorMsg" class="mb-6 p-3 rounded-xl bg-functional-red/10 text-functional-red text-sm font-bold flex items-center gap-2 animate-shake">
      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" x2="12" y1="8" y2="12"/><line x1="12" x2="12.01" y1="16" y2="16"/></svg>
      {{ errorMsg }}
    </div>

    <form @submit.prevent="handleLogin" class="flex flex-col gap-5">
      <div class="space-y-1.5">
        <label class="text-xs font-bold text-vibrant-muted ml-1 uppercase tracking-wider">账号</label>
        <input 
          v-model="form.username" 
          type="text" 
          placeholder="请输入用户名" 
          class="w-full h-12 px-4 rounded-xl bg-slate-50 border-2 border-transparent focus:bg-white focus:border-brand-blue/50 focus:ring-4 focus:ring-brand-blue/10 transition-all outline-none font-medium text-vibrant-main"
        />
      </div>

      <div class="space-y-1.5">
        <div class="flex justify-between items-center ml-1">
          <label class="text-xs font-bold text-vibrant-muted uppercase tracking-wider">密码</label>
          <a href="#" class="text-xs font-bold text-brand-blue hover:underline" tabindex="-1">忘记密码？</a>
        </div>
        <input 
          v-model="form.password" 
          type="password" 
          placeholder="••••••" 
          class="w-full h-12 px-4 rounded-xl bg-slate-50 border-2 border-transparent focus:bg-white focus:border-brand-blue/50 focus:ring-4 focus:ring-brand-blue/10 transition-all outline-none font-medium"
        />
      </div>

      <div class="space-y-1.5">
        <label class="text-xs font-bold text-vibrant-muted ml-1 uppercase tracking-wider">验证码</label>
        <div class="flex gap-3">
          <input 
            v-model="form.captcha" 
            type="text" 
            placeholder="输入右侧图片内容" 
            class="flex-1 h-12 px-4 rounded-xl bg-slate-50 border-2 border-transparent focus:bg-white focus:border-brand-blue/50 focus:ring-4 focus:ring-brand-blue/10 transition-all outline-none font-medium text-vibrant-main"
          />
          <div 
            class="h-12 w-[150px] bg-slate-100 rounded-xl overflow-hidden cursor-pointer border-2 border-transparent hover:border-brand-blue/30 transition-all relative shrink-0"
            @click="refreshCaptcha"
            title="点击刷新验证码"
          >
            <img 
              v-if="captchaUrl" 
              :src="captchaUrl" 
              alt="验证码" 
              class="h-full w-full object-contain" 
            />
            <div v-else class="h-full w-full flex items-center justify-center text-xs text-vibrant-muted">
              加载中...
            </div>
          </div>
        </div>
      </div>

      <button 
        type="submit" 
        :disabled="isLoading"
        class="mt-4 h-12 w-full bg-brand-blue text-white font-bold rounded-xl hover:bg-brand-indigo active:scale-95 transition-all disabled:opacity-50 flex items-center justify-center gap-2"
      >
        <svg v-if="isLoading" class="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
        {{ isLoading ? '安全登录中...' : '立即登录' }}
      </button>
    </form>

    <div class="mt-8 text-center text-sm text-vibrant-muted font-medium">
      还没有账号？ 
      <button type="button" @click="goToRegister" class="text-brand-blue font-bold hover:underline focus:outline-none px-2 py-1">
        立即免费注册
      </button>
    </div>
  </div>
</template>