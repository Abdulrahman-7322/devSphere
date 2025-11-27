<script setup lang="ts">
import { computed, ref, watch, onUnmounted } from 'vue'
import { useCallStore } from '../../stores/callStore'
import webRTCService from '../../services/WebRTCService'

const callStore = useCallStore()
const remoteAudioRef = ref<HTMLAudioElement | null>(null)
const remoteVideoRef = ref<HTMLVideoElement | null>(null)
const localVideoRef = ref<HTMLVideoElement | null>(null)

// Duration Timer
const duration = ref('00:00')
let timerInterval: any = null

// Draggable Directive
const vDrag = {
  mounted(el: HTMLElement) {
    let isDragging = false
    let startX = 0
    let startY = 0
    let initialLeft = 0
    let initialTop = 0

    const onMouseDown = (e: MouseEvent) => {
      isDragging = true
      startX = e.clientX
      startY = e.clientY
      const rect = el.getBoundingClientRect()
      initialLeft = rect.left
      initialTop = rect.top
      el.style.cursor = 'grabbing'
      
      // Prevent selection
      e.preventDefault()
    }

    const onMouseMove = (e: MouseEvent) => {
      if (!isDragging) return
      const dx = e.clientX - startX
      const dy = e.clientY - startY
      
      // Boundary checks could be added here
      el.style.position = 'fixed'
      el.style.left = `${initialLeft + dx}px`
      el.style.top = `${initialTop + dy}px`
      el.style.bottom = 'auto'
      el.style.right = 'auto'
    }

    const onMouseUp = () => {
      isDragging = false
      el.style.cursor = 'grab'
    }

    el.addEventListener('mousedown', onMouseDown)
    window.addEventListener('mousemove', onMouseMove)
    window.addEventListener('mouseup', onMouseUp)

    // Touch support
    el.addEventListener('touchstart', (e) => {
      const touch = e.touches[0]
      if (touch) {
        onMouseDown({ clientX: touch.clientX, clientY: touch.clientY, preventDefault: () => {} } as any)
      }
    })
    window.addEventListener('touchmove', (e) => {
      const touch = e.touches[0]
      if (touch) {
        onMouseMove({ clientX: touch.clientX, clientY: touch.clientY } as any)
      }
    })
    window.addEventListener('touchend', onMouseUp)
  }
}

watch(() => callStore.startTime, (newTime) => {
  if (newTime) {
    timerInterval = setInterval(() => {
      const now = new Date()
      const diff = Math.floor((now.getTime() - newTime.getTime()) / 1000)
      const mins = Math.floor(diff / 60).toString().padStart(2, '0')
      const secs = (diff % 60).toString().padStart(2, '0')
      duration.value = `${mins}:${secs}`
    }, 1000)
  } else {
    if (timerInterval) clearInterval(timerInterval)
    duration.value = '00:00'
  }
})

// Watch for remote stream changes
watch(() => webRTCService.remoteStream.value, (newStream) => {
  console.log('[CallModal] Remote stream changed', newStream)
  if (newStream) {
    // Audio binding
    if (remoteAudioRef.value) {
      remoteAudioRef.value.srcObject = newStream
      remoteAudioRef.value.play().catch(e => console.error('Audio auto-play failed', e))
    }
    // Video binding
    if (remoteVideoRef.value) {
      remoteVideoRef.value.srcObject = newStream
      remoteVideoRef.value.play().catch(e => console.error('Remote video auto-play failed', e))
    }
  }
}, { immediate: true, flush: 'post' })

// Watch for local stream changes (for video self-view)
watch(() => webRTCService.localStream.value, (newStream) => {
  console.log('[CallModal] Local stream changed', newStream)
  if (newStream && localVideoRef.value) {
    localVideoRef.value.srcObject = newStream
    localVideoRef.value.play().catch(e => console.error('Local video auto-play failed', e))
  }
}, { immediate: true, flush: 'post' })

const handleAccept = () => {
  webRTCService.acceptCall()
}

const handleReject = () => {
  webRTCService.rejectCall()
}

const handleHangup = () => {
  webRTCService.hangup()
}

const toggleMute = () => {
  webRTCService.toggleMute()
}

const toggleVideo = () => {
  webRTCService.toggleVideo()
}

const switchCamera = () => {
  webRTCService.switchCamera()
}

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
})
</script>

<template>
  <Teleport to="body">
    <div v-if="callStore.callState !== 'idle'" class="fixed inset-0 z-[9999] flex items-center justify-center bg-slate-900 overflow-hidden">
      
      <!-- Remote Audio Element (Always needed for audio) -->
      <audio ref="remoteAudioRef" autoplay></audio>

      <!-- VIDEO MODE UI -->
      <template v-if="callStore.callType === 'video'">
        <!-- Remote Video (Full Screen Background) -->
        <div class="absolute inset-0 w-full h-full bg-black">
          <video 
            ref="remoteVideoRef" 
            class="w-full h-full object-cover" 
            autoplay 
            playsinline
          ></video>
          <!-- Placeholder if no video yet -->
          <div v-if="!webRTCService.remoteStream.value" class="absolute inset-0 flex items-center justify-center bg-slate-800/50 backdrop-blur-sm">
             <div class="flex flex-col items-center gap-4">
                <img :src="callStore.remoteUserInfo?.avatar" class="w-24 h-24 rounded-full border-4 border-white/20 animate-pulse" />
                <span class="text-white/80 text-lg">等待对方视频...</span>
             </div>
          </div>
        </div>

        <!-- Local Video (Draggable PiP) -->
        <div 
          v-if="callStore.callState === 'connected' || callStore.callState === 'calling'"
          v-drag
          class="fixed right-4 top-4 w-32 h-48 bg-black rounded-xl overflow-hidden shadow-2xl border border-white/10 cursor-grab active:cursor-grabbing z-10 hover:scale-105 transition-transform"
        >
          <video 
            ref="localVideoRef" 
            class="w-full h-full object-cover mirror" 
            autoplay 
            playsinline 
            muted
          ></video>
          <div v-if="!callStore.isCameraOn" class="absolute inset-0 flex items-center justify-center bg-slate-800">
            <svg xmlns="http://www.w3.org/2000/svg" class="w-8 h-8 text-slate-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m16 13 5.223 3.482a.5.5 0 0 0 .777-.416V7.87a.5.5 0 0 0-.752-.432L16 10.5"/><rect x="2" y="6" width="14" height="12" rx="2"/><line x1="2" y1="2" x2="22" y2="22"/></svg>
          </div>
        </div>
      </template>

      <!-- AUDIO MODE UI (Or Video Overlay) -->
      <div class="absolute inset-0 pointer-events-none flex flex-col justify-between p-8">
        
        <!-- Top Info (Only for Audio or Incoming Video) -->
        <div v-if="callStore.callType === 'audio' || callStore.callState === 'incoming'" class="flex flex-col items-center gap-4 mt-12 pointer-events-auto">
           <img 
            :src="callStore.remoteUserInfo?.avatar" 
            class="w-32 h-32 rounded-full border-4 border-white/10 shadow-2xl"
            :class="callStore.callState === 'incoming' ? 'animate-pulse-slow' : ''"
          />
          <h2 class="text-2xl font-bold text-white tracking-wide drop-shadow-md">{{ callStore.remoteUserInfo?.name }}</h2>
          <p class="text-slate-200 font-medium drop-shadow-md bg-black/20 px-4 py-1 rounded-full backdrop-blur-sm">
            <span v-if="callStore.callState === 'incoming'">
              {{ callStore.callType === 'video' ? '邀请你进行视频通话...' : '邀请你进行语音通话...' }}
            </span>
            <span v-else-if="callStore.callState === 'calling'">正在等待对方接受...</span>
            <span v-else-if="callStore.callState === 'connected'">{{ duration }}</span>
            <span v-else-if="callStore.callState === 'ended'">通话结束</span>
          </p>
        </div>
        <div v-else></div> <!-- Spacer -->

        <!-- Controls Bar -->
        <div class="pointer-events-auto flex items-center justify-center gap-8 pb-8">
            
            <!-- Incoming: Reject / Accept -->
            <template v-if="callStore.callState === 'incoming'">
              <button @click="handleReject" class="flex flex-col items-center gap-2 group">
                <div class="w-16 h-16 rounded-full bg-rose-500 flex items-center justify-center text-white shadow-lg group-hover:bg-rose-600 transition-all group-active:scale-95">
                  <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.68 13.31a16 16 0 0 0 3.41 2.6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7 2 2 0 0 1 1.72 2v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.42 19.42 0 0 1-3.33-2.67m-2.67-3.34a19.79 19.79 0 0 1-3.07-8.63A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91"/><line x1="23" y1="1" x2="1" y2="23"/></svg>
                </div>
                <span class="text-white text-sm font-medium drop-shadow-md">拒绝</span>
              </button>

              <button @click="handleAccept" class="flex flex-col items-center gap-2 group">
                <div class="w-16 h-16 rounded-full bg-emerald-500 flex items-center justify-center text-white shadow-lg group-hover:bg-emerald-600 transition-all group-active:scale-95 animate-bounce-subtle">
                  <svg v-if="callStore.callType === 'video'" xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M23 7l-7 5 7 5V7z"/><rect x="1" y="5" width="15" height="14" rx="2" ry="2"/></svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
                </div>
                <span class="text-white text-sm font-medium drop-shadow-md">接听</span>
              </button>
            </template>

            <!-- Connected Controls -->
            <template v-else>
               <!-- Mute -->
               <button @click="toggleMute" class="control-btn" :class="callStore.isMuted ? 'bg-white text-slate-900' : 'bg-white/20 text-white'">
                 <svg v-if="!callStore.isMuted" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><line x1="12" y1="19" x2="12" y2="23"/><line x1="8" y1="23" x2="16" y2="23"/></svg>
                 <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="1" y1="1" x2="23" y2="23"/><path d="M9 9v3a3 3 0 0 0 5.12 2.12M15 9.34V4a3 3 0 0 0-5.94-.6"/><path d="M17 16.95A7 7 0 0 1 5 12v-2m14 0v2a7 7 0 0 1-.11 1.23"/><line x1="12" y1="19" x2="12" y2="23"/><line x1="8" y1="23" x2="16" y2="23"/></svg>
               </button>

               <!-- Video Controls (Only for Video Call) -->
               <template v-if="callStore.callType === 'video'">
                  <!-- Toggle Camera -->
                  <button @click="toggleVideo" class="control-btn" :class="!callStore.isCameraOn ? 'bg-white text-slate-900' : 'bg-white/20 text-white'">
                    <svg v-if="callStore.isCameraOn" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M23 7l-7 5 7 5V7z"/><rect x="1" y="5" width="15" height="14" rx="2" ry="2"/></svg>
                    <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m16 13 5.223 3.482a.5.5 0 0 0 .777-.416V7.87a.5.5 0 0 0-.752-.432L16 10.5"/><rect x="2" y="6" width="14" height="12" rx="2"/><line x1="2" y1="2" x2="22" y2="22"/></svg>
                  </button>

                  <!-- Switch Camera -->
                  <button @click="switchCamera" class="control-btn bg-white/20 text-white">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/><circle cx="12" cy="10" r="3"/><path d="M12 2v2"/><path d="m4.93 4.93 1.41 1.41"/><path d="M2 12h2"/><path d="m4.93 19.07 1.41-1.41"/><path d="M12 22v-2"/><path d="m19.07 19.07-1.41-1.41"/><path d="M22 12h-2"/><path d="m19.07 4.93-1.41 1.41"/></svg>
                  </button>
               </template>

               <!-- Hangup -->
               <button @click="handleHangup" class="flex flex-col items-center gap-2 group">
                <div class="w-16 h-16 rounded-full bg-rose-500 flex items-center justify-center text-white shadow-lg group-hover:bg-rose-600 transition-all group-active:scale-95">
                  <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.68 13.31a16 16 0 0 0 3.41 2.6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7 2 2 0 0 1 1.72 2v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.42 19.42 0 0 1-3.33-2.67m-2.67-3.34a19.79 19.79 0 0 1-3.07-8.63A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91"/><line x1="23" y1="1" x2="1" y2="23"/></svg>
                </div>
              </button>
            </template>

        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.animate-pulse-slow {
  animation: pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}
.animate-bounce-subtle {
  animation: bounce 2s infinite;
}
.control-btn {
  @apply w-14 h-14 rounded-full backdrop-blur-md flex items-center justify-center shadow-lg hover:bg-white/30 transition-all active:scale-95;
}
.mirror {
  transform: scaleX(-1);
}
</style>

<style scoped>
.animate-pulse-slow {
  animation: pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}
.animate-bounce-subtle {
  animation: bounce 2s infinite;
}
</style>
