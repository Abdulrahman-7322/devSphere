<script setup lang="ts">
import { ref } from 'vue'
import { chatService, type AddFriendVo } from '../../services/chatService'
import { noticeService } from '../../services/noticeService'

const props = defineProps<{
  show: boolean
}>()

const emit = defineEmits(['close'])

const searchQuery = ref('')
const isLoading = ref(false)
const errorMsg = ref('')
const searchResult = ref<AddFriendVo | null>(null)
const addStatus = ref<'idle' | 'sending' | 'success'>('idle')

const handleClose = () => {
  searchQuery.value = ''
  isLoading.value = false
  errorMsg.value = ''
  searchResult.value = null
  addStatus.value = 'idle'
  emit('close')
}

// 执行搜索
const handleSearch = async () => {
  if (!searchQuery.value) return
  isLoading.value = true
  errorMsg.value = ''
  searchResult.value = null
  addStatus.value = 'idle'
  
  try {
    // 核心修改：调用新接口
    const result = await chatService.searchForAdd({ query: searchQuery.value })
    if (result) {
      searchResult.value = result
    } else {
      errorMsg.value = '未找到该用户或群聊'
    }
  } catch (e: any) {
    errorMsg.value = e.message || '搜索失败'
  } finally {
    isLoading.value = false
  }
}

// 执行添加好友
const handleAddFriend = async (targetUserId: number) => {
  addStatus.value = 'sending'
  try {
    await noticeService.addFriend({
      userId: targetUserId,
      remark: '你好，我想添加你为好友'
    })
    addStatus.value = 'success'
  } catch (e: any) {
    errorMsg.value = e.message || '发送请求失败'
    addStatus.value = 'idle'
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
      <div v-if="show" class="fixed inset-0 z-[100] bg-slate-900/40 backdrop-blur-sm" @click="handleClose"></div>
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
        <div class="w-full max-w-md bg-white rounded-2xl shadow-xl pointer-events-auto relative overflow-hidden" @click.stop>
           
           <div class="flex items-center justify-between p-5 border-b border-slate-100">
             <h3 class="text-lg font-bold text-vibrant-main">添加朋友 / 群聊</h3>
             <button @click="handleClose" class="p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600 rounded-full transition-all">
               <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
             </button>
           </div>
           
           <div class="p-6 space-y-5">
             <form @submit.prevent="handleSearch" class="flex items-center gap-3">
               <input 
                 v-model="searchQuery"
                 type="text" 
                 placeholder="输入好友用户名 或 群聊ID" 
                 class="flex-1 h-11 px-4 rounded-xl bg-slate-50 border-2 border-transparent focus:bg-white focus:border-brand-blue/50 focus:ring-4 focus:ring-brand-blue/10 transition-all outline-none font-medium"
               />
               <button 
                 type="submit" 
                 :disabled="isLoading"
                 class="h-11 px-5 bg-brand-blue text-white font-bold rounded-xl hover:bg-brand-indigo active:scale-95 transition-all disabled:opacity-50"
               >
                 <svg v-if="isLoading" class="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class.opacity-75 fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
                 <span v-else>搜索</span>
               </button>
             </form>

             <div v-if="errorMsg" class="text-center text-sm text-functional-red p-4 bg-red-50 rounded-lg">
               {{ errorMsg }}
             </div>

             <div v-if="searchResult" class="v-card p-4 flex items-center gap-4 animate-fadeIn">
               <img :src="searchResult.avatar" class="h-14 w-14 rounded-xl bg-slate-100" />
               <div class="flex-1">
                 <div class="font-bold text-vibrant-main">{{ searchResult.name }}</div>
                 <div class="text-xs text-vibrant-muted mt-1">
                   {{ searchResult.type === 2 ? '好友' : '群聊' }} (ID: {{ searchResult.uid || searchResult.roomId }})
                 </div>
               </div>
               
               <div v-if="searchResult.friendTarget === 1" class="px-3 py-1.5 rounded-md text-sm font-medium text-slate-400 bg-slate-100">
                 已添加
               </div>

               <template v-else>
                 <button v-if="addStatus === 'idle'" @click="handleAddFriend(searchResult.uid)" class="px-4 py-2 bg-brand-blue text-white text-sm font-bold rounded-xl hover:bg-brand-indigo active:scale-95 transition-all">
                   添加好友
                 </button>
                 <button v-if="addStatus === 'sending'" disabled class="px-4 py-2 bg-slate-200 text-slate-500 text-sm font-bold rounded-xl">
                   发送中...
                 </button>
                 <div v-if="addStatus === 'success'" class="px-3 py-1.5 rounded-md text-sm font-medium text-functional-green bg-green-50">
                   已发送
                 </div>
               </template>
             </div>
           </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.animate-fadeIn {
  animation: fadeIn 0.3s ease-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>