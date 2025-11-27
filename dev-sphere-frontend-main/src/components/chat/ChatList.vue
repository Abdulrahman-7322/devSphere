<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useChatStore, type Conversation } from '../../stores/chatStore'

const chatStore = useChatStore()

// --- 工具函数：格式化时间 ---
const formatTime = (date: Date) => {
  const now = new Date()
  const d = new Date(date)
  
  // 清除时间部分以便比较日期
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const target = new Date(d.getFullYear(), d.getMonth(), d.getDate())
  
  const diffTime = today.getTime() - target.getTime()
  const diffDays = diffTime / (1000 * 60 * 60 * 24)

  if (diffDays === 0) {
    // 今天
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  } else if (diffDays === 1) {
    // 昨天
    return '昨天 ' + d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  } else {
    // 其他
    return (d.getMonth() + 1).toString().padStart(2, '0') + '-' + d.getDate().toString().padStart(2, '0')
  }
}

// --- 消息格式化 ---
const isImageUrl = (content: string) => {
  if (!content) return false
  return /\.(jpg|jpeg|png|gif|webp|bmp|svg)$/i.test(content) || content.startsWith('blob:')
}

const isFileUrl = (content: string) => {
  if (!content) return false
  return (content.startsWith('http') || content.startsWith('/')) && !isImageUrl(content)
}

const formatMessage = (content: string) => {
  if (!content) return ''
  if (isImageUrl(content)) return '[图片]'
  if (isFileUrl(content)) return '[文件]'
  return content
}

// --- 右键菜单逻辑 (新增功能) ---
const contextMenuVisible = ref(false)
const contextMenuPosition = ref({ x: 0, y: 0 })
const selectedChatId = ref<number | null>(null)

const handleContextMenu = (e: MouseEvent, chat: Conversation) => {
  e.preventDefault()
  selectedChatId.value = chat.id
  // 简单的边界检测
  const x = e.clientX
  const y = e.clientY
  contextMenuPosition.value = { x, y }
  contextMenuVisible.value = true
}

const closeContextMenu = () => {
  contextMenuVisible.value = false
  selectedChatId.value = null
}

const handleHideChat = async () => {
  if (selectedChatId.value) {
    await chatStore.hideConversation(selectedChatId.value)
  }
  closeContextMenu()
}

const handleDeleteChat = async () => {
  if (selectedChatId.value) {
    if (confirm('确定要删除该会话吗？聊天记录将被清空。')) {
      await chatStore.deleteConversation(selectedChatId.value)
    }
  }
  closeContextMenu()
}

const handleGlobalClick = () => {
  if (contextMenuVisible.value) closeContextMenu()
}

onMounted(() => document.addEventListener('click', handleGlobalClick))
onUnmounted(() => document.removeEventListener('click', handleGlobalClick))
</script>

<template>
  <div class="flex flex-col h-full bg-white dark:bg-slate-900 border-r border-slate-200/50 dark:border-slate-700/50 transition-colors duration-300">
    
    <!-- 顶部搜索区 (新增) -->
    <div class="p-5 pb-2 shrink-0">
      <div class="flex items-center justify-between mb-4 px-1">
        <h2 class="text-2xl font-bold text-slate-900 dark:text-white tracking-tight">消息</h2>
        <button class="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 dark:hover:bg-slate-800 rounded-xl transition-all">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
        </button>
      </div>
      
      <div class="relative group">
        <div class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-slate-400 group-focus-within:text-blue-500 transition-colors"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        </div>
        <input 
          type="text" 
          placeholder="搜索会话..." 
          class="w-full bg-slate-100 dark:bg-slate-800 text-slate-900 dark:text-white pl-10 pr-4 py-3 rounded-2xl border-none outline-none focus:ring-2 focus:ring-blue-500/20 transition-all placeholder-slate-400 dark:placeholder-slate-500 text-sm font-medium"
        >
      </div>
    </div>

    <!-- 会话列表 -->
    <div class="flex-1 overflow-y-auto custom-scrollbar px-3 py-2">
      <!-- 加载中骨架屏 -->
      <div v-if="chatStore.isSessionListLoading" class="space-y-3 mt-2">
        <div v-for="i in 5" :key="i" class="flex items-center gap-4 px-3 py-3.5 rounded-2xl animate-pulse">
          <div class="h-[52px] w-[52px] rounded-2xl bg-slate-200 dark:bg-slate-700 shrink-0"></div>
          <div class="flex-1 space-y-2">
            <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-1/3"></div>
            <div class="h-3 bg-slate-200 dark:bg-slate-700 rounded w-2/3"></div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="chatStore.conversations.length === 0" class="flex flex-col items-center justify-center h-64 text-slate-400 dark:text-slate-500 text-sm animate-in fade-in duration-500">
        <div class="w-16 h-16 bg-slate-50 dark:bg-slate-800 rounded-full flex items-center justify-center mb-4">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="opacity-50"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
        </div>
        <p class="mb-2">暂无消息</p>
        <button @click="chatStore.loadSessionList()" class="text-xs text-blue-500 hover:underline">刷新列表</button>
      </div>

      <!-- 列表项 -->
      <TransitionGroup name="chat-list" tag="div" class="relative">
        <div 
           v-for="chat in chatStore.conversations" 
           :key="chat.id"
           @click="chatStore.setActiveRoom(chat.id)"
           @contextmenu.prevent.stop="handleContextMenu($event, chat)"
           class="chat-item flex items-center gap-4 px-3 py-3.5 mb-1.5 rounded-2xl cursor-pointer relative group select-none border border-transparent bg-white dark:bg-transparent"
           :class="chatStore.activeRoomId === chat.id ? 'bg-blue-50 dark:bg-blue-900/20 border-blue-100 dark:border-blue-900/30 shadow-sm z-10' : 'hover:bg-slate-50 dark:hover:bg-slate-800/50'"
        >
           <!-- 头像 -->
           <div class="relative shrink-0">
             <img :src="chat.avatar" class="h-[52px] w-[52px] rounded-2xl bg-slate-200 dark:bg-slate-700 object-cover shadow-sm group-hover:scale-105 transition-transform duration-300" />
             
             <!-- 在线状态 -->
             <span v-if="chat.isOnline" class="absolute -bottom-0.5 -right-0.5 block h-4 w-4 rounded-full ring-2 ring-white dark:ring-slate-900 bg-emerald-500"></span>
             
             <!-- 未读数 Badge -->
             <span v-if="chat.unreadCount > 0" class="absolute -top-1.5 -right-1.5 flex h-5 min-w-[1.25rem] px-1.5 items-center justify-center rounded-full ring-2 ring-white dark:ring-slate-900 bg-rose-500 text-white text-[10px] font-bold shadow-sm animate-bounce-in">
               {{ chat.unreadCount > 99 ? '99+' : chat.unreadCount }}
             </span>
           </div>
  
           <!-- 内容区 -->
           <div class="flex-1 min-w-0 flex flex-col gap-1">
             <div class="flex justify-between items-center">
               <h3 class="font-bold text-[15px] truncate transition-colors" :class="chatStore.activeRoomId === chat.id ? 'text-blue-600 dark:text-blue-400' : 'text-slate-900 dark:text-white'">{{ chat.name }}</h3>
               <span class="text-[11px] text-slate-400 dark:text-slate-500 shrink-0 font-medium">{{ formatTime(chat.lastTime) }}</span>
             </div>
             <p class="text-[13px] truncate transition-colors leading-relaxed" :class="[
               chat.unreadCount > 0 ? 'text-slate-700 dark:text-slate-200 font-medium' : 'text-slate-500 dark:text-slate-400',
               chatStore.activeRoomId === chat.id ? 'text-blue-600/80 dark:text-blue-300/80' : ''
             ]">
               {{ formatMessage(chat.lastMessage) }}
             </p>
           </div>
        </div>
      </TransitionGroup>
    </div>

    <!-- 右键菜单 (Portal) -->
    <Teleport to="body">
      <Transition
        enter-active-class="transition duration-150 ease-out"
        enter-from-class="transform scale-95 opacity-0"
        enter-to-class="transform scale-100 opacity-100"
        leave-active-class="transition duration-100 ease-in"
        leave-from-class="transform scale-100 opacity-100"
        leave-to-class="transform scale-95 opacity-0"
      >
        <div 
          v-if="contextMenuVisible"
          class="fixed z-[9999] bg-white dark:bg-slate-800 rounded-xl shadow-2xl border border-slate-100 dark:border-slate-700 py-1.5 w-40 overflow-hidden backdrop-blur-xl ring-1 ring-black/5"
          :style="{ top: `${contextMenuPosition.y}px`, left: `${contextMenuPosition.x}px` }"
          @click.stop
        >
          <button 
            @click="handleHideChat"
            class="w-full text-left px-4 py-2.5 text-sm text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-colors font-medium flex items-center gap-3"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9.88 9.88a3 3 0 1 0 4.24 4.24"/><path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68"/><path d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61"/><line x1="2" x2="22" y1="2" y2="22"/></svg>
            不显示
          </button>
          
          <div class="h-px bg-slate-100 dark:bg-slate-700/50 my-1 mx-2"></div>

          <button 
            @click="handleDeleteChat"
            class="w-full text-left px-4 py-2.5 text-sm text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-900/20 transition-colors font-medium flex items-center gap-3"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/></svg>
            删除会话
          </button>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.animate-bounce-in {
  animation: bounceIn 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

@keyframes bounceIn {
  0% { transform: scale(0); }
  100% { transform: scale(1); }
}

/* 列表重排动画 */
.chat-list-move {
  transition: transform 0.3s ease-out;
}

/* 确保置顶的会话在动画过程中位于上层 */
.chat-item:first-child {
  z-index: 5;
}
</style>