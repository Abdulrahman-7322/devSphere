<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '../../stores/userStore'

const userStore = useUserStore()
const avatar = computed(() => userStore.userAvatar)

const emit = defineEmits(['click'])
</script>

<template>
  <div class="bg-white dark:bg-slate-900 rounded-xl shadow-sm p-6 mb-6">
    <div class="flex space-x-4">
      <img 
        :src="avatar" 
        alt="用户头像" 
        class="w-10 h-10 rounded-full object-cover"
      >
      <div class="flex-1">
        <div 
          @click="emit('click')"
          class="w-full px-4 py-3 rounded-full bg-slate-100 dark:bg-slate-800 text-slate-500 dark:text-slate-400 cursor-pointer hover:bg-slate-200 dark:hover:bg-slate-700 transition-colors"
        >
          分享你的想法...
        </div>
        <div class="flex justify-between mt-4">
          <div class="flex space-x-4">
            <button class="flex items-center text-slate-500 hover:text-blue-500 transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-1"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg>
              <span class="text-sm">图片</span>
            </button>
            <button class="flex items-center text-slate-500 hover:text-blue-500 transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-1"><polygon points="23 7 16 12 23 17 23 7"></polygon><rect x="1" y="5" width="15" height="14" rx="2" ry="2"></rect></svg>
              <span class="text-sm">视频</span>
            </button>
            <button class="flex items-center text-slate-500 hover:text-blue-500 transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-1"><circle cx="12" cy="12" r="10"></circle><path d="M8 14s1.5 2 4 2 4-2 4-2"></path><line x1="9" y1="9" x2="9.01" y2="9"></line><line x1="15" y1="9" x2="15.01" y2="9"></line></svg>
              <span class="text-sm">心情</span>
            </button>
          </div>
          <button 
            @click="emit('click')"
            class="px-6 py-2 bg-blue-500 text-white rounded-full hover:bg-blue-600 transition-colors font-medium text-sm shadow-md hover:shadow-lg"
          >
            发布
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
