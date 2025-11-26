<script setup lang="ts">
import { ref } from 'vue'

const emit = defineEmits(['publish'])

const content = ref('')
const showEmojiPicker = ref(false)

const handlePublish = () => {
  if (content.value.trim()) {
    emit('publish', { content: content.value })
    content.value = ''
  }
}
</script>

<template>
  <div class="quick-publish">
    <h3 class="title">快速发布</h3>
    
    <textarea 
      v-model="content"
      placeholder="分享新鲜事..."
      class="textarea"
      rows="3"
    ></textarea>
    
    <div class="actions">
      <div class="action-buttons">
        <button class="action-btn" title="上传图片">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
        </button>
        <button class="action-btn" title="表情">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M8 14s1.5 2 4 2 4-2 4-2"/><line x1="9" y1="9" x2="9.01" y2="9"/><line x1="15" y1="9" x2="15.01" y2="9"/></svg>
        </button>
        <button class="action-btn" title="位置">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
        </button>
      </div>
      
      <button 
        @click="handlePublish"
        :disabled="!content.trim()"
        class="publish-btn"
      >
        发布
      </button>
    </div>
  </div>
</template>

<style scoped>
.quick-publish {
  @apply bg-white dark:bg-slate-900 rounded-2xl p-5 shadow-sm border border-slate-200 dark:border-slate-800;
}

.title {
  @apply text-base font-semibold text-slate-900 dark:text-white mb-3;
}

.textarea {
  @apply w-full px-4 py-3 text-sm bg-slate-50 dark:bg-slate-800 border-0 rounded-xl 
         focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none
         text-slate-900 dark:text-white placeholder-slate-400;
}

.actions {
  @apply flex items-center justify-between mt-3;
}

.action-buttons {
  @apply flex items-center gap-2;
}

.action-btn {
  @apply p-2 text-slate-500 hover:text-blue-500 hover:bg-blue-50 dark:hover:bg-blue-900/20 
         rounded-lg transition-colors;
}

.publish-btn {
  @apply px-5 py-2 bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700
         disabled:from-slate-300 disabled:to-slate-300 dark:disabled:from-slate-700 dark:disabled:to-slate-700
         text-white rounded-lg text-sm font-medium transition-all
         disabled:cursor-not-allowed shadow-lg shadow-blue-500/30 disabled:shadow-none;
}
</style>
