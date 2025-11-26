<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import { useChatStore, type ChatMessage, MsgType } from '../../stores/chatStore'
import { useUserStore } from '../../stores/userStore'
import ChatDetailModal from './ChatDetailModal.vue'
import GroupDetailModal from './GroupDetailModal.vue'

const chatStore = useChatStore()
const userStore = useUserStore()
const messageInput = ref('')
const messagesContainer = ref<HTMLElement | null>(null)

// [V7] 状态：是否正在加载更多历史消息
const isLoadingMore = ref(false)

// [V6] 宽限期活动通知
const handleActivity = () => {
  chatStore.handleUserActivity()
}

// [V7] 滚动到顶部的处理函数（加载历史）
const handleScrollTop = async () => {
  const el = messagesContainer.value
  if (!el) return

  // 如果已经在加载中，直接返回
  if (isLoadingMore.value) return

  // 小于 5px 视为到顶（更稳妥），避免精度问题
  if (el.scrollTop > 5) return

  const currentRoomId = chatStore.activeRoomId
  if (!currentRoomId) return

  // 检查 store 中是否还有更多数据
  const pagination = chatStore.roomPagination[currentRoomId]
  if (pagination && !pagination.hasMore) {
    // 没有更多了
    return
  }

  // 开始加载
  isLoadingMore.value = true

  // 记录加载前的高度
  const oldScrollHeight = el.scrollHeight

  try {
    // 加载历史（store 内会把新消息 prepend 到前面）
    await chatStore.loadMoreMessages(currentRoomId)

    // 等待 DOM 更新（确保消息插入）
    await nextTick()

    // 等到浏览器布局稳定后恢复滚动
    requestAnimationFrame(() => {
      const newScrollHeight = el.scrollHeight
      // 恢复到用户看到的位置（新高度 - 老高度）
      el.scrollTop = newScrollHeight - oldScrollHeight
    })
  } catch (error) {
    console.error('加载更多历史消息失败:', error)
  } finally {
    // 稍微延迟以避免抖动
    setTimeout(() => {
      isLoadingMore.value = false
    }, 120)
  }
}

// 发送消息
const handleSend = (e?: KeyboardEvent) => {
  if (e && e.shiftKey) return
  const content = messageInput.value.trim()
  if (!content) return
  chatStore.sendMessage(content)
  messageInput.value = ''
  // 发送后立即滚动到底（auto）
  scrollToBottom('auto')
}

// 菜单按钮
const handleMenuClick = () => {
  if (!chatStore.activeConversation) return
  if (chatStore.activeConversation.type === MsgType.GROUP) {
    chatStore.openGroupDetailModal(chatStore.activeConversation.id)
  } else {
    chatStore.openChatDetailModal()
  }
}

// 滚动到底部
const scrollToBottom = async (behavior: 'smooth' | 'auto' = 'smooth') => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTo({
      top: messagesContainer.value.scrollHeight,
      behavior,
    })
    
    // 如果是初始加载（auto），滚动完成后显示内容
    if (behavior === 'auto' && isInitialLoad.value) {
      // 稍微延迟一帧确保渲染完成
      requestAnimationFrame(() => {
        isInitialLoad.value = false
      })
    }
  }
}

// 判断是否需要显示时间戳（5 分钟间隔）
const shouldShowTime = (currentMsg: ChatMessage, index: number) => {
  if (index === 0) return true
  const prevMsg = chatStore.activeMessages[index - 1]
  if (!prevMsg || !prevMsg.time || !currentMsg.time) return true
  
  const currTime = currentMsg.time instanceof Date ? currentMsg.time.getTime() : new Date(currentMsg.time).getTime()
  const prevTime = prevMsg.time instanceof Date ? prevMsg.time.getTime() : new Date(prevMsg.time).getTime()
  
  return currTime - prevTime > 5 * 60 * 1000 // 5 分钟
}

// 格式化时间戳，健壮处理无效日期
const formatMessageTime = (date: Date | string | null) => {
  const d = date ? (date instanceof Date ? date : new Date(date)) : new Date()
  if (isNaN(d.getTime())) return ''
  const now = new Date()
  if (d.toDateString() === now.toDateString()) {
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }
  return d.toLocaleString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

// 辅助：消息 key 生成（保证唯一且稳定）
const messageKey = (msg: ChatMessage, index: number) => {
  if (msg.id) return String(msg.id)
  if (msg.tempId) return msg.tempId
  return `idx_${index}`
}

// ---------- 滚动/加载交互逻辑 ----------

// [V8] 初始加载状态 (用于隐藏滚动过程)
const isInitialLoad = ref(false)

// ---------- 滚动/加载交互逻辑 ----------

// 1) 监听 activeRoomId
watch(
  () => chatStore.activeRoomId,
  (newId) => {
    if (!newId) return
    
    // 切换房间时，先隐藏内容，避免看到从上往下滚动的过程
    isInitialLoad.value = true

    if (chatStore.lastOpenedLoadedRoomId === newId) {
      scrollToBottom('auto')
      chatStore.lastOpenedLoadedRoomId = null as any
      return
    }
    const stop = watch(
      () => chatStore.lastOpenedLoadedRoomId,
      (val) => {
        if (val === newId) {
          scrollToBottom('auto')
          chatStore.lastOpenedLoadedRoomId = null as any
          stop()
        }
      },
      { immediate: true }
    )
  },
  { immediate: true }
)

// 2) 监听消息长度变化
watch(
  () => chatStore.activeMessages.length,
  async (newLength, oldLength) => {
    if (newLength <= oldLength) return
    if (isLoadingMore.value) return
    if (chatStore.lastMessageDirection === 'append') {
      await scrollToBottom('smooth')
      chatStore.lastMessageDirection = null as any
    }
  }
)
</script>

<template>
  <div class="flex flex-col h-full bg-[#F5F7FA] dark:bg-slate-900 transition-colors duration-300 relative" @click="handleActivity">
    
    <!-- 1. 顶部 Header (毛玻璃效果) -->
    <div class="h-[72px] px-6 flex items-center justify-between border-b border-slate-200/50 dark:border-slate-700/50 bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl z-20 sticky top-0 transition-all">
      <div class="flex items-center gap-4">
        <div class="flex flex-col">
          <div class="flex items-center gap-2">
            <span class="font-bold text-lg text-slate-900 dark:text-white tracking-tight">{{ chatStore.activeConversation?.name }}</span>
            <span
              v-if="chatStore.activeConversation?.type === MsgType.GROUP && chatStore.activeConversation?.memberCount"
              class="px-2 py-0.5 bg-slate-100 dark:bg-slate-800 text-slate-500 text-xs rounded-full font-medium"
            >
              {{ chatStore.activeConversation.memberCount }}人
            </span>
          </div>
          <!-- 在线状态 -->
          <div v-if="chatStore.activeConversation?.isOnline" class="flex items-center gap-1.5 mt-0.5">
             <span class="relative flex h-2 w-2">
              <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-500 opacity-75"></span>
              <span class="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
            </span>
            <span class="text-xs text-emerald-600 dark:text-emerald-400 font-medium">在线</span>
          </div>
        </div>
      </div>
      
      <!-- 更多操作按钮 -->
      <button @click="handleMenuClick" class="p-2.5 text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-white rounded-xl transition-all active:scale-95">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/><circle cx="5" cy="12" r="1"/></svg>
      </button>
    </div>

    <!-- 2. 消息区域 -->
    <div
      ref="messagesContainer"
      @scroll="handleScrollTop"
      class="flex-1 overflow-y-auto p-6 space-y-8 custom-scrollbar transition-opacity duration-200"
      :class="{ 'opacity-0': isInitialLoad }"
    >
      <!-- 加载 Loading -->
      <div v-if="isLoadingMore" class="flex justify-center py-4">
        <div class="w-8 h-8 border-4 border-blue-100 dark:border-slate-700 border-t-blue-500 rounded-full animate-spin"></div>
      </div>

      <template v-for="(msg, index) in chatStore.activeMessages" :key="messageKey(msg, index)">
        <!-- 时间分割线 -->
        <div v-if="shouldShowTime(msg, index)" class="flex justify-center my-6">
          <span class="text-xs font-medium text-slate-400 dark:text-slate-500 bg-slate-200/50 dark:bg-slate-800/50 px-3 py-1 rounded-full backdrop-blur-sm">
            {{ formatMessageTime(msg.time) }}
          </span>
        </div>

        <!-- 消息体 -->
        <div
          class="flex gap-4 group"
          :class="String(msg.senderId) === String(userStore.userInfo?.id) ? 'flex-row-reverse' : ''"
        >
          <!-- 头像 -->
          <img
            :src="msg.senderAvatar"
            class="h-10 w-10 rounded-2xl bg-white dark:bg-slate-800 border-2 border-white dark:border-slate-700 shadow-sm shrink-0 cursor-pointer hover:scale-105 transition-transform self-start mt-1"
            :title="msg.senderName"
          />

          <div class="flex flex-col max-w-[70%]" :class="String(msg.senderId) === String(userStore.userInfo?.id) ? 'items-end' : 'items-start'">
            <!-- 群聊显示昵称 -->
            <div v-if="chatStore.activeConversation?.type === MsgType.GROUP && String(msg.senderId) !== String(userStore.userInfo?.id)" class="text-[11px] text-slate-400 dark:text-slate-500 mb-1 px-1 font-medium">
              {{ msg.senderName }}
            </div>

            <div class="relative">
              <!-- 气泡 -->
              <div
                class="px-5 py-3 text-[15px] leading-relaxed shadow-sm transition-all duration-200"
                :class="[
                  'whitespace-pre-wrap',
                  'break-words',
                  String(msg.senderId) === String(userStore.userInfo?.id)
                    ? 'bg-gradient-to-br from-blue-500 to-indigo-600 text-white rounded-2xl rounded-tr-sm shadow-blue-500/20'
                    : 'bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 border border-slate-100 dark:border-slate-700 rounded-2xl rounded-tl-sm',
                  msg.status === 'sending' ? 'opacity-70' : ''
                ]"
              >
                {{ msg.content }}
              </div>

              <!-- 状态图标 (发送中/失败) -->
              <div
                class="absolute top-1/2 -translate-y-1/2 flex items-center gap-2 transition-all duration-200"
                :class="[
                  String(msg.senderId) === String(userStore.userInfo?.id) ? '-left-8' : '-right-8'
                ]"
              >
                <div v-if="msg.status === 'sending'" class="w-4 h-4 border-2 border-slate-300 border-t-blue-500 rounded-full animate-spin"></div>
                <svg v-else-if="msg.status === 'error'" class="h-5 w-5 text-rose-500 cursor-pointer hover:scale-110 transition-transform" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"><path d="M12 22C6.47715 22 2 17.5228 2 12C2 6.47715 6.47715 2 12 2C17.5228 2 22 6.47715 22 17.5228 17.5228 22 12 22ZM11 15V17H13V15H11ZM11 7V13H13V7H11Z"></path></svg>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 3. 底部输入区 (悬浮式设计) -->
    <div class="p-6 pt-2 bg-[#F5F7FA] dark:bg-slate-900 transition-colors z-20">
      <div class="bg-white dark:bg-slate-800 rounded-3xl border border-slate-200 dark:border-slate-700 shadow-sm focus-within:shadow-lg focus-within:border-blue-500/30 dark:focus-within:border-blue-500/30 transition-all duration-300 overflow-hidden">
        
        <!-- 工具栏 -->
        <div class="flex items-center gap-1 px-3 pt-2 text-slate-400 dark:text-slate-500">
          <button class="p-2 hover:bg-slate-100 dark:hover:bg-slate-700/50 rounded-xl transition-colors hover:text-blue-500" title="表情"><svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M8 14s1.5 2 4 2 4-2 4-2"/><line x1="9" x2="9.01" y1="9" y2="9"/><line x1="15" x2="15.01" y1="9" y2="9"/></svg></button>
          <button class="p-2 hover:bg-slate-100 dark:hover:bg-slate-700/50 rounded-xl transition-colors hover:text-blue-500" title="发送图片"><svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="3" rx="2" ry="2"/><circle cx="9" cy="9" r="2"/><path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21"/></svg></button>
          <button class="p-2 hover:bg-slate-100 dark:hover:bg-slate-700/50 rounded-xl transition-colors hover:text-blue-500" title="发送文件"><svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m21.44 11.05-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg></button>
          <div class="h-4 w-px bg-slate-200 dark:bg-slate-700 mx-2"></div>
          <button class="p-2 hover:bg-slate-100 dark:hover:bg-slate-700/50 rounded-xl transition-colors hover:text-blue-500" title="代码块"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg></button>
        </div>

        <!-- 输入框 -->
        <textarea
          v-model="messageInput"
          @keydown.enter="handleSend"
          placeholder="输入消息..."
          class="w-full h-24 px-5 py-2 bg-transparent resize-none text-[15px] text-slate-800 dark:text-slate-100 placeholder-slate-400 dark:placeholder-slate-500 outline-none custom-scrollbar leading-relaxed"
        ></textarea>
        
        <!-- 发送按钮 -->
        <div class="flex justify-end px-4 pb-4">
           <button
            @click="handleSend()"
            :disabled="!messageInput.trim()"
            class="px-6 py-2 bg-blue-600 text-white text-sm font-bold rounded-xl hover:bg-blue-700 hover:shadow-lg hover:shadow-blue-500/30 active:scale-95 transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:shadow-none flex items-center gap-2"
          >
            <span>发送</span>
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
          </button>
        </div>
      </div>
    </div>
    <!-- Modals -->
    <GroupDetailModal />
    <ChatDetailModal />
  </div>
</template>
