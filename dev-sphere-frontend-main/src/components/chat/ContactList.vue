<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { chatService, type FriendContentVo, type FriendVo } from '../../services/chatService'
import { useChatStore, MsgType } from '../../stores/chatStore'

const chatStore = useChatStore()
const emit = defineEmits(['switch-to-chat'])

// (修改) 从 store 获取数据
const isLoading = computed(() => chatStore.isLoadingContacts)
const contactGroups = computed(() => chatStore.contactGroups)

// (新增) 控制哪个菜单是打开的
const openMenuId = ref<string | null>(null)

// (修改) 仅在 store 列表为空时加载
onMounted(() => {
  if (chatStore.contactGroups.length === 0) {
    chatStore.loadContactList()
  }
})

// (新增) 搜索功能
const searchQuery = ref('')
const filteredContactGroups = computed(() => {
  if (!searchQuery.value) {
    return contactGroups.value // 返回原始分组
  }
  
  const query = searchQuery.value.toLowerCase()
  const filteredGroups: FriendContentVo[] = []

  for (const group of contactGroups.value) {
    const filteredContent = group.content.filter(contact => 
      contact.name.toLowerCase().includes(query)
    )
    if (filteredContent.length > 0) {
      // 复制 group 并替换 content
      filteredGroups.push({
        ...group,
        content: filteredContent
      })
    }
  }
  return filteredGroups
})

// 点击联系人
const handleContactClick = (contact: FriendVo) => {
  if (!contact.roomId) {
    alert("该好友暂无聊天室，数据异常")
    return
  }
  chatStore.setActiveRoom(contact.roomId)
  emit('switch-to-chat')
}

// (新增) 打开菜单
const toggleMenu = (e: MouseEvent, id: number | string) => {
  e.stopPropagation() // 阻止触发 handleContactClick
  openMenuId.value = openMenuId.value === id ? null : String(id)
}

// (新增) 修改备注
const handleEditRemark = (contact: FriendVo) => {
  const newRemark = window.prompt("请输入新的好友备注：", contact.name) // 暂用 prompt 简化
  if (newRemark !== null) {
    chatStore.updateFriendRemark(contact.uid, newRemark)
  }
  openMenuId.value = null
}

// (新增) 删除好友
const handleDeleteFriend = (contact: FriendVo) => {
  if (window.confirm(`确定要删除好友 "${contact.name}" 吗？`)) {
    chatStore.deleteFriend(contact.uid)
  }
  openMenuId.value = null
}

</script>

<template>
  <div class="flex flex-col h-full">
    <div class="p-4 py-3 border-b border-slate-100">
      <div class="relative group">
         <span class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">
           <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
         </span>
         <input 
           v-model="searchQuery"
           type="text" 
           placeholder="搜索好友或群聊..." 
           class="w-full h-9 pl-10 pr-4 rounded-lg bg-slate-200/50 border-transparent text-sm focus:bg-white focus:border-brand-blue/50 focus:ring-2 focus:ring-brand-blue/20 transition-all outline-none" 
         />
      </div>
    </div>

    <div v-if="isLoading" class="flex-1 flex items-center justify-center text-sm text-slate-400">
      <svg class="animate-spin h-5 w-5 text-slate-300 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
      加载中...
    </div>

    <div v-else class="flex-1 overflow-y-auto py-2 custom-scrollbar">
      <div v-if="filteredContactGroups.length === 0" class="text-center text-sm text-slate-400 pt-10">
        {{ searchQuery ? '无搜索结果' : '暂无联系人' }}
      </div>
      
      <div v-for="group in filteredContactGroups" :key="group.type" class="mb-2">
        <h4 class="px-4 py-1.5 text-xs font-bold text-vibrant-muted/80 uppercase tracking-wider">
          {{ group.typeName }}
          <span class="font-normal">({{ group.content.length }})</span>
        </h4>
        
        <div 
          v-for="contact in group.content" 
          :key="contact.roomId" 
          @click="handleContactClick(contact)"
          class="flex items-center gap-3 px-4 py-2.5 mx-2 rounded-lg cursor-pointer hover:bg-slate-100 transition-colors relative group/item"
        >
          <img :src="contact.avatar" class="h-10 w-10 rounded-xl bg-slate-200" />
          <div class="flex-1 min-w-0">
            <h3 class="font-bold text-vibrant-main text-sm truncate">{{ contact.name }}</h3>
          </div>
          
          <div v-if="group.type === MsgType.PRIVATE" class="relative z-10">
            <button 
              @click.stop="toggleMenu($event, contact.uid)" 
              class="p-1 rounded-md text-slate-400 opacity-0 group-hover/item:opacity-100 hover:bg-slate-200 hover:text-slate-700 transition-all"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/><circle cx="5" cy="12" r="1"/></svg>
            </button>
            
            <Transition
              enter-active-class="transition duration-100 ease-out"
              enter-from-class="transform scale-95 opacity-0"
              enter-to-class="transform scale-100 opacity-100"
              leave-active-class="transition duration-75 ease-in"
              leave-from-class="transform scale-100 opacity-100"
              leave-to-class="transform scale-95 opacity-0"
            >
              <div 
                v-if="openMenuId === String(contact.uid)" 
                @blur="openMenuId = null" 
                @click.stop 
                class="absolute top-8 right-0 w-40 bg-white shadow-xl rounded-lg border border-slate-100 p-2 z-20"
              >
                <button @click="handleEditRemark(contact)" class="w-full flex items-center gap-2 px-3 py-2 rounded-md hover:bg-slate-100 text-sm font-medium text-vibrant-main">
                  修改备注
                </button>
                <button @click="handleDeleteFriend(contact)" class="w-full flex items-center gap-2 px-3 py-2 rounded-md hover:bg-red-50 text-sm font-medium text-functional-red">
                  删除好友
                </button>
              </div>
            </Transition>
          </div>
          
        </div>
      </div>
    </div>
  </div>
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