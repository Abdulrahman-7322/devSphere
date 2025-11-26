<script setup lang="ts">
import { ref, watch } from 'vue'
import { useUserStore } from '../../stores/userStore'
import { userService } from '../../services/userService'

const props = defineProps<{
  show: boolean
}>()

const emit = defineEmits(['close', 'success'])
const userStore = useUserStore()

const form = ref({
  realName: '',
  gender: 0,
  mobile: '',
  email: ''
})

const isLoading = ref(false)

// Initialize form when modal opens
watch(() => props.show, (newVal) => {
  if (newVal && userStore.userInfo) {
    form.value = {
      realName: userStore.userInfo.realName || '',
      gender: userStore.userInfo.gender || 0, // Assuming 0: Unknown, 1: Male, 2: Female
      mobile: userStore.userInfo.mobile || '',
      email: userStore.userInfo.email || ''
    }
  }
})

const handleClose = () => {
  emit('close')
}

const handleSubmit = async () => {
  isLoading.value = true
  try {
    const updateData = {
      id: userStore.userInfo?.id,
      ...form.value
    }
    
    await userService.updateUserInfo(updateData)
    
    // Update local store
    if (userStore.userInfo) {
      userStore.login(userStore.token || '', {
        ...userStore.userInfo,
        ...form.value
      })
    }
    
    emit('success')
    handleClose()
  } catch (error) {
    console.error('Update failed:', error)
    alert('更新失败，请重试')
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
        <div class="w-full max-w-md bg-white dark:bg-slate-800 rounded-2xl shadow-xl pointer-events-auto relative overflow-hidden" @click.stop>
           
           <div class="flex items-center justify-between p-5 border-b border-slate-100 dark:border-slate-700">
             <h3 class="text-lg font-bold text-slate-900 dark:text-white">编辑个人资料</h3>
             <button @click="handleClose" class="p-2 text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-700 hover:text-slate-600 dark:hover:text-slate-300 rounded-full transition-all">
               <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
             </button>
           </div>
           
           <div class="p-6 space-y-4">
             <div class="space-y-1">
               <label class="text-sm font-medium text-slate-700 dark:text-slate-300">真实姓名</label>
               <input v-model="form.realName" type="text" class="w-full px-4 py-2 rounded-xl bg-slate-50 dark:bg-slate-700 border border-slate-200 dark:border-slate-600 focus:ring-2 focus:ring-violet-500/20 focus:border-violet-500 outline-none transition-all text-slate-900 dark:text-white" />
             </div>

             <div class="space-y-1">
               <label class="text-sm font-medium text-slate-700 dark:text-slate-300">性别</label>
               <div class="flex gap-4">
                 <label class="flex items-center gap-2 cursor-pointer">
                   <input type="radio" v-model="form.gender" :value="1" class="text-violet-600 focus:ring-violet-500" />
                   <span class="text-slate-700 dark:text-slate-300">男</span>
                 </label>
                 <label class="flex items-center gap-2 cursor-pointer">
                   <input type="radio" v-model="form.gender" :value="2" class="text-violet-600 focus:ring-violet-500" />
                   <span class="text-slate-700 dark:text-slate-300">女</span>
                 </label>
                 <label class="flex items-center gap-2 cursor-pointer">
                   <input type="radio" v-model="form.gender" :value="0" class="text-violet-600 focus:ring-violet-500" />
                   <span class="text-slate-700 dark:text-slate-300">保密</span>
                 </label>
               </div>
             </div>

             <div class="space-y-1">
               <label class="text-sm font-medium text-slate-700 dark:text-slate-300">手机号</label>
               <input v-model="form.mobile" type="tel" class="w-full px-4 py-2 rounded-xl bg-slate-50 dark:bg-slate-700 border border-slate-200 dark:border-slate-600 focus:ring-2 focus:ring-violet-500/20 focus:border-violet-500 outline-none transition-all text-slate-900 dark:text-white" />
             </div>

             <div class="space-y-1">
               <label class="text-sm font-medium text-slate-700 dark:text-slate-300">邮箱</label>
               <input v-model="form.email" type="email" class="w-full px-4 py-2 rounded-xl bg-slate-50 dark:bg-slate-700 border border-slate-200 dark:border-slate-600 focus:ring-2 focus:ring-violet-500/20 focus:border-violet-500 outline-none transition-all text-slate-900 dark:text-white" />
             </div>

             <div class="pt-4 flex justify-end gap-3">
               <button @click="handleClose" class="px-4 py-2 text-slate-600 dark:text-slate-300 font-medium hover:bg-slate-100 dark:hover:bg-slate-700 rounded-lg transition-colors">取消</button>
               <button 
                 @click="handleSubmit" 
                 :disabled="isLoading"
                 class="px-6 py-2 bg-violet-600 text-white font-medium rounded-lg hover:bg-violet-700 active:scale-95 transition-all disabled:opacity-50 flex items-center gap-2"
               >
                 <svg v-if="isLoading" class="animate-spin h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
                 保存
               </button>
             </div>
           </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
