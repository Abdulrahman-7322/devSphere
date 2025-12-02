<template>
  <div class="flex flex-col h-screen bg-gray-900 text-white overflow-hidden relative font-sans">
    <!-- Background Gradient -->
    <div class="absolute inset-0 bg-[radial-gradient(ellipse_at_center,_var(--tw-gradient-stops))] from-gray-800 via-gray-900 to-black z-0"></div>

    <!-- Header -->
    <div class="absolute top-0 left-0 w-full p-6 z-20 flex justify-between items-center bg-gradient-to-b from-black/80 to-transparent pointer-events-none">
      <div class="flex items-center gap-3 pointer-events-auto">
        <div class="w-3 h-3 rounded-full animate-pulse" :class="statusColorDot"></div>
        <div class="text-2xl font-bold tracking-tight shadow-black drop-shadow-lg">AI Interviewer</div>
      </div>
      <div class="flex gap-4 items-center pointer-events-auto">
        <div class="px-4 py-1.5 rounded-full bg-gray-800/80 backdrop-blur border border-gray-700 text-sm font-medium shadow-lg">
          Status: <span :class="statusColorText">{{ statusText }}</span>
        </div>
        <button @click="handleEndInterview" class="px-6 py-2 rounded-full bg-red-600/80 hover:bg-red-600 text-sm font-bold transition-all shadow-lg hover:shadow-red-900/40 backdrop-blur-sm">
          End Session
        </button>
      </div>
    </div>

    <!-- Main Content -->
    <div class="flex-1 flex flex-col relative z-10">
      
      <!-- Avatar Area (Top/Center) -->
      <div class="flex-1 flex justify-center items-center relative transition-all duration-500" :class="{'flex-[2]': hasStarted}">
        <div class="relative w-full h-full flex justify-center items-center">
           <!-- Placeholder for Avatar -->
           <Avatar :viseme="currentViseme" :audio-url="currentAudioUrl" @speak-end="onSpeakEnd" />
           
           <!-- Loading State -->
           <div v-if="isLoading" class="absolute inset-0 flex items-center justify-center bg-black/40 backdrop-blur-sm z-30">
             <div class="flex flex-col items-center gap-4">
               <div class="w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
               <p class="text-blue-400 font-medium animate-pulse">AI is thinking...</p>
             </div>
           </div>
        </div>
      </div>

      <!-- Interaction Area (Bottom) -->
      <div class="flex-1 bg-gray-950/80 backdrop-blur-xl border-t border-white/10 flex flex-col relative transition-all duration-500">
        
        <!-- Question Display -->
        <div class="flex-1 overflow-y-auto p-8 flex flex-col items-center justify-center text-center">
          <transition name="fade" mode="out-in">
            <div v-if="currentQuestion" :key="currentQuestion.id" class="max-w-4xl space-y-6">
              <div class="inline-block px-3 py-1 rounded-full bg-blue-500/10 text-blue-400 text-xs font-bold tracking-wider mb-2 border border-blue-500/20">
                QUESTION {{ questionCount }}
              </div>
              <h2 class="text-2xl md:text-3xl font-medium leading-relaxed text-gray-100">
                {{ currentQuestion.content }}
              </h2>
            </div>
            <div v-else class="text-gray-500 text-lg">
              Preparing your interview...
            </div>
          </transition>
        </div>

        <!-- Input Area -->
        <div class="p-6 pb-10 flex justify-center items-end gap-4 max-w-5xl mx-auto w-full">
          
          <!-- Text Input (Fallback/Auxiliary) -->
          <div class="flex-1 relative group">
            <textarea 
              v-model="answerText" 
              @keydown.enter.prevent="submitTextAnswer"
              placeholder="Type your answer here..." 
              class="w-full bg-gray-900/50 border border-gray-700 rounded-2xl px-6 py-4 text-gray-200 focus:outline-none focus:border-blue-500/50 focus:bg-gray-900 focus:ring-1 focus:ring-blue-500/50 transition-all resize-none h-16 focus:h-32 shadow-inner"
              :disabled="isLoading || !currentQuestion"
            ></textarea>
            <div class="absolute bottom-4 right-4 text-xs text-gray-600 group-focus-within:text-gray-500">
              Press Enter to submit
            </div>
          </div>

          <!-- Mic Button (Future Integration) -->
          <button 
            class="w-16 h-16 rounded-full flex items-center justify-center transition-all duration-300 shadow-xl border border-gray-700 bg-gray-800 text-gray-500 cursor-not-allowed opacity-50"
            title="Voice input coming soon"
          >
            <span class="text-2xl">🎤</span>
          </button>

          <!-- Submit Button -->
          <button 
            @click="submitTextAnswer"
            :disabled="!answerText.trim() || isLoading"
            class="h-16 px-8 rounded-2xl bg-blue-600 hover:bg-blue-500 text-white font-bold shadow-lg shadow-blue-900/20 disabled:opacity-50 disabled:cursor-not-allowed transition-all flex items-center gap-2"
          >
            <span>Send</span>
            <span class="text-xl">➤</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Avatar from './components/Avatar.vue';
import { startInterview, submitAnswer, completeInterview, type Question } from '@/api/interview';

const route = useRoute();
const router = useRouter();
const interviewId = route.params.id as string;

const status = ref<'IDLE' | 'THINKING' | 'SPEAKING' | 'LISTENING'>('IDLE');
const currentQuestion = ref<Question | null>(null);
const answerText = ref('');
const questionCount = ref(0);
const isLoading = ref(false);
const hasStarted = ref(false);

// Avatar props (Mock for now)
const currentViseme = ref(null);
const currentAudioUrl = ref('');

const statusText = computed(() => {
  switch (status.value) {
    case 'IDLE': return 'Ready';
    case 'THINKING': return 'Thinking...';
    case 'SPEAKING': return 'Speaking';
    case 'LISTENING': return 'Listening';
    default: return 'Unknown';
  }
});

const statusColorDot = computed(() => {
  switch (status.value) {
    case 'SPEAKING': return 'bg-blue-500';
    case 'LISTENING': return 'bg-green-500';
    case 'THINKING': return 'bg-purple-500';
    default: return 'bg-gray-500';
  }
});

const statusColorText = computed(() => {
  switch (status.value) {
    case 'SPEAKING': return 'text-blue-400';
    case 'LISTENING': return 'text-green-400';
    case 'THINKING': return 'text-purple-400';
    default: return 'text-gray-400';
  }
});

const initInterview = async () => {
  if (!interviewId) {
    alert('Invalid Interview ID');
    router.push('/interview');
    return;
  }

  isLoading.value = true;
  status.value = 'THINKING';
  try {
    const question = await startInterview(interviewId);
    if (question) {
      handleNewQuestion(question as unknown as Question);
    } else {
      // Handle case where no question is returned (maybe error or immediate finish)
      alert('Failed to start interview');
    }
  } catch (e) {
    console.error('Start error', e);
    alert('Failed to start interview session');
  } finally {
    isLoading.value = false;
  }
};

const handleNewQuestion = (question: Question) => {
  currentQuestion.value = question;
  questionCount.value++;
  status.value = 'SPEAKING'; // Ideally, we play TTS here
  hasStarted.value = true;
  
  // Simulate TTS delay/completion
  setTimeout(() => {
    status.value = 'LISTENING';
  }, 1000);
};

const submitTextAnswer = async () => {
  if (!answerText.value.trim() || isLoading.value) return;

  const answer = answerText.value;
  answerText.value = ''; // Clear input
  isLoading.value = true;
  status.value = 'THINKING';

  try {
    const nextQuestion = await submitAnswer(interviewId, answer);
    
    if (nextQuestion) {
      handleNewQuestion(nextQuestion as unknown as Question);
    } else {
      // Interview Complete
      finishInterview();
    }
  } catch (e) {
    console.error('Submit error', e);
    alert('Failed to submit answer');
    isLoading.value = false;
    status.value = 'LISTENING'; // Revert state
    answerText.value = answer; // Restore answer
  }
};

const finishInterview = async () => {
  try {
    await completeInterview(interviewId);
    router.push({ name: 'interview-result', params: { id: interviewId } });
  } catch (e) {
    console.error('Complete error', e);
    // Even if API fails, navigate to result
    router.push({ name: 'interview-result', params: { id: interviewId } });
  }
};

const handleEndInterview = async () => {
  if (confirm('Are you sure you want to end the interview?')) {
    await finishInterview();
  }
};

const onSpeakEnd = () => {
  if (status.value === 'SPEAKING') {
    status.value = 'LISTENING';
  }
};

onMounted(() => {
  initInterview();
});
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease, transform 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
