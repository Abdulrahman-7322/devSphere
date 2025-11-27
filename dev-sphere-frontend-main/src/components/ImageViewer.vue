<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps<{
  modelValue: boolean
  images: string[]
  initialIndex?: number
}>()

const emit = defineEmits(['update:modelValue'])

const currentIndex = ref(props.initialIndex || 0)
const scale = ref(1)
const rotation = ref(0)
const isDragging = ref(false)
const startX = ref(0)
const startY = ref(0)
const translateX = ref(0)
const translateY = ref(0)

// Reset state when opening or changing image
watch(() => props.modelValue, (val) => {
  if (val) {
    currentIndex.value = props.initialIndex || 0
    resetTransform()
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})

watch(currentIndex, () => {
  resetTransform()
})

const resetTransform = () => {
  scale.value = 1
  rotation.value = 0
  translateX.value = 0
  translateY.value = 0
}

const close = () => {
  emit('update:modelValue', false)
}

const prev = (e?: Event) => {
  e?.stopPropagation()
  if (currentIndex.value > 0) {
    currentIndex.value--
  }
}

const next = (e?: Event) => {
  e?.stopPropagation()
  if (currentIndex.value < props.images.length - 1) {
    currentIndex.value++
  }
}

const zoomIn = (e?: Event) => {
  e?.stopPropagation()
  scale.value = Math.min(scale.value + 0.25, 3)
}

const zoomOut = (e?: Event) => {
  e?.stopPropagation()
  scale.value = Math.max(scale.value - 0.25, 0.5)
}

const rotateLeft = (e?: Event) => {
  e?.stopPropagation()
  rotation.value -= 90
}

const rotateRight = (e?: Event) => {
  e?.stopPropagation()
  rotation.value += 90
}

// Mouse Wheel Zoom
const handleWheel = (e: WheelEvent) => {
  e.preventDefault()
  if (e.deltaY < 0) {
    zoomIn()
  } else {
    zoomOut()
  }
}

// Dragging Logic
const handleMouseDown = (e: MouseEvent) => {
  if (scale.value <= 1) return
  isDragging.value = true
  startX.value = e.clientX - translateX.value
  startY.value = e.clientY - translateY.value
}

const handleMouseMove = (e: MouseEvent) => {
  if (!isDragging.value) return
  e.preventDefault()
  translateX.value = e.clientX - startX.value
  translateY.value = e.clientY - startY.value
}

const handleMouseUp = () => {
  isDragging.value = false
}

// Keyboard Navigation
const handleKeydown = (e: KeyboardEvent) => {
  if (!props.modelValue) return
  switch (e.key) {
    case 'Escape':
      close()
      break
    case 'ArrowLeft':
      prev()
      break
    case 'ArrowRight':
      next()
      break
    case 'ArrowUp':
      zoomIn()
      break
    case 'ArrowDown':
      zoomOut()
      break
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
  window.addEventListener('mouseup', handleMouseUp)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  window.removeEventListener('mouseup', handleMouseUp)
})
</script>

<template>
  <Teleport to="body">
    <div 
      v-if="modelValue" 
      class="fixed inset-0 z-[9999] bg-black/90 flex items-center justify-center select-none"
      @wheel="handleWheel"
    >
      <!-- Close Button -->
      <button 
        @click="close" 
        class="absolute top-4 right-4 p-2 text-white/70 hover:text-white hover:bg-white/10 rounded-full transition-colors z-50"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
      </button>

      <!-- Toolbar -->
      <div class="absolute bottom-8 left-1/2 -translate-x-1/2 flex items-center gap-4 bg-black/50 backdrop-blur-md px-6 py-3 rounded-full z-50">
        <button @click="zoomOut" class="p-2 text-white/80 hover:text-white hover:bg-white/10 rounded-full transition-colors" title="Zoom Out">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line><line x1="8" y1="11" x2="14" y2="11"></line></svg>
        </button>
        <span class="text-white/90 font-mono text-sm w-12 text-center">{{ Math.round(scale * 100) }}%</span>
        <button @click="zoomIn" class="p-2 text-white/80 hover:text-white hover:bg-white/10 rounded-full transition-colors" title="Zoom In">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line><line x1="11" y1="8" x2="11" y2="14"></line><line x1="8" y1="11" x2="14" y2="11"></line></svg>
        </button>
        <div class="w-px h-6 bg-white/20 mx-2"></div>
        <button @click="rotateLeft" class="p-2 text-white/80 hover:text-white hover:bg-white/10 rounded-full transition-colors" title="Rotate Left">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"></path><path d="M3 3v5h5"></path></svg>
        </button>
        <button @click="rotateRight" class="p-2 text-white/80 hover:text-white hover:bg-white/10 rounded-full transition-colors" title="Rotate Right">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12a9 9 0 1 1-9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"></path><path d="M21 3v5h-5"></path></svg>
        </button>
      </div>

      <!-- Navigation -->
      <button 
        v-if="images.length > 1" 
        @click="prev" 
        class="absolute left-4 top-1/2 -translate-y-1/2 p-3 text-white/70 hover:text-white hover:bg-white/10 rounded-full transition-colors z-50 disabled:opacity-30 disabled:cursor-not-allowed"
        :disabled="currentIndex === 0"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg>
      </button>

      <button 
        v-if="images.length > 1" 
        @click="next" 
        class="absolute right-4 top-1/2 -translate-y-1/2 p-3 text-white/70 hover:text-white hover:bg-white/10 rounded-full transition-colors z-50 disabled:opacity-30 disabled:cursor-not-allowed"
        :disabled="currentIndex === images.length - 1"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"></polyline></svg>
      </button>

      <!-- Main Image Container -->
      <div 
        class="w-full h-full flex items-center justify-center overflow-hidden cursor-grab active:cursor-grabbing"
        @mousedown="handleMouseDown"
        @mousemove="handleMouseMove"
      >
        <img 
          :src="images[currentIndex]" 
          class="max-w-full max-h-full transition-transform duration-200 ease-out object-contain"
          :style="{
            transform: `translate(${translateX}px, ${translateY}px) scale(${scale}) rotate(${rotation}deg)`
          }"
          draggable="false"
          @click.stop
        />
      </div>

      <!-- Counter -->
      <div v-if="images.length > 1" class="absolute top-4 left-4 px-3 py-1 bg-black/50 text-white/80 rounded-full text-sm font-medium backdrop-blur-sm">
        {{ currentIndex + 1 }} / {{ images.length }}
      </div>
    </div>
  </Teleport>
</template>
