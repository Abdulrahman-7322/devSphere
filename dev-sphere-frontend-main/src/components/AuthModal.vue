<script setup lang="ts">
import { ref, shallowRef } from 'vue'
import LoginForm from './auth/LoginForm.vue'
import RegisterForm from './auth/RegisterForm.vue'

type AuthMode = 'login' | 'register'
const isVisible = ref(false)
const currentMode = shallowRef<AuthMode>('login')

const open = (mode: AuthMode = 'login') => {
  console.log('%c [AuthModal] 打开, 模式: ' + mode, 'color:green;font-weight:bold;')
  currentMode.value = mode
  isVisible.value = true
}

const close = () => isVisible.value = false

const handleSwitchMode = (mode: AuthMode) => {
  console.log('%c [AuthModal] 切换到: ' + mode, 'color:blue;font-weight:bold;')
  currentMode.value = mode
}

defineExpose({ open, close })
</script>

<template>
  <Teleport to="body">
    <Transition enter-active-class="transition duration-300 ease-out" enter-from-class="opacity-0" leave-active-class="transition duration-200 ease-in" leave-to-class="opacity-0">
      <div v-if="isVisible" class="fixed inset-0 z-[100] bg-slate-900/40 backdrop-blur-sm" @click="close"></div>
    </Transition>

    <Transition enter-active-class="transition duration-400 ease-out-cubic" enter-from-class="opacity-0 translate-y-8" leave-active-class="transition duration-300 ease-in-cubic" leave-to-class="opacity-0 translate-y-8">
      <div v-if="isVisible" class="fixed inset-0 z-[101] flex items-center justify-center p-4 pointer-events-none">
        <div class="w-full max-w-[420px] bg-white rounded-3xl shadow-2xl pointer-events-auto relative overflow-hidden ">
           <div class="h-2 w-full bg-gradient-to-r from-brand-blue via-brand-indigo to-purple-500"></div>
           <button @click="close" class="absolute top-5 right-5 p-2 text-slate-400 hover:bg-slate-100 rounded-full transition-all z-10">
             <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
           </button>

           <div class="p-8 pt-10">
              <Transition mode="out-in" enter-active-class="transition duration-200 ease-out" enter-from-class="opacity-0 -translate-x-4" leave-active-class="transition duration-150 ease-in" leave-to-class="opacity-0 translate-x-4">
                <component :is="currentMode === 'login' ? LoginForm : RegisterForm" @switch-mode="handleSwitchMode" @success="close" />
              </Transition>
           </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>