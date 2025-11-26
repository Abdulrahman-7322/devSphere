<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { authService } from '../../services/authService'

// 定义向父组件发送的事件
const emit = defineEmits(['switch-mode'])

const isLoading = ref(false)
const errorMsg = ref('')
const isSuccess = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  agreeTerms: false
})

// 密码强度计算
const passwordStrength = computed(() => {
  const pwd = form.password
  if (!pwd) return 0
  let score = 0
  if (pwd.length >= 8) score++
  if (/[A-Z]/.test(pwd)) score++
  if (/[0-9]/.test(pwd)) score++
  if (/[^A-Za-z0-9]/.test(pwd)) score++
  return score
})

// 处理注册点击
const handleRegister = async () => {
  errorMsg.value = ''

  // --- 详细校验 ---
  if (!form.username) {
     errorMsg.value = '请输入用户名'
     return
  }
  if (form.username.length < 4) {
     errorMsg.value = '用户名至少4位'
     return
  }
  if (!form.password) {
     errorMsg.value = '请输入密码'
     return
  }
  if (form.password.length < 6) { // 放宽一点到6位，方便测试，生产建议8位
     errorMsg.value = '密码太短，至少需要6位'
     return
  }
  if (form.password !== form.confirmPassword) {
     errorMsg.value = '两次输入的密码不一致'
     return
  }
  if (!form.agreeTerms) {
     errorMsg.value = '必须同意服务条款才能注册'
     return
  }

  try {
    isLoading.value = true
    // 调用更新后的注册接口
    await authService.register({ 
        username: form.username, 
        password: form.password 
    })
    isSuccess.value = true
    
    // 1.5秒后自动切换去登录
    setTimeout(() => {
      emit('switch-mode', 'login')
    }, 1500)

  } catch (e: any) {
    // 显示后端返回的具体错误信息
    errorMsg.value = e.message || '注册失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

// 处理切换到登录
const toLogin = () => {
  emit('switch-mode', 'login')
}
</script>

<template>
  <div class="animate-fadeIn">
    <div class="mb-8">
      <h2 class="text-2xl font-extrabold text-vibrant-main tracking-tight">创建你的账号</h2>
      <p class="text-vibrant-muted text-sm mt-2">开启你的极客之旅，完全免费</p>
    </div>

    <div v-if="isSuccess" class="py-12 flex flex-col items-center justify-center text-center animate-scaleIn">
      <div class="h-20 w-20 bg-functional-green/10 rounded-full flex items-center justify-center text-functional-green mb-6">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-10 w-10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
      </div>
      <h3 class="text-xl font-bold text-vibrant-main mb-2">注册成功！</h3>
      <p class="text-vibrant-muted">正在为您跳转到登录页面...</p>
    </div>

    <template v-else>
        <div v-if="errorMsg" class="mb-6 p-3 rounded-xl bg-functional-red/10 text-functional-red text-sm font-bold flex items-center gap-2 animate-shake">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" x2="12" y1="8" y2="12"/><line x1="12" x2="12.01" y1="16" y2="16"/></svg>
          {{ errorMsg }}
        </div>

        <form @submit.prevent="handleRegister" class="flex flex-col gap-4">
          <div class="space-y-1.5">
            <label class="text-xs font-bold text-vibrant-muted ml-1 uppercase tracking-wider">用户名</label>
            <input v-model="form.username" type="text" placeholder="4-20位字母或数字" class="w-full h-11 px-4 rounded-xl bg-slate-50 border-2 border-transparent focus:bg-white focus:border-brand-blue/50 focus:ring-4 focus:ring-brand-blue/10 transition-all outline-none font-medium text-vibrant-main"/>
          </div>

          <div class="space-y-1.5">
            <label class="text-xs font-bold text-vibrant-muted ml-1 uppercase tracking-wider">设置密码</label>
            <input v-model="form.password" type="password" placeholder="至少6位" class="w-full h-11 px-4 rounded-xl bg-slate-50 border-2 border-transparent focus:bg-white focus:border-brand-blue/50 focus:ring-4 focus:ring-brand-blue/10 transition-all outline-none font-medium"/>
            <div v-if="form.password" class="flex gap-1 h-1 mt-2 px-1 transition-all duration-300 ease-out">
               <div class="flex-1 rounded-full transition-all duration-500" :class="passwordStrength >= 1 ? 'bg-functional-red' : 'bg-slate-200'"></div>
               <div class="flex-1 rounded-full transition-all duration-500" :class="passwordStrength >= 2 ? 'bg-functional-amber' : 'bg-slate-200'"></div>
               <div class="flex-1 rounded-full transition-all duration-500" :class="passwordStrength >= 3 ? 'bg-brand-blue' : 'bg-slate-200'"></div>
               <div class="flex-1 rounded-full transition-all duration-500" :class="passwordStrength >= 4 ? 'bg-functional-green' : 'bg-slate-200'"></div>
            </div>
          </div>

          <div class="space-y-1.5">
            <label class="text-xs font-bold text-vibrant-muted ml-1 uppercase tracking-wider">确认密码</label>
            <input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" class="w-full h-11 px-4 rounded-xl bg-slate-50 border-2 border-transparent focus:bg-white focus:border-brand-blue/50 focus:ring-4 focus:ring-brand-blue/10 transition-all outline-none font-medium"/>
          </div>

          <label class="flex items-center gap-2 cursor-pointer mt-2 select-none group">
             <input type="checkbox" v-model="form.agreeTerms" class="w-4 h-4 rounded text-brand-blue border-slate-300 focus:ring-brand-blue/50 accent-brand-blue cursor-pointer transition-all">
             <span class="text-sm text-vibrant-muted group-hover:text-vibrant-main transition-colors">
               我已阅读并同意 <a href="#" class="text-brand-blue hover:underline font-medium">《用户服务协议》</a>
             </span>
          </label>

          <button 
            type="submit" 
            :disabled="isLoading"
            class="mt-4 h-12 w-full bg-brand-indigo text-white font-bold rounded-xl hover:bg-brand-indigo/90 active:scale-95 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            <svg v-if="isLoading" class="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
            {{ isLoading ? '正在创建账号...' : '立即注册' }}
          </button>
        </form>

        <div class="mt-8 text-center text-sm text-vibrant-muted font-medium">
          已有账号？ 
          <button type="button" @click="toLogin" class="text-brand-blue font-bold hover:underline focus:outline-none px-2 py-1">
            直接登录
          </button>
        </div>
    </template>
  </div>
</template>