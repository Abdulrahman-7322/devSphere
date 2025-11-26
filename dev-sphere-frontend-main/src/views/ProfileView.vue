<template>
  <div class="min-h-screen bg-slate-50 dark:bg-slate-900 text-slate-800 dark:text-slate-200 font-sans transition-colors duration-300">
    <!-- Hero Section -->
    <div class="relative h-64 md:h-80 bg-gradient-to-r from-violet-600 to-fuchsia-600 overflow-hidden">
      <div class="absolute inset-0 opacity-20 bg-[url('https://www.transparenttextures.com/patterns/cubes.png')]"></div>
      <div class="absolute inset-0 bg-gradient-to-b from-transparent to-slate-900/90"></div>
    </div>

    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 -mt-32 relative z-10 pb-12">
      <!-- Profile Header Card -->
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-xl border border-slate-200 dark:border-slate-700 backdrop-blur-xl bg-opacity-80 dark:bg-opacity-80 p-6 md:p-8 flex flex-col md:flex-row items-center md:items-end gap-6">
        <!-- Avatar -->
        <div class="relative group cursor-pointer" @click="triggerFileUpload">
          <div class="w-32 h-32 md:w-40 md:h-40 rounded-full p-1 bg-gradient-to-tr from-violet-500 to-fuchsia-500 shadow-lg relative overflow-hidden">
            <img 
              :src="userStore.userInfo?.headUrl || 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + (userStore.userInfo?.username || 'User')" 
              alt="Avatar" 
              class="w-full h-full rounded-full object-cover border-4 border-white dark:border-slate-800 bg-white dark:bg-slate-800 transition-opacity group-hover:opacity-75"
            />
            <!-- Upload Overlay -->
            <div class="absolute inset-0 flex items-center justify-center bg-black/30 opacity-0 group-hover:opacity-100 transition-opacity rounded-full">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </div>
          </div>
          <div class="absolute bottom-2 right-2 w-6 h-6 bg-emerald-500 border-4 border-white dark:border-slate-800 rounded-full animate-pulse" title="在线"></div>
          
          <!-- Hidden File Input -->
          <input 
            type="file" 
            ref="fileInput" 
            class="hidden" 
            accept="image/*"
            @change="handleFileChange"
          />
        </div>

        <!-- Info -->
        <div class="flex-1 text-center md:text-left">
          <h1 class="text-3xl md:text-4xl font-bold text-slate-900 dark:text-white mb-2">
            {{ userStore.userInfo?.realName || userStore.userInfo?.username || '开发者' }}
          </h1>
          <p class="text-violet-600 dark:text-violet-400 font-mono text-sm md:text-base mb-4">
            @{{ userStore.userInfo?.username }} • 全栈开发者 • 📍 中国上海
          </p>
          
          <!-- Bio -->
          <p class="text-slate-600 dark:text-slate-400 max-w-2xl mb-6 leading-relaxed">
            用 <span class="text-sky-500 font-mono">Java</span>, <span class="text-emerald-500 font-mono">Vue.js</span> 和 ☕ 构建未来。
            热衷于整洁代码和分布式系统。
          </p>

          <!-- Socials & Actions -->
          <div class="flex flex-wrap justify-center md:justify-start gap-3">
            <button class="btn-icon group" title="GitHub">
              <svg class="w-5 h-5" viewBox="0 0 24 24" fill="currentColor"><path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/></svg>
            </button>
            <button class="btn-icon group" title="Twitter">
              <svg class="w-5 h-5" viewBox="0 0 24 24" fill="currentColor"><path d="M24 4.557c-.883.392-1.832.656-2.828.775 1.017-.609 1.798-1.574 2.165-2.724-.951.564-2.005.974-3.127 1.195-.897-.957-2.178-1.555-3.594-1.555-3.179 0-5.515 2.966-4.797 6.045-4.091-.205-7.719-2.165-10.148-5.144-1.29 2.213-.669 5.108 1.523 6.574-.806-.026-1.566-.247-2.229-.616-.054 2.281 1.581 4.415 3.949 4.89-.693.188-1.452.232-2.224.084.626 1.956 2.444 3.379 4.6 3.419-2.07 1.623-4.678 2.348-7.29 2.04 2.179 1.397 4.768 2.212 7.548 2.212 9.142 0 14.307-7.721 13.995-14.646.962-.695 1.797-1.562 2.457-2.549z"/></svg>
            </button>
            <div class="h-8 w-px bg-slate-200 dark:bg-slate-700 mx-2"></div>
            <button class="btn-primary" @click="isEditProfileOpen = true">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" /></svg>
              编辑资料
            </button>
          </div>
        </div>
      </div>

      <!-- Stats Grid -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mt-6">
        <div v-for="(stat, index) in stats" :key="index" class="bg-white dark:bg-slate-800 p-5 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm hover:shadow-md transition-shadow relative overflow-hidden group">
          <div class="relative z-10">
            <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">{{ stat.label }}</p>
            <p class="text-2xl font-bold text-slate-900 dark:text-white font-mono">{{ stat.value }}</p>
          </div>
          <div class="absolute right-0 bottom-0 opacity-10 group-hover:opacity-20 transition-opacity transform translate-y-2 translate-x-2">
            <component :is="stat.icon" class="w-16 h-16 text-current" :class="stat.color" />
          </div>
        </div>
      </div>

      <!-- Main Content Tabs -->
      <div class="mt-8 grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Left Column: Tabs & Content -->
        <div class="lg:col-span-2 space-y-6">
          <!-- Tabs Navigation -->
          <div class="flex space-x-1 bg-slate-200 dark:bg-slate-800 p-1 rounded-lg">
            <button 
              v-for="tab in tabs" 
              :key="tab.id"
              @click="activeTab = tab.id"
              :class="[
                'flex-1 py-2.5 text-sm font-medium rounded-md transition-all duration-200',
                activeTab === tab.id 
                  ? 'bg-white dark:bg-slate-700 text-violet-600 dark:text-violet-400 shadow-sm' 
                  : 'text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-200'
              ]"
            >
              {{ tab.name }}
            </button>
          </div>

          <!-- Overview Tab -->
          <div v-if="activeTab === 'Overview'" class="space-y-6">
            <!-- Pinned Projects -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div v-for="project in projects" :key="project.name" class="bg-white dark:bg-slate-800 p-5 rounded-xl border border-slate-200 dark:border-slate-700 hover:border-violet-500 dark:hover:border-violet-500 transition-colors group cursor-pointer">
                <div class="flex justify-between items-start mb-3">
                  <div class="flex items-center gap-2">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-slate-400 group-hover:text-violet-500 transition-colors" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" /></svg>
                    <h3 class="font-semibold text-slate-900 dark:text-white group-hover:text-violet-500 transition-colors">{{ project.name }}</h3>
                  </div>
                  <span class="text-xs font-mono text-slate-400 border border-slate-200 dark:border-slate-700 px-2 py-0.5 rounded-full">{{ project.visibility }}</span>
                </div>
                <p class="text-sm text-slate-500 dark:text-slate-400 mb-4 line-clamp-2">{{ project.desc }}</p>
                <div class="flex items-center gap-3">
                  <span class="flex items-center gap-1 text-xs text-slate-500">
                    <span class="w-2 h-2 rounded-full" :style="{ backgroundColor: project.langColor }"></span>
                    {{ project.lang }}
                  </span>
                  <span class="flex items-center gap-1 text-xs text-slate-500 hover:text-violet-500">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5" viewBox="0 0 20 20" fill="currentColor"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" /></svg>
                    {{ project.stars }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Contribution Graph (Mock) -->
            <div class="bg-white dark:bg-slate-800 p-6 rounded-xl border border-slate-200 dark:border-slate-700">
              <h3 class="text-sm font-semibold text-slate-900 dark:text-white mb-4">过去一年贡献 1,284 次</h3>
              <div class="flex gap-1 overflow-hidden h-32 items-end opacity-80">
                 <!-- Generating random bars for visual effect -->
                 <div v-for="i in 52" :key="i" 
                      class="flex-1 bg-emerald-500 rounded-sm hover:bg-emerald-400 transition-colors"
                      :style="{ height: Math.max(10, Math.random() * 100) + '%', opacity: Math.random() * 0.8 + 0.2 }"
                      :title="'第 ' + i + ' 周'"
                 ></div>
              </div>
            </div>
          </div>

          <!-- Other Tabs (Placeholder) -->
          <div v-else class="bg-white dark:bg-slate-800 p-12 rounded-xl border border-slate-200 dark:border-slate-700 text-center">
            <div class="inline-block p-4 rounded-full bg-slate-100 dark:bg-slate-700 mb-4">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" /></svg>
            </div>
            <h3 class="text-lg font-medium text-slate-900 dark:text-white">建设中</h3>
            <p class="text-slate-500 mt-2">{{ activeTab }} 模块即将上线。</p>
          </div>
        </div>

        <!-- Right Column: Recent Activity -->
        <div class="space-y-6">
          <div class="bg-white dark:bg-slate-800 p-6 rounded-xl border border-slate-200 dark:border-slate-700">
            <h3 class="text-lg font-bold text-slate-900 dark:text-white mb-4 flex items-center gap-2">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-violet-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
              最近活动
            </h3>
            <div class="space-y-6 relative before:absolute before:inset-y-0 before:left-2.5 before:w-0.5 before:bg-slate-200 dark:before:bg-slate-700">
              <div v-for="(activity, index) in activities" :key="index" class="relative pl-8">
                <div class="absolute left-0 top-1.5 w-5 h-5 rounded-full border-4 border-white dark:border-slate-800 bg-violet-500"></div>
                <p class="text-sm text-slate-800 dark:text-slate-200">
                  <span class="font-medium">{{ activity.action }}</span> 
                  <span class="text-slate-500 dark:text-slate-400"> {{ activity.target }}</span>
                </p>
                <p class="text-xs text-slate-400 mt-0.5 font-mono">{{ activity.time }}</p>
              </div>
            </div>
          </div>

          <!-- Skills / Tags -->
          <div class="bg-white dark:bg-slate-800 p-6 rounded-xl border border-slate-200 dark:border-slate-700">
            <h3 class="text-lg font-bold text-slate-900 dark:text-white mb-4">技能栈</h3>
            <div class="flex flex-wrap gap-2">
              <span v-for="skill in skills" :key="skill" class="px-3 py-1 rounded-full text-xs font-medium bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-300 border border-slate-200 dark:border-slate-600">
                {{ skill }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Edit Profile Modal -->
    <EditProfileModal :show="isEditProfileOpen" @close="isEditProfileOpen = false" @success="handleEditSuccess" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useUserStore } from '@/stores/userStore';
import { ossService } from '@/services/ossService';
import { userService } from '@/services/userService';
import EditProfileModal from '@/components/profile/EditProfileModal.vue';

const userStore = useUserStore();
const activeTab = ref('Overview');
const fileInput = ref<HTMLInputElement | null>(null);
const isEditProfileOpen = ref(false);

const tabs = [
  { id: 'Overview', name: '概览' },
  { id: 'Repositories', name: '仓库' },
  { id: 'Snippets', name: '代码片段' },
  { id: 'Settings', name: '设置' }
];

// Trigger file input click
const triggerFileUpload = () => {
  fileInput.value?.click();
};

const handleEditSuccess = () => {
  // Optional: Show toast or refresh data if needed
  // userStore is already updated in the modal
};

// Handle file selection and upload
const handleFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    const file = target.files[0];
    
    // Basic validation
    if (!file.type.startsWith('image/')) {
      alert('请上传图片文件');
      return;
    }
    if (file.size > 5 * 1024 * 1024) { // 5MB
      alert('图片大小不能超过 5MB');
      return;
    }

    try {
      // 1. Upload to OSS
      const uploadRes = await ossService.upload(file);
      const newAvatarUrl = uploadRes.url;

      // 2. Update User Profile
      // 必须传递所有必填字段 (username, realName, email, mobile, deptId, status)
      // 因为后端使用了 DefaultGroup 校验
      if (userStore.userInfo) {
        await userService.updateUserInfo({
          ...userStore.userInfo,
          headUrl: newAvatarUrl
        });
      }

      // 3. Update Local Store
      if (userStore.userInfo) {
        userStore.login(userStore.token || '', {
          ...userStore.userInfo,
          headUrl: newAvatarUrl
        });
      }


    } catch (error) {
      console.error('上传失败:', error);
      alert('头像上传失败，请重试');
    } finally {
      // Clear input
      if (fileInput.value) {
        fileInput.value.value = '';
      }
    }
  }
};

// Mock Data
const stats = [
  { label: '声望值', value: '2,458', icon: 'svg', color: 'text-amber-500' },
  { label: '提交数', value: '8,942', icon: 'svg', color: 'text-emerald-500' },
  { label: '连胜天数', value: '14 天', icon: 'svg', color: 'text-rose-500' },
  { label: '排名', value: '#42', icon: 'svg', color: 'text-sky-500' },
];

const projects = [
  { name: 'dev-sphere-chat', desc: '基于 Netty 和 Vue 3 构建的高性能即时通讯系统。', lang: 'Java', langColor: '#b07219', stars: 128, visibility: 'Public' },
  { name: 'code-arena-web', desc: '在线代码对战平台的前端项目。', lang: 'Vue', langColor: '#41b883', stars: 84, visibility: 'Public' },
  { name: 'algo-visualizer', desc: '实时可视化排序算法。', lang: 'TypeScript', langColor: '#3178c6', stars: 256, visibility: 'Public' },
];

const activities = [
  { action: '推送到', target: 'dev-sphere-chat/main', time: '2 小时前' },
  { action: '发起 PR', target: '#42 in code-arena', time: '5 小时前' },
  { action: '标星', target: 'vuejs/core', time: '1 天前' },
  { action: '加入', target: 'Java 爱好者小组', time: '2 天前' },
];

const skills = ['Java', 'Spring Boot', 'Vue.js', 'TypeScript', 'MySQL', 'Redis', 'Docker', 'Kubernetes'];
</script>

<style scoped>
.btn-icon {
  @apply p-2 rounded-lg bg-slate-100 dark:bg-slate-700 text-slate-500 dark:text-slate-400 hover:bg-violet-100 dark:hover:bg-violet-900 hover:text-violet-600 dark:hover:text-violet-300 transition-all;
}
.btn-primary {
  @apply px-4 py-2 rounded-lg bg-violet-600 hover:bg-violet-700 text-white font-medium shadow-lg shadow-violet-500/30 transition-all flex items-center;
}
</style>