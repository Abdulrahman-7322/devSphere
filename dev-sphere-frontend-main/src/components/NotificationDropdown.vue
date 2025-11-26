<script setup lang="ts">
import { useNotificationStore, NoticeType, ProcessResultValue } from '../stores/notificationStore'

defineProps<{
  show: boolean
}>()
const emit = defineEmits(['close'])

const notificationStore = useNotificationStore()

// 阻止事件冒泡，防止点击下拉框时关闭
const handleClickInside = (event: Event) => {
  event.stopPropagation()
}

// 同意
const onAccept = (notice: any) => {
  notificationStore.handleFriendRequest(notice, true)
}

// 拒绝
const onReject = (notice: any) => {
  notificationStore.handleFriendRequest(notice, false)
}

// 标记已读
const onRead = (noticeId: number) => {
  notificationStore.markAsRead(noticeId)
}

</script>

<template>
  <Transition
    enter-active-class="transition duration-150 ease-out"
    enter-from-class="transform scale-95 opacity-0"
    enter-to-class="transform scale-100 opacity-100"
    leave-active-class="transition duration-100 ease-in"
    leave-from-class="transform scale-100 opacity-100"
    leave-to-class="transform scale-95 opacity-0"
  >
    <div 
      v-if="show" 
      @mousedown.stop="handleClickInside" 
      class="absolute top-14 right-0 w-80 sm:w-96 bg-white rounded-xl shadow-xl border border-slate-100 z-50 overflow-hidden"
    >
      <div class="p-4 border-b border-slate-100 flex justify-between items-center">
        <h4 class="font-bold text-vibrant-main">消息通知</h4>
        <button class="text-xs font-medium text-brand-blue hover:underline">全部已读</button>
      </div>

      <div class="max-h-[70vh] overflow-y-auto custom-scrollbar">
        
        <div v-if="notificationStore.notifications.length === 0" class="p-10 text-center">
          <svg class="h-12 w-12 text-slate-200 mx-auto" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/></svg>
          <p class="text-sm font-medium text-slate-500 mt-4">暂无新通知</p>
        </div>
        
        <div v-else>
          <div 
            v-for="notice in notificationStore.notifications" 
            :key="notice.id"
            class="p-4 border-b border-slate-100 hover:bg-slate-50 transition-colors"
            :class="{ 'bg-blue-50/50': notice.readTarget === 0 }"
          >
            <div class="flex items-center gap-3 mb-3">
              <img :src="notice.avatar" class="h-10 w-10 rounded-full bg-slate-100" />
              <div class="flex-1">
                <div class="text-sm font-semibold text-vibrant-main">{{ notice.title }}</div>
                <div class="Read-vibrant-muted mt-1">{{ new Date(notice.createTime).toLocaleString() }}</div>
              </div>
            </div>
            
            <p class="text-sm text-vibrant-muted mb-4">{{ notice.noticeContent }}</p>

            <div 
              v-if="notice.noticeType === NoticeType.USER && notice.processResult === ProcessResultValue.PENDING" 
              class="flex items-center justify-end gap-3"
            >
              <button 
                @click="onReject(notice)"
                class="px-4 py-1.5 text-xs font-bold text-slate-500 bg-slate-100 hover:bg-slate-200 rounded-md transition-colors"
              >
                拒绝
              </button>
              <button 
                @click="onAccept(notice)"
                class="px-4 py-1.5 text-xs font-bold text-white bg-brand-blue hover:bg-brand-indigo rounded-md transition-colors"
              >
                同意
              </button>
            </div>
            
            <div v-if="notice.processResult === ProcessResultValue.AGREE" class="text-right text-xs font-bold text-functional-green">
              ✓ 已同意
            </div>
            <div v-if="notice.processResult === ProcessResultValue.REFUSE" class="text-right text-xs font-bold text-functional-red">
              ✗ 已拒绝
            </div>

            <div 
              v-if="notice.noticeType !== NoticeType.USER && notice.readTarget === 0" 
              class="text-right"
            >
               <button 
                 @click="onRead(notice.id)"
                 class="px-4 py-1.5 text-xs font-bold text-brand-blue bg-blue-50 hover:bg-blue-100 rounded-md transition-colors"
               >
                 标为已读
               </button>
            </div>

          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 5px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  @apply bg-slate-200 rounded-full hover:bg-slate-300;
}
</style>