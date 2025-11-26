<script setup lang="ts">
import { computed } from 'vue'
import { useChatStore } from '../../stores/chatStore'
import { useUserStore } from '../../stores/userStore'
import { chatService } from '../../services/chatService'

const chatStore = useChatStore()
const userStore = useUserStore()

// 当前会话信息
const currentConversation = computed(() => chatStore.activeConversation)

// 清空聊天记录
const handleClearHistory = async () => {
  if (!confirm('确定要清空聊天记录吗？此操作不可恢复。')) return
  if (!chatStore.activeRoomId) return
  
  try {
    await chatStore.clearHistory(chatStore.activeRoomId)
    // 刷新消息列表 (重新加载)
    // 注意：store 中没有直接暴露 messageMap 的 setter，但可以直接修改 reactive 对象
    // 或者调用 store action 来清空
    // 这里假设可以直接修改，或者重新加载会覆盖
    // 更好的方式是在 store 中添加 clearLocalMessages action
    // 暂时先重新加载
    await chatStore.loadMoreMessages(chatStore.activeRoomId)
    alert('聊天记录已清空')
    chatStore.closeChatDetailModal()
  } catch (e: any) {
    alert(e.message || '操作失败')
  }
}

// 查找聊天记录 (暂未实现 UI，先占位)
const handleSearchHistory = () => {
  alert('查找功能即将上线')
  // TODO: 实现查找 UI
}

// 删除好友
const handleDeleteFriend = async () => {
  if (!confirm('确定要删除该好友吗？同时会删除聊天记录。')) return
  if (!currentConversation.value) return
  
  try {
    await chatStore.deleteFriend(currentConversation.value.targetId)
    alert('好友已删除')
    chatStore.closeChatDetailModal()
    // 可以在这里跳转回首页或清空当前会话
  } catch (e: any) {
    alert(e.message || '操作失败')
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition 
      enter-active-class="transition-opacity duration-300 ease-out" 
      enter-from-class="opacity-0" 
      enter-to-class="opacity-100" 
      leave-active-class="transition-opacity duration-200 ease-in" 
      leave-from-class="opacity-100" 
      leave-to-class="opacity-0"
    >
      <div 
        v-if="chatStore.isChatDetailOpen" 
        class="fixed inset-0 z-[100] bg-slate-900/40 backdrop-blur-sm" 
        @click="chatStore.closeChatDetailModal"
      ></div>
    </Transition>

    <Transition
      enter-active-class="transition duration-400 ease-out-cubic"
      enter-from-class="translate-x-full"
      enter-to-class="translate-x-0"
      leave-active-class="transition duration-300 ease-in-cubic"
      leave-from-class="translate-x-0"
      leave-to-class="translate-x-full"
    >
      <div 
        v-if="chatStore.isChatDetailOpen" 
        class="fixed top-0 right-0 z-[101] h-full w-full max-w-sm bg-white shadow-2xl flex flex-col"
      >
        <div class="flex items-center justify-between p-5 border-b border-slate-100 shrink-0">
          <h3 class="text-lg font-bold text-vibrant-main">聊天详情</h3>
          <button @click="chatStore.closeChatDetailModal" class="p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600 rounded-full transition-all">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
          </button>
        </div>

        <div class="flex-1 overflow-y-auto p-6 space-y-6">
          <!-- 头像和名称 -->
          <div class="flex flex-col items-center gap-3 pb-6 border-b border-slate-100">
            <img 
              :src="currentConversation?.avatar" 
              class="w-20 h-20 rounded-full object-cover border-4 border-slate-50 shadow-sm"
              alt="Avatar"
            />
            <h4 class="text-xl font-bold text-slate-800">{{ currentConversation?.name }}</h4>
          </div>

          <!-- 操作列表 -->
          <div class="space-y-2">
            <button 
              @click="handleSearchHistory"
              class="w-full flex items-center justify-between p-4 rounded-xl bg-slate-50 hover:bg-slate-100 transition-colors text-slate-700 font-medium"
            >
              <span>查找聊天记录</span>
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-slate-400"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
            </button>

            <button 
              @click="handleClearHistory"
              class="w-full flex items-center justify-between p-4 rounded-xl bg-slate-50 hover:bg-slate-100 transition-colors text-slate-700 font-medium"
            >
              <span>清空聊天记录</span>
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-slate-400"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/></svg>
            </button>
          </div>

          <div class="pt-6 border-t border-slate-100">
            <button 
              @click="handleDeleteFriend"
              class="w-full py-3 px-4 rounded-xl bg-red-50 text-red-600 font-bold hover:bg-red-100 transition-colors flex items-center justify-center gap-2"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="18" x2="23" y1="8" y2="13"/><line x1="23" x2="18" y1="8" y2="13"/></svg>
              删除好友
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
