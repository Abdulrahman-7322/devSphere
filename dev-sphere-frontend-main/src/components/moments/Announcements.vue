<script setup lang="ts">
const announcements = [
  { id: 1, title: 'DevSphere v2.0 即将发布', date: '2023-11-25', type: 'important' },
  { id: 2, title: '服务器维护通知', date: '2023-11-24', type: 'normal' },
  { id: 3, title: '社区规范更新', date: '2023-11-20', type: 'normal' },
]
</script>

<template>
  <div class="bg-white dark:bg-slate-900 rounded-2xl p-5 shadow-sm border border-slate-200 dark:border-slate-800">
    <div class="flex items-center justify-between mb-4">
      <h3 class="font-semibold text-slate-900 dark:text-white flex items-center gap-2">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-blue-500"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
        公告栏
      </h3>
      <a href="#" class="text-xs text-blue-500 hover:text-blue-600">更多</a>
    </div>

    <div class="space-y-3">
      <div 
        v-for="item in announcements" 
        :key="item.id"
        class="group cursor-pointer"
      >
        <div class="flex items-start justify-between gap-2">
          <p class="text-sm text-slate-700 dark:text-slate-300 group-hover:text-blue-500 transition-colors line-clamp-1">
            <span v-if="item.type === 'important'" class="inline-block w-2 h-2 rounded-full bg-red-500 mr-2 mb-0.5"></span>
            <span v-else class="inline-block w-2 h-2 rounded-full bg-slate-300 mr-2 mb-0.5"></span>
            {{ item.title }}
          </p>
          <span class="text-xs text-slate-400 shrink-0">{{ item.date.slice(5) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
