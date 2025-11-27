<script setup lang="ts">
import { ref, computed } from 'vue'
import { useMomentStore, type Moment } from '../../stores/momentStore'
import { useUserStore } from '../../stores/userStore'

const props = defineProps<{
  moment: Moment
}>()

const momentStore = useMomentStore()
const userStore = useUserStore()

const isSelf = computed(() => String(props.moment.userId) === String(userStore.userInfo?.id))
const showCommentInput = ref(false)
const commentContent = ref('')
const isSubmitting = ref(false)

// 智能图片布局类
const imageGridClass = computed(() => {
  const count = props.moment.imageUrls?.length || 0
  if (count === 0) return ''
  if (count === 1) return 'single-image'
  if (count === 2) return 'grid grid-cols-2 gap-2'
  if (count === 3) return 'grid-3-layout'
  if (count === 4) return 'grid grid-cols-2 gap-2'
  return 'grid grid-cols-3 gap-2'
})

// 格式化时间
const formatTime = (dateStr: string) => {
  try {
    const date = new Date(dateStr)
    const now = new Date()
    const diff = now.getTime() - date.getTime()
    const minutes = Math.floor(diff / 60000)
    const hours = Math.floor(diff / 3600000)
    const days = Math.floor(diff / 86400000)
    
    if (minutes < 1) return '刚刚'
    if (minutes < 60) return `${minutes}分钟前`
    if (hours < 24) return `${hours}小时前`
    if (days === 1) return '昨天'
    if (days < 7) return `${days}天前`
    
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    const hour = date.getHours().toString().padStart(2, '0')
    const minute = date.getMinutes().toString().padStart(2, '0')
    return `${month}-${day} ${hour}:${minute}`
  } catch {
    return dateStr
  }
}

const handleLike = () => {
  if (props.moment.isLiked) {
    momentStore.unlikeMoment(props.moment.id)
  } else {
    momentStore.likeMoment(props.moment.id)
  }
}

const handleDelete = () => {
  if (confirm('确定要删除这条动态吗?')) {
    momentStore.deleteMoment(props.moment.id)
  }
}

const handleSubmitComment = async () => {
  if (!commentContent.value.trim()) return
  isSubmitting.value = true
  await momentStore.addComment(props.moment.id, commentContent.value)
  commentContent.value = ''
  showCommentInput.value = false
  isSubmitting.value = false
}

const previewImage = (url: string) => {
  window.open(url, '_blank')
}

// ImageViewer State
import ImageViewer from '../ImageViewer.vue'
const showImageViewer = ref(false)
const currentImageIndex = ref(0)

const openImageViewer = (index: number) => {
  currentImageIndex.value = index
  showImageViewer.value = true
}

// 用户显示名称优先级: username > "用户"+ID后4位
const displayName = computed(() => {
  if (props.moment.user.username) return props.moment.user.username
  if (props.moment.userId) return `用户${String(props.moment.userId).slice(-4)}`
  return '匿名用户'
})

// 渐变色默认头像
const defaultAvatar = computed(() => {
  const colors = ['from-purple-400 to-pink-500', 'from-blue-400 to-cyan-500', 'from-green-400 to-emerald-500', 'from-orange-400 to-red-500']
  const randomGradient = colors[Math.floor(Math.random() * colors.length)]
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(displayName.value)}&background=random&color=fff&size=128`
})
</script>

<template>
  <div class="bg-white dark:bg-slate-900 rounded-xl shadow-sm overflow-hidden mb-6 transition-all duration-300 hover:-translate-y-1 hover:shadow-md border border-slate-100 dark:border-slate-800">
    <div class="p-6">
      <!-- 头部信息 -->
      <div class="flex items-center mb-4">
        <div class="relative group cursor-pointer">
          <img 
            :src="moment.user.avatar || defaultAvatar" 
            class="w-10 h-10 rounded-full object-cover border border-slate-100 dark:border-slate-800"
            alt="avatar"
          />
          <!-- 在线状态指示器 -->
          <div class="absolute bottom-0 right-0 w-2.5 h-2.5 bg-green-500 border-2 border-white dark:border-slate-900 rounded-full"></div>
        </div>
        
        <div class="ml-3 flex-1">
          <div class="flex items-center gap-2">
            <span class="font-bold text-slate-900 dark:text-white text-sm">{{ displayName }}</span>
            <!-- 认证标识示例 -->
            <svg v-if="true" xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="currentColor" class="text-blue-500"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg>
          </div>
          <span class="text-xs text-slate-500 dark:text-slate-400 mt-0.5 block">{{ formatTime(moment.createdAt) }}</span>
        </div>

        <button class="text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition-colors p-1 rounded-full hover:bg-slate-100 dark:hover:bg-slate-800">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="1"></circle><circle cx="19" cy="12" r="1"></circle><circle cx="5" cy="12" r="1"></circle></svg>
        </button>
      </div>

      <!-- 内容文本 -->
      <p class="text-slate-700 dark:text-slate-300 text-base leading-relaxed mb-4 whitespace-pre-wrap">{{ moment.content }}</p>

      <!-- 图片网格 -->
      <div v-if="moment.imageUrls && moment.imageUrls.length > 0" class="mb-4">
        <!-- 单图模式 -->
        <div v-if="moment.imageUrls.length === 1" class="rounded-lg overflow-hidden max-h-[400px]">
          <img 
            :src="moment.imageUrls[0]" 
            class="w-full h-full object-cover hover:scale-105 transition-transform duration-500 cursor-zoom-in"
            @click="openImageViewer(0)"
          />
        </div>
        <!-- 多图模式 -->
        <div v-else class="grid gap-1" :class="imageGridClass">
          <div 
            v-for="(url, index) in moment.imageUrls" 
            :key="index"
            class="aspect-square rounded-lg overflow-hidden relative group"
          >
            <img 
              :src="url" 
              class="w-full h-full object-cover hover:scale-110 transition-transform duration-500 cursor-zoom-in"
              @click="openImageViewer(index)"
            />
          </div>
        </div>
      </div>

      <ImageViewer 
        v-model="showImageViewer"
        :images="moment.imageUrls || []"
        :initial-index="currentImageIndex"
      />

      <!-- 底部操作栏 -->
      <div class="flex items-center justify-between pt-2">
        <button 
          @click="handleLike"
          class="flex items-center gap-1.5 text-slate-500 hover:text-red-500 transition-colors group"
          :class="{ 'text-red-500': moment.isLiked }"
        >
          <div class="p-2 rounded-full group-hover:bg-red-50 dark:group-hover:bg-red-900/20 transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" :fill="moment.isLiked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="transition-transform group-active:scale-125"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>
          </div>
          <span class="text-sm font-medium">{{ moment.likeCount || 0 }}</span>
        </button>

        <button 
          @click="showCommentInput = !showCommentInput"
          class="flex items-center gap-1.5 text-slate-500 hover:text-blue-500 transition-colors group"
        >
          <div class="p-2 rounded-full group-hover:bg-blue-50 dark:group-hover:bg-blue-900/20 transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"></path></svg>
          </div>
          <span class="text-sm font-medium">{{ moment.commentCount || 0 }}</span>
        </button>

        <button class="flex items-center gap-1.5 text-slate-500 hover:text-blue-500 transition-colors group">
          <div class="p-2 rounded-full group-hover:bg-blue-50 dark:group-hover:bg-blue-900/20 transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="18" cy="5" r="3"></circle><circle cx="6" cy="12" r="3"></circle><circle cx="18" cy="19" r="3"></circle><line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line></svg>
          </div>
          <span class="text-sm font-medium">分享</span>
        </button>

        <button class="flex items-center gap-1.5 text-slate-500 hover:text-yellow-500 transition-colors group">
          <div class="p-2 rounded-full group-hover:bg-yellow-50 dark:group-hover:bg-yellow-900/20 transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"></path></svg>
          </div>
          <span class="text-sm font-medium">收藏</span>
        </button>
      </div>
    </div>

    <!-- 评论区 -->
    <div v-if="showCommentInput || (moment.comments && moment.comments.length > 0)" class="bg-slate-50 dark:bg-slate-800/50 border-t border-slate-100 dark:border-slate-800 px-6 py-4">
      <!-- 评论输入框 -->
      <div v-if="showCommentInput" class="flex items-center gap-3 mb-4">
        <img :src="userStore.userAvatar" class="w-8 h-8 rounded-full object-cover" />
        <div class="flex-1 relative">
          <input 
            v-model="commentContent"
            type="text" 
            placeholder="写下你的评论..."
            class="w-full bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-full py-2 px-4 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
            @keyup.enter="handleSubmitComment"
          >
          <button 
            @click="handleSubmitComment"
            class="absolute right-2 top-1/2 -translate-y-1/2 text-blue-500 hover:text-blue-600 p-1"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"></line><polygon points="22 2 15 22 11 13 2 9 22 2"></polygon></svg>
          </button>
        </div>
      </div>

      <!-- 评论列表 -->
      <div class="space-y-4">
        <div v-for="comment in moment.comments" :key="comment.id" class="flex gap-3 group">
          <img 
            :src="comment.avatar || `https://ui-avatars.com/api/?name=${comment.username}&background=random`" 
            class="w-8 h-8 rounded-full object-cover mt-1"
          />
          <div class="flex-1">
            <div class="bg-white dark:bg-slate-900 rounded-2xl rounded-tl-none p-3 shadow-sm inline-block min-w-[200px]">
              <div class="flex items-center justify-between mb-1">
                <span class="font-bold text-sm text-slate-900 dark:text-white">{{ comment.username }}</span>
                <span class="text-xs text-slate-400">{{ formatTime(comment.createTime) }}</span>
              </div>
              <p class="text-sm text-slate-700 dark:text-slate-300">
                <span v-if="comment.replyToUserId" class="text-blue-500 mr-1">@{{ comment.replyToUsername }}</span>
                {{ comment.content }}
              </p>
            </div>
            <!-- 评论操作按钮可以按需添加 -->
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.moment-card {
  @apply bg-white dark:bg-slate-900 rounded-[20px] p-6 mb-6
         shadow-[0_4px_20px_rgba(0,0,0,0.05)] 
         hover:shadow-[0_8px_30px_rgba(0,0,0,0.08)]
         transition-all duration-300
         border border-[#E5E7EB] dark:border-slate-800;
}

.moment-card:hover {
  transform: translateY(-2px);
}

.grid-3-layout {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.5rem;
}

.grid-3-layout > :first-child {
  grid-row: span 2;
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes like-bounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

.animate-fade-in-up {
  animation: fade-in-up 0.5s ease-out;
}

.animate-fade-in {
  animation: fade-in 0.3s ease-out;
}

.animate-like-bounce {
  animation: like-bounce 0.4s ease-out;
}
</style>
