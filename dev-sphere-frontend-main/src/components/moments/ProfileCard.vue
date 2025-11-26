<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '../../stores/userStore'

const userStore = useUserStore()
const user = computed(() => userStore.userInfo)
const avatar = computed(() => userStore.userAvatar)
</script>

<template>
  <div class="bg-white dark:bg-slate-900 rounded-xl shadow-sm p-6">
    <div class="flex flex-col items-center">
      <img 
        :src="avatar" 
        alt="用户头像" 
        class="w-20 h-20 rounded-full object-cover border-4 border-blue-500 shadow-md"
      >
      <h2 class="mt-4 text-lg font-bold text-slate-900 dark:text-white">{{ user?.username || '未登录' }}</h2>
      <p class="text-slate-500 dark:text-slate-400 text-sm mt-1">{{ user?.realName || '这个人很懒，什么都没写' }}</p>
      
      <div class="flex justify-between w-full mt-6 border-t border-slate-100 dark:border-slate-800 pt-4">
        <div class="text-center flex-1">
          <p class="font-bold text-slate-900 dark:text-white">268</p>
          <p class="text-xs text-slate-500 dark:text-slate-400">动态</p>
        </div>
        <div class="text-center flex-1 border-l border-r border-slate-100 dark:border-slate-800">
          <p class="font-bold text-slate-900 dark:text-white">342</p>
          <p class="text-xs text-slate-500 dark:text-slate-400">好友</p>
        </div>
        <div class="text-center flex-1">
          <p class="font-bold text-slate-900 dark:text-white">1.2k</p>
          <p class="text-xs text-slate-500 dark:text-slate-400">获赞</p>
        </div>
      </div>
    </div>
  </div>
</template>
