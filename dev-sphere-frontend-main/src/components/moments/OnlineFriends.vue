<script setup lang="ts">
const onlineFriends = [
  { id: 1, name: '张三', avatar: '', online: true },
  { id: 2, name: '李四', avatar: '', online: true },
  { id: 3, name: '王五', avatar: '', online: true },
  { id: 4, name: '赵六', avatar: '', online: true },
]

const getDefaultAvatar = (name: string) => {
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=random&size=64`
}
</script>

<template>
  <div class="online-friends">
    <div class="header">
      <h3 class="title">在线好友</h3>
      <span class="count">{{ onlineFriends.length }}</span>
    </div>
    
    <div class="friends-grid">
      <div 
        v-for="friend in onlineFriends" 
        :key="friend.id"
        class="friend-item"
        :title="friend.name"
      >
        <div class="avatar-wrapper">
          <img 
            :src="friend.avatar || getDefaultAvatar(friend.name)" 
            class="avatar"
            :alt="friend.name"
          />
          <div v-if="friend.online" class="online-dot"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.online-friends {
  @apply bg-white dark:bg-slate-900 rounded-2xl p-5 shadow-sm border border-slate-200 dark:border-slate-800;
}

.header {
  @apply flex items-center justify-between mb-3;
}

.title {
  @apply text-base font-semibold text-slate-900 dark:text-white;
}

.count {
  @apply text-xs px-2 py-1 bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 
         rounded-full font-medium;
}

.friends-grid {
  @apply grid grid-cols-4 gap-3;
}

.friend-item {
  @apply cursor-pointer transition-transform hover:scale-110;
}

.avatar-wrapper {
  @apply relative;
}

.avatar {
  @apply w-full aspect-square rounded-full object-cover ring-2 ring-white dark:ring-slate-800;
}

.online-dot {
  @apply absolute bottom-0 right-0 w-3 h-3 bg-green-500 rounded-full 
         border-2 border-white dark:border-slate-900;
}
</style>
