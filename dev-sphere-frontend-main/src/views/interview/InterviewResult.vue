<template>
  <div class="min-h-screen bg-gray-900 text-white font-sans flex flex-col items-center justify-center relative overflow-hidden p-6">
    <!-- Background Effects -->
    <div class="absolute inset-0 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-gray-800 via-gray-900 to-black z-0"></div>

    <div class="relative z-10 max-w-2xl w-full bg-gray-800/50 backdrop-blur-xl rounded-3xl border border-gray-700 p-10 shadow-2xl text-center space-y-8 animate-fade-in-up">
      
      <!-- Icon -->
      <div class="w-24 h-24 rounded-full bg-gradient-to-br from-green-400 to-emerald-600 flex items-center justify-center text-5xl shadow-lg shadow-green-900/50 mx-auto">
        🎉
      </div>

      <!-- Title -->
      <div class="space-y-2">
        <h1 class="text-4xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-green-400 to-emerald-400">
          Interview Completed!
        </h1>
        <p class="text-gray-400 text-lg">
          Great job! Your session has been recorded.
        </p>
      </div>

      <!-- Stats (Mock for now, can be real later) -->
      <div class="grid grid-cols-3 gap-4 py-6 border-y border-gray-700/50">
        <div class="p-4 rounded-2xl bg-gray-900/50">
          <div class="text-2xl font-bold text-blue-400">5</div>
          <div class="text-xs text-gray-500 uppercase tracking-wider mt-1">Questions</div>
        </div>
        <div class="p-4 rounded-2xl bg-gray-900/50">
          <div class="text-2xl font-bold text-purple-400">15m</div>
          <div class="text-xs text-gray-500 uppercase tracking-wider mt-1">Duration</div>
        </div>
        <div class="p-4 rounded-2xl bg-gray-900/50">
          <div class="text-2xl font-bold text-yellow-400">Pending</div>
          <div class="text-xs text-gray-500 uppercase tracking-wider mt-1">Score</div>
        </div>
      </div>

      <!-- Actions -->
      <div class="flex flex-col sm:flex-row gap-4 justify-center">
        <button 
          @click="goHome"
          class="px-8 py-3 rounded-xl bg-gray-700 hover:bg-gray-600 text-white font-medium transition-all"
        >
          Back to Home
        </button>
        <button 
          @click="viewReport"
          class="px-8 py-3 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white font-bold shadow-lg shadow-blue-900/30 transition-all"
        >
          View Detailed Report
        </button>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();

const goHome = () => {
  router.push('/');
};

const viewReport = () => {
  // TODO: Implement report view
  alert('Detailed report generation is processing. Check back later!');
};
</script>

<style scoped>
.animate-fade-in-up {
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
