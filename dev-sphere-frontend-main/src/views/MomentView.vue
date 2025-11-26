<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Sidebar from '../components/chat/Sidebar.vue'
import MomentItem from '../components/moments/MomentItem.vue'
import MomentHeader from '../components/moments/MomentHeader.vue'
import PublishModal from '../components/moments/PublishModal.vue'
import TrendingTopics from '../components/moments/TrendingTopics.vue'
import DailyTip from '../components/moments/DailyTip.vue'
import Announcements from '../components/moments/Announcements.vue'
import ProfileCard from '../components/moments/ProfileCard.vue'
import ShortcutsCard from '../components/moments/ShortcutsCard.vue'
import PublishBox from '../components/moments/PublishBox.vue'
import FilterBar from '../components/moments/FilterBar.vue'
import { useMomentStore } from '../stores/momentStore'

const momentStore = useMomentStore()
const isPublishModalOpen = ref(false)

onMounted(() => {
  console.log('MomentView mounted')
  momentStore.fetchMoments(true)
})

const handlePublish = () => {
  isPublishModalOpen.value = true
}

const handlePublishSuccess = () => {
  momentStore.fetchMoments(true)
}
</script>

<template>
  <div class="flex h-full w-full overflow-hidden bg-[#F9FAFB] dark:bg-slate-900 text-slate-900 dark:text-white transition-colors duration-300 font-sans">
    <!-- 侧边导航栏 -->
    <Sidebar active-tab="moments" class="shrink-0" />

    <!-- 主内容区 -->
    <div class="flex-1 h-full overflow-y-auto custom-scrollbar">
      <!-- 顶部封面 (全宽) -->
      <MomentHeader @publish="handlePublish" />
      
      <!-- 3栏布局容器 - 增加最大宽度以加宽中间内容 -->
      <div class="max-w-[1600px] mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div class="flex flex-col lg:flex-row gap-6">
          
          <!-- 左侧边栏: 个人资料 & 快捷功能 (280px) -->
          <div class="w-full lg:w-[280px] flex-shrink-0 space-y-6">
            <ProfileCard />
            <ShortcutsCard />
          </div>

          <!-- 中间内容区: 发布框 & 筛选 & 动态列表 (Flex-1) -->
          <div class="flex-1 min-w-0">
            <PublishBox @click="handlePublish" />
            <FilterBar />
            
            <!-- 骨架屏加载 -->
            <div v-if="momentStore.isLoading && momentStore.moments.length === 0" class="space-y-4">
              <div v-for="i in 3" :key="i" class="bg-white dark:bg-slate-900 rounded-2xl p-6 animate-pulse">
                <div class="flex items-start gap-4 mb-4">
                  <div class="w-12 h-12 bg-slate-200 dark:bg-slate-700 rounded-full"></div>
                  <div class="flex-1">
                    <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-32 mb-2"></div>
                    <div class="h-3 bg-slate-200 dark:bg-slate-700 rounded w-20"></div>
                  </div>
                </div>
                <div class="space-y-2 mb-4">
                  <div class="h-3 bg-slate-200 dark:bg-slate-700 rounded w-full"></div>
                  <div class="h-3 bg-slate-200 dark:bg-slate-700 rounded w-4/5"></div>
                </div>
                <div class="h-48 bg-slate-200 dark:bg-slate-700 rounded-2xl"></div>
              </div>
            </div>
            
            <!-- 动态列表 -->
            <div v-else class="space-y-6">
              <MomentItem 
                v-for="moment in momentStore.moments" 
                :key="moment.id" 
                :moment="moment" 
              />
            </div>
            
            <!-- 到底提示 -->
            <div v-if="!momentStore.hasMore && momentStore.moments.length > 0" class="py-8 text-center text-[#9CA3AF] text-sm">
              <div class="inline-flex items-center gap-2">
                <div class="w-12 h-px bg-[#E5E7EB]"></div>
                <span>已经到底了</span>
                <div class="w-12 h-px bg-[#E5E7EB]"></div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-if="!momentStore.isLoading && momentStore.moments.length === 0" class="py-20 text-center">
              <div class="inline-flex items-center justify-center w-20 h-20 bg-slate-100 dark:bg-slate-800 rounded-full mb-4">
                <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="text-slate-400"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>
              </div>
              <p class="text-[#6B7280] text-base">还没有动态</p>
              <p class="text-[#9CA3AF] text-sm mt-2">发布第一条动态,分享你的精彩瞬间</p>
            </div>
          </div>

          <!-- 右侧边栏: 推荐 & 话题 & 公告 (320px) -->
          <div class="w-full lg:w-[320px] flex-shrink-0 space-y-6 hidden xl:block">
            <DailyTip />
            <Announcements />
            <TrendingTopics />
          </div>

        </div>
      </div>

      <PublishModal 
        :is-open="isPublishModalOpen" 
        @close="isPublishModalOpen = false"
        @success="handlePublishSuccess"
      />
    </div>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  @apply bg-slate-300 dark:bg-slate-700 rounded-full hover:bg-slate-400 dark:hover:bg-slate-600 transition-colors;
}
</style>
