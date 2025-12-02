<template>
  <div class="avatar-container relative w-96 h-96 flex justify-center items-center">
    <!-- Placeholder Avatar -->
    <div :class="['w-64 h-64 bg-gradient-to-br from-blue-400 to-indigo-600 rounded-full flex items-center justify-center shadow-2xl transition-transform duration-100', isSpeaking ? 'scale-110' : 'scale-100']">
      <div class="text-8xl filter drop-shadow-lg">👩‍💼</div>
    </div>
    
    <!-- Speaking Indicator -->
    <div v-if="isSpeaking" class="absolute -bottom-4 left-1/2 transform -translate-x-1/2">
      <span class="text-blue-300 text-sm animate-pulse">Speaking...</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps, watch, ref, defineEmits } from 'vue';

const props = defineProps<{
  viseme?: any,
  audioUrl?: string
}>();

const emit = defineEmits(['speak-end']);
const isSpeaking = ref(false);

watch(() => props.audioUrl, (newVal) => {
  if (newVal) {
    isSpeaking.value = true;
    // Mock audio playback for demo if no real URL
    // In real app: const audio = new Audio(newVal);
    
    // Simulate speaking duration
    setTimeout(() => {
      isSpeaking.value = false;
      emit('speak-end');
    }, 3000);
  }
});
</script>
