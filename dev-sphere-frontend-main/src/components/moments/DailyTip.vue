<script setup lang="ts">
import { ref, onMounted } from 'vue'

const tips = [
  "保持代码简洁，可读性比聪明更重要。",
  "定期重构代码，不要等到技术债务堆积如山。",
  "编写单元测试是投资未来，而不是浪费时间。",
  "遇到 Bug 时，先休息一下，换个思路也许就解决了。",
  "学习新技术很好，但掌握基础更关键。",
  "多阅读优秀的开源项目源码，是提升能力的捷径。",
  "不要过早优化，先让它跑起来，再让它跑得快。",
  "好的命名是代码自解释的第一步。"
]

const currentTip = ref('')

onMounted(() => {
  currentTip.value = tips[Math.floor(Math.random() * tips.length)]
})

const refreshTip = () => {
  let newTip = currentTip.value
  while (newTip === currentTip.value) {
    newTip = tips[Math.floor(Math.random() * tips.length)]
  }
  currentTip.value = newTip
}
</script>

<template>
  <div class="bg-gradient-to-br from-blue-500 to-indigo-600 rounded-2xl p-5 text-white shadow-lg relative overflow-hidden group">
    <!-- 背景装饰 -->
    <div class="absolute top-0 right-0 -mt-4 -mr-4 w-24 h-24 bg-white/10 rounded-full blur-2xl"></div>
    <div class="absolute bottom-0 left-0 -mb-4 -ml-4 w-20 h-20 bg-black/10 rounded-full blur-xl"></div>

    <div class="relative z-10">
      <div class="flex items-center justify-between mb-3">
        <h3 class="font-semibold text-lg flex items-center gap-2">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-2.072-4-3-6-.523-.5-1.5-.5-2 0-1.072 2.143-2.072 4-3 6a2.5 2.5 0 0 0 2.5 2.5z"/><path d="M11.5 14.5A2.5 2.5 0 0 0 14 12c0-1.38-.5-2-1-3-1.072-2.143-2.072-4-3-6-.523-.5-1.5-.5-2 0-1.072 2.143-2.072 4-3 6a2.5 2.5 0 0 0 2.5 2.5z"/><path d="M15.5 19.5a2.5 2.5 0 0 0 2.5-2.5c0-1.38-.5-2-1-3-1.072-2.143-2.072-4-3-6-.523-.5-1.5-.5-2 0-1.072 2.143-2.072 4-3 6a2.5 2.5 0 0 0 2.5 2.5z"/></svg>
          每日小贴士
        </h3>
        <button 
          @click="refreshTip" 
          class="p-1 hover:bg-white/20 rounded-full transition-colors"
          title="换一条"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.3"/></svg>
        </button>
      </div>
      
      <p class="text-white/90 leading-relaxed text-sm font-medium">
        "{{ currentTip }}"
      </p>
    </div>
  </div>
</template>
