<script setup lang="ts">
import { ref } from 'vue'

const activeFilter = ref('latest')

const setFilter = (filter: string) => {
  activeFilter.value = filter
  // 这里可以触发事件通知父组件重新加载数据
}
</script>

<template>
  <div class="bg-white dark:bg-slate-900 rounded-xl shadow-sm p-4 mb-6">
    <div class="flex items-center justify-between">
      <div class="flex space-x-4">
        <button 
          @click="setFilter('latest')"
          class="px-4 py-2 rounded-full transition-colors font-medium text-sm"
          :class="activeFilter === 'latest' ? 'bg-blue-500 text-white' : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800'"
        >
          最新
        </button>
        <button 
          @click="setFilter('hot')"
          class="px-4 py-2 rounded-full transition-colors font-medium text-sm"
          :class="activeFilter === 'hot' ? 'bg-blue-500 text-white' : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800'"
        >
          热门
        </button>
        <button 
          @click="setFilter('following')"
          class="px-4 py-2 rounded-full transition-colors font-medium text-sm"
          :class="activeFilter === 'following' ? 'bg-blue-500 text-white' : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800'"
        >
          关注
        </button>
      </div>
      <button class="text-slate-500 hover:text-blue-500 transition-colors">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="4" y1="21" x2="4" y2="14"></line><line x1="4" y1="10" x2="4" y2="3"></line><line x1="12" y1="21" x2="12" y2="12"></line><line x1="12" y1="8" x2="12" y2="3"></line><line x1="20" y1="21" x2="20" y2="16"></line><line x1="20" y1="12" x2="20" y2="3"></line><line x1="1" y1="14" x2="7" y2="14"></line><line x1="9" y1="8" x2="15" y2="8"></line><line x1="17" y1="16" x2="23" y2="16"></line></svg>
      </button>
    </div>
  </div>
</template>
