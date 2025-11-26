<script setup lang="ts">
import { useRoute } from 'vue-router'
const route = useRoute()
</script>
<template>
  <main class="w-full max-w-[1400px] mx-auto px-4 lg:px-8 py-8">
    <div class="v-card p-12 text-center max-w-lg mx-auto border-functional-red/50">
      <h1 class="text-3xl font-extrabold text-functional-red mb-4">
        🚨 作战室
      </h1>
      <p class="text-vibrant-muted">
        你已进入 ID 为 <b class="text-vibrant-main font-mono">{{ route.params.id }}</b> 的作战室。
      </p>
    </div>
  </main>
</template>