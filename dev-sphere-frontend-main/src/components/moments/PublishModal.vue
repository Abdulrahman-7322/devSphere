<script setup lang="ts">
import { ref } from 'vue'
import { ossService } from '../../services/ossService'
import { useMomentStore } from '../../stores/momentStore'
import { momentService } from '../../services/momentService'
import request from '../../utils/request'

const props = defineProps<{
  isOpen: boolean
}>()

const emit = defineEmits(['close', 'success'])

const momentStore = useMomentStore()
const content = ref('')
const images = ref<{ file: File, url: string, status: 'pending' | 'uploading' | 'done' | 'error' }[]>([])
const isPublishing = ref(false)

const handleFileChange = async (e: Event) => {
  const files = (e.target as HTMLInputElement).files
  if (!files) return

  for (let i = 0; i < files.length; i++) {
    const file = files[i]
    // 预览
    const url = URL.createObjectURL(file)
    images.value.push({ file, url, status: 'pending' })
  }
  
  // 清空 input
  (e.target as HTMLInputElement).value = ''
}

const removeImage = (index: number) => {
  images.value.splice(index, 1)
}

const handlePublish = async () => {
  if (!content.value.trim() && images.value.length === 0) return
  
  isPublishing.value = true
  try {
    // 1. 上传图片
    const uploadedUrls: string[] = []
    for (const img of images.value) {
      if (img.status !== 'done') {
        img.status = 'uploading'
        try {
          const res = await ossService.upload(img.file)
          // 假设 res 是 UploadDTO { url: string }，或者 res.data 是
          // 根据 ossService.ts，它是直接返回 UploadDTO (如果是拦截器处理了 data)
          // 但通常 request.post 返回的是 AxiosResponse
          // ossService.ts: return request.post(...)
          // 让我们假设 request 拦截器已经解包了 data，或者我们需要检查 request 定义
          // 暂时假设 res.data.url 或 res.url
          const url = (res as any).data?.url || (res as any).url
          if (url) {
            uploadedUrls.push(url)
            img.status = 'done'
          } else {
            throw new Error('Upload failed')
          }
        } catch (e) {
          img.status = 'error'
          throw e
        }
      } else {
        uploadedUrls.push(img.url) // 已经是远程 URL (如果有)
      }
    }

    // 2. 发布动态
    // 2. 发布动态
    await momentService.publishMoment({
      content: content.value,
      imageUrls: uploadedUrls,
      visibility: 1 // 公开
    })

    // 3. 成功
    emit('success')
    emit('close')
    
    // 重置
    content.value = ''
    images.value = []
    
    // 刷新列表
    momentStore.fetchMoments(true)
    
  } catch (error) {
    console.error('Publish failed:', error)
    alert('发布失败，请重试')
  } finally {
    isPublishing.value = false
  }
}
</script>

<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
    <!-- 遮罩 -->
    <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="emit('close')"></div>
    
    <!-- 模态框 -->
    <div class="relative w-full max-w-lg bg-white dark:bg-slate-900 rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
      <!-- 标题 -->
      <div class="px-6 py-4 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center">
        <h3 class="font-bold text-lg text-slate-900 dark:text-white">发布动态</h3>
        <button @click="emit('close')" class="text-slate-400 hover:text-slate-600 dark:hover:text-slate-200">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
        </button>
      </div>
      
      <!-- 内容区 -->
      <div class="p-6 overflow-y-auto custom-scrollbar">
        <textarea 
          v-model="content"
          placeholder="分享你的新鲜事..." 
          class="w-full h-32 bg-transparent resize-none focus:outline-none text-slate-800 dark:text-slate-200 placeholder-slate-400 text-lg"
        ></textarea>
        
        <!-- 图片预览 -->
        <div class="grid grid-cols-3 gap-3 mt-4">
          <div v-for="(img, idx) in images" :key="idx" class="relative aspect-square group">
            <img :src="img.url" class="w-full h-full object-cover rounded-lg border border-slate-200 dark:border-slate-700" />
            <!-- 删除按钮 -->
            <button 
              @click="removeImage(idx)"
              class="absolute top-1 right-1 p-1 bg-black/50 rounded-full text-white opacity-0 group-hover:opacity-100 transition-opacity"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
            </button>
            <!-- 状态 -->
            <div v-if="img.status === 'uploading'" class="absolute inset-0 bg-black/30 flex items-center justify-center rounded-lg">
              <div class="w-6 h-6 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
            </div>
          </div>
          
          <!-- 添加按钮 -->
          <label class="aspect-square flex flex-col items-center justify-center border-2 border-dashed border-slate-200 dark:border-slate-700 rounded-lg cursor-pointer hover:border-blue-500 hover:bg-blue-50 dark:hover:bg-slate-800/50 transition-colors text-slate-400 hover:text-blue-500">
            <input type="file" multiple accept="image/*" class="hidden" @change="handleFileChange" />
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
          </label>
        </div>
      </div>
      
      <!-- 底部 -->
      <div class="px-6 py-4 border-t border-slate-100 dark:border-slate-800 flex justify-end gap-3 bg-slate-50 dark:bg-slate-900/50">
        <button 
          @click="emit('close')"
          class="px-4 py-2 text-slate-600 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-800 rounded-lg transition-colors"
        >
          取消
        </button>
        <button 
          @click="handlePublish"
          :disabled="isPublishing || (!content && !images.length)"
          class="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
        >
          <div v-if="isPublishing" class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
          发布
        </button>
      </div>
    </div>
  </div>
</template>
