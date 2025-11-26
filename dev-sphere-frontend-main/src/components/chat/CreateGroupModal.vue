<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { chatService, type FriendVo } from '../../services/chatService'
import { useChatStore } from '../../stores/chatStore'
import { useUserStore } from '../../stores/userStore'

const props = defineProps<{
  show: boolean
}>()

const emit = defineEmits(['close'])

const chatStore = useChatStore()
const userStore = useUserStore()

const isLoading = ref(false)
const errorMsg = ref('')
const groupName = ref('')
const friendList = ref<FriendVo[]>([])
const selectedFriendIds = ref<number[]>([])

// 搜索好友
const friendSearchQuery = ref('')
const filteredFriendList = computed(() => {
  if (!friendSearchQuery.value) {
    return friendList.value
  }
  return friendList.value.filter(friend => 
    friend.name.toLowerCase().includes(friendSearchQuery.value.toLowerCase())
  )
})

// 已选好友列表
const selectedFriends = computed(() => {
  return friendList.value.filter(friend => 
    selectedFriendIds.value.includes(friend.uid)
  )
})

// 拉取好友列表
const fetchFriends = async () => {
  isLoading.value = true
  try {
    const result = await chatService.getFriendList()
    // 假设好友列表在 type 2 (私聊)
    const friends = result.find(group => group.type === 2)?.content || []
    friendList.value = friends
  } catch (e: any) {
    errorMsg.value = '好友列表加载失败'
  } finally {
    isLoading.value = false
  }
}

// 弹窗显示时，加载数据
watch(() => props.show, (newVal) => {
  if (newVal) {
    fetchFriends()
  } else {
    // 关闭时重置状态
    groupName.value = ''
    selectedFriendIds.value = []
    friendSearchQuery.value = ''
    errorMsg.value = ''
  }
})

// 创建群聊
const handleCreateGroup = async () => {
  if (!groupName.value) {
    errorMsg.value = '请输入群聊名称'
    return
  }
  if (selectedFriendIds.value.length < 2) {
    errorMsg.value = '至少选择 2 位好友才能创建群聊'
    return
  }

  isLoading.value = true
  errorMsg.value = ''
  
  try {
    // 加上自己
    const allUserIds = [...selectedFriendIds.value, userStore.userInfo!.id]
    
    // 调用(模拟的)创建接口
    const newGroup = await chatService.createGroup({
      name: groupName.value,
      userIds: allUserIds
    })
    
    // 成功后：
    // 1. 重新加载会话列表 (最简单粗暴但有效)
    chatStore.loadSessionList()
    // 2. 关闭弹窗
    emit('close')
    
  } catch (e: any) {
    errorMsg.value = e.message || '创建失败'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition 
      enter-active-class="transition duration-300 ease-out" 
      enter-from-class="opacity-0" 
      enter-to-class="opacity-100" 
      leave-active-class="transition duration-200 ease-in" 
      leave-from-class="opacity-100" 
      leave-to-class="opacity-0"
    >
      <div v-if="show" class="fixed inset-0 z-[100] bg-slate-900/40 backdrop-blur-sm" @click="emit('close')"></div>
    </Transition>

    <Transition
      enter-active-class="transition duration-400 ease-out-cubic"
      enter-from-class="opacity-0 scale-95 translate-y-8"
      enter-to-class="opacity-100 scale-100 translate-y-0"
      leave-active-class="transition duration-300 ease-in-cubic"
      leave-from-class="opacity-100 scale-100 translate-y-0"
      leave-to-class="opacity-0 scale-95 translate-y-8"
    >
      <div v-if="show" class="fixed inset-0 z-[101] flex items-center justify-center p-4 pointer-events-none">
        <div class="w-full max-w-lg bg-white rounded-2xl shadow-xl pointer-events-auto relative overflow-hidden flex flex-col h-[70vh]" @click.stop>
           
           <div class="flex items-center justify-between p-5 border-b border-slate-100 shrink-0">
             <h3 class="text-lg font-bold text-vibrant-main">创建群聊</h3>
             <button @click="emit('close')" class="p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600 rounded-full transition-all">
               <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
             </button>
           </div>
           
           <div class="p-6 flex-1 min-h-0 flex flex-col gap-5">
             <div class="space-y-1.5">
               <label class="text-xs font-bold text-vibrant-muted ml-1 uppercase tracking-wider">群聊名称</label>
               <input 
                 v-model="groupName"
                 type="text" 
                 placeholder="给你的群起个名字吧" 
                 class="w-full h-11 px-4 rounded-xl bg-slate-50 border-2 border-transparent focus:bg-white focus:border-brand-blue/50 focus:ring-4 focus:ring-brand-blue/10 transition-all outline-none font-medium"
               />
             </div>
             
             <div class="flex-1 min-h-0 flex flex-col border border-slate-200 rounded-lg">
                <div class="p-3 border-b border-slate-100">
                  <input 
                    v-model="friendSearchQuery"
                    type="text" 
                    placeholder="搜索好友" 
                    class="w-full h-9 px-4 rounded-lg bg-slate-100 border-transparent text-sm"
                  />
                </div>
                <div class="flex-1 overflow-y-auto custom-scrollbar p-3 space-y-2">
                  <div v-if="isLoading" class="text-center text-sm text-slate-400 py-4">好友加载中...</div>
                  <div v-else-if="filteredFriendList.length === 0" class="text-center text-sm text-slate-400 py-4">暂无好友或无搜索结果</div>
                  
                  <label 
                    v-for="friend in filteredFriendList" 
                    :key="friend.uid"
                    class="flex items-center gap-3 p-2.5 rounded-lg hover:bg-slate-100 cursor-pointer"
                  >
                    <input 
                      type="checkbox" 
                      :value="friend.uid" 
                      v-model="selectedFriendIds"
                      class="w-5 h-5 rounded text-brand-blue accent-brand-blue focus:ring-brand-blue/50"
                    />
                    <img :src="friend.avatar" class="h-10 w-10 rounded-full bg-slate-200" />
                    <span class="font-medium text-vibrant-main">{{ friend.name }}</span>
                  </label>
                </div>
             </div>
           </div>

           <div class="p-5 border-t border-slate-100 flex justify-between items-center shrink-0">
             <div class="text-sm text-slate-500">
               已选择 <span class="font-bold text-brand-blue">{{ selectedFriendIds.length }}</span> 人
             </div>
             <button
               @click="handleCreateGroup"
               :disabled="isLoading || selectedFriendIds.length < 2"
               class="h-11 px-6 bg-brand-blue text-white font-bold rounded-xl hover:bg-brand-indigo active:scale-95 transition-all disabled:opacity-50"
             >
               {{ isLoading ? '创建中...' : '立即创建' }}
             </button>
           </div>
        </div>
      </div>
    </Transition>
  </Teleport>
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