<template>
  <div class="flex gap-1 items-end h-12 p-2 bg-black/20 rounded-lg backdrop-blur-sm">
    <div v-for="i in 20" :key="i" 
         class="w-1.5 bg-green-400 rounded-t transition-all duration-100 ease-in-out"
         :style="{ height: active ? (Math.random() * 100 + '%') : '5%' }">
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps } from 'vue';

defineProps<{ active: boolean }>();
</script>
