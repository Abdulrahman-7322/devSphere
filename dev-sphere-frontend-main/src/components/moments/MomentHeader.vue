<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/userStore'
import { ossService } from '../../services/ossService'

const userStore = useUserStore()
const user = computed(() => userStore.userInfo)
const avatar = computed(() => userStore.userAvatar)

// 默认海边背景
const DEFAULT_COVER = 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=2073&auto=format&fit=crop'
const coverUrl = ref(DEFAULT_COVER)
const fileInput = ref<HTMLInputElement | null>(null)
const isUploading = ref(false)

onMounted(() => {
  // 从本地存储加载用户自定义背景
  const savedCover = localStorage.getItem(`moment_cover_${userStore.userInfo?.id}`)
  if (savedCover) {
    coverUrl.value = savedCover
  }
})

const handleCoverClick = () => {
  fileInput.value?.click()
}

const handleFileChange = async (e: Event) => {
  const target = e.target as HTMLInputElement
  const files = target.files
  if (!files || files.length === 0) return

  const file = files[0]
  if (!file) return

  isUploading.value = true
  
  try {
    const res = await ossService.upload(file)
    // 兼容不同的返回格式
    const url = (res as any).data?.url || (res as any).url
    
    if (url) {
      coverUrl.value = url
      // 保存到本地存储
      if (userStore.userInfo?.id) {
        localStorage.setItem(`moment_cover_${userStore.userInfo.id}`, url)
      }
    }
  } catch (error) {
    console.error('Upload cover failed:', error)
    alert('更换封面失败，请重试')
  } finally {
    isUploading.value = false
    // 清空 input 以便下次选择同一文件
    if (fileInput.value) fileInput.value.value = ''
  }
}

const emit = defineEmits(['publish'])
</script>

<template>
  <div class="relative mb-16">
    <!-- 封面图 (高度增加到 h-80) -->
    <div class="h-80 w-full overflow-hidden relative group cursor-pointer" @click="handleCoverClick">
      <img 
        :src="coverUrl" 
        class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
        alt="cover"
      />
      <div class="absolute inset-0 bg-gradient-to-t from-black/30 to-transparent"></div>
      
      <!-- 上传提示 -->
      <div class="absolute inset-0 flex items-center justify-center bg-black/20 opacity-0 group-hover:opacity-100 transition-opacity">
        <span class="text-white font-medium border border-white/50 px-4 py-1 rounded-full backdrop-blur-sm">
          {{ isUploading ? '上传中...' : '点击更换封面' }}
        </span>
      </div>

      <!-- 隐藏的文件输入 -->
      <input 
        ref="fileInput"
        type="file" 
        accept="image/*" 
        class="hidden" 
        @change="handleFileChange" 
        @click.stop
      />
      
      <!-- 发布按钮 (悬浮在封面右下角) -->
      <button 
        @click.stop="emit('publish')"
        class="absolute top-4 right-4 p-2 bg-white/20 backdrop-blur-md hover:bg-white/30 rounded-full transition-all text-white"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></svg>
      </button>
    </div>

    <!-- 个人信息区 (悬浮) -->
    <div class="absolute -bottom-12 right-10 flex items-end gap-4 px-4">
      <div class="flex flex-col items-end mb-2 px-3 py-2 bg-black/40 backdrop-blur-sm rounded-lg">
        <span class="text-xl font-bold text-white drop-shadow-[0_2px_4px_rgba(0,0,0,0.8)]">{{ user?.username }}</span>
        <span class="text-sm text-white/90 drop-shadow-[0_2px_4px_rgba(0,0,0,0.8)]">{{ user?.realName || '这个人很懒,什么都没写' }}</span>
      </div>
      
      <div class="relative group cursor-pointer">
        <img 
          :src="avatar" 
          class="w-24 h-24 rounded-xl border-4 border-white dark:border-slate-900 shadow-lg object-cover bg-white transition-transform duration-300 group-hover:scale-105"
          alt="avatar"
        />
      </div>
    </div>
  </div>
</template>
