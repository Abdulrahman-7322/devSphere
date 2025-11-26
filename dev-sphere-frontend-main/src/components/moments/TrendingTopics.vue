<script setup lang="ts">
const topics = [
  { name: '前端开发', count: 1234, trend: 'up' },
  { name: 'Vue3实战', count: 856, trend: 'up' },
  { name: '设计分享', count: 645, trend: 'hot' },
  { name: '生活日常', count: 432, trend: 'up' },
]
</script>

<template>
  <div class="trending-topics">
    <h3 class="title">热门话题</h3>
    
    <div class="topics-list">
      <div 
        v-for="topic in topics" 
        :key="topic.name" 
        class="topic-item"
      >
        <div class="topic-info">
          <div class="topic-name">
            <span class="hash">#</span>
            {{ topic.name }}
          </div>
          <div class="topic-stats">
            {{ topic.count }} 参与
          </div>
        </div>
        
        <div class="topic-badge">
          <svg v-if="topic.trend === 'hot'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="currentColor" class="text-orange-500">
            <path d="M12 2c1.1 0 2 .9 2 2v1.17c0 .53.21 1.04.59 1.42l1.83 1.83c.78.78.78 2.05 0 2.83l-1.83 1.83c-.38.38-.59.89-.59 1.42V16c0 1.1-.9 2-2 2s-2-.9-2-2v-1.17c0-.53-.21-1.04-.59-1.42l-1.83-1.83c-.78-.78-.78-2.05 0-2.83l1.83-1.83c.38-.38.59-.89.59-1.42V4c0-1.1.9-2 2-2m0 18c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"/>
          </svg>
          <svg v-else xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-green-500">
            <polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/>
            <polyline points="17 6 23 6 23 12"/>
          </svg>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.trending-topics {
  @apply bg-white dark:bg-slate-900 rounded-2xl p-5 shadow-sm border border-slate-200 dark:border-slate-800;
}

.title {
  @apply text-base font-semibold text-slate-900 dark:text-white mb-3;
}

.topic-item {
  @apply flex items-center justify-between p-3 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors cursor-pointer;
}

.hash {
  @apply text-blue-500 mr-1;
}

.topic-stats {
  @apply text-xs text-slate-500 dark:text-slate-400;
}

.topic-badge {
  @apply flex-shrink-0 ml-2;
}
</style>
