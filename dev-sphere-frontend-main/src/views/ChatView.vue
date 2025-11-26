<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import ChatList from '../components/chat/ChatList.vue'
import ChatBox from '../components/chat/ChatBox.vue'
import AddFriendModal from '../components/chat/AddFriendModal.vue'
import CreateGroupModal from '../components/chat/CreateGroupModal.vue'
import GroupDetailModal from '../components/chat/GroupDetailModal.vue'
import ContactList from '../components/chat/ContactList.vue'
import Sidebar from '../components/chat/Sidebar.vue'
import { useChatStore } from '../stores/chatStore'

const chatStore = useChatStore()

// 模态框状态
const isAddFriendModalOpen = ref(false)
const isCreateGroupModalOpen = ref(false)

// 侧边栏 Tab 控制
const activeTab = ref('chat') // 'chat' | 'contacts'

const handleTabChange = (tab: string) => {
  // 路由跳转已由Sidebar处理,这里只需要更新内部状态
  if (tab === 'chat' || tab === 'contacts') {
    activeTab.value = tab
  }
}

// 动态切换列表组件
const activeSideComponent = computed(() => {
  return activeTab.value === 'chat' ? ChatList : ContactList
})

onMounted(() => {
  chatStore.loadSessionList()
})
</script>

<template>
  <!-- 全屏容器：使用 Flex 布局，禁止溢出 -->
  <div class="flex h-full w-full overflow-hidden bg-[#F5F7FA] dark:bg-slate-900 text-slate-900 dark:text-white transition-colors duration-300 font-sans">
    
    <!-- 1. 侧边导航栏 (固定宽度) -->
    <Sidebar @tab-change="handleTabChange" class="shrink-0" />

    <!-- 2. 列表区域 (固定宽度 320px) -->
    <aside class="w-[320px] flex flex-col h-full bg-white dark:bg-slate-900 border-r border-slate-200/50 dark:border-slate-700/50 transition-colors duration-300 z-40 relative">
      <keep-alive>
        <component :is="activeSideComponent" />
      </keep-alive>
    </aside>

    <!-- 3. 聊天主区域 (自适应剩余空间) -->
    <main class="flex-1 flex flex-col h-full relative min-w-0 bg-[#F5F7FA] dark:bg-slate-900">
      <!-- 空状态 (未选择会话时显示) -->
      <div v-if="!chatStore.activeRoomId" class="flex-1 flex flex-col items-center justify-center text-slate-300 dark:text-slate-600 select-none animate-in fade-in duration-700">
         <div class="p-10 bg-white dark:bg-slate-800 rounded-full mb-8 shadow-xl shadow-slate-200/50 dark:shadow-black/20">
            <svg xmlns="http://www.w3.org/2000/svg" width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round" class="text-blue-500/80"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/><path d="M13 8H7"/><path d="M17 12H7"/></svg>
         </div>
         <p class="text-2xl font-bold text-slate-700 dark:text-slate-200 tracking-tight">CodeArena Chat</p>
         <p class="text-base mt-3 text-slate-400 dark:text-slate-500">选择一个联系人，开始高效沟通</p>
      </div>
      
      <!-- 聊天窗口 -->
      <ChatBox v-else class="h-full w-full" />
    </main>

    <!-- 全局模态框 -->
    <AddFriendModal :show="isAddFriendModalOpen" @close="isAddFriendModalOpen = false" />
    <CreateGroupModal :show="isCreateGroupModalOpen" @close="isCreateGroupModalOpen = false" />
    <GroupDetailModal />
  </div>
</template>

<style>
/* 全局滚动条样式优化 */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  @apply bg-slate-200 dark:bg-slate-700 rounded-full hover:bg-slate-300 dark:hover:bg-slate-600 transition-colors;
}
</style>