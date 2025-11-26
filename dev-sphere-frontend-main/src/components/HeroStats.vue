<template>
  <div class="grid grid-cols-4 gap-5 mb-6">
    <div class="v-card p-5 flex items-center gap-4 relative overflow-hidden">
       <div class="absolute -right-4 -top-4 h-24 w-24 rounded-full bg-brand-blue/5"></div>
       <div class="h-12 w-12 rounded-xl bg-brand-blue/10 flex items-center justify-center text-brand-blue shrink-0 relative z-10">
         <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
       </div>
       <div>
         <div class="text-sm font-medium text-vibrant-muted">在线开发者</div>
         <div class="text-2xl font-extrabold text-vibrant-main flex items-baseline gap-2">
           1,024 <span class="relative flex h-2.5 w-2.5"><span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-functional-green opacity-75"></span><span class="relative inline-flex rounded-full h-2.5 w-2.5 bg-functional-green"></span></span>
         </div>
       </div>
    </div>
    <div class="v-card p-5 flex items-center gap-4 relative overflow-hidden">
       <div class="absolute -right-4 -top-4 h-24 w-24 rounded-full bg-brand-indigo/5"></div>
       <div class="h-12 w-12 rounded-xl bg-brand-indigo/10 flex items-center justify-center text-brand-indigo shrink-0 relative z-10">
         <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3Z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><line x1="12" x2="12" y1="19" y2="22"/></svg>
       </div>
       <div>
         <div class="text-sm font-medium text-vibrant-muted">AI 面试中</div>
         <div class="text-2xl font-extrabold text-vibrant-main">85 <span class="text-sm font-normal text-vibrant-muted">场</span></div>
       </div>
    </div>
    <div class="v-card p-5 flex items-center gap-4 relative overflow-hidden">
       <div class="absolute -right-4 -top-4 h-24 w-24 rounded-full bg-functional-red/5"></div>
       <div class="h-12 w-12 rounded-xl bg-functional-red/10 flex items-center justify-center text-functional-red shrink-0 relative z-10">
         <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
       </div>
       <div>
         <div class="text-sm font-bold text-functional-red">活跃作战室</div>
         <div class="text-2xl font-extrabold text-functional-red">12</div>
       </div>
    </div>
    <div class="v-card p-5 flex items-center gap-4 relative overflow-hidden">
       <div class="absolute -right-4 -top-4 h-24 w-24 rounded-full bg-functional-green/5"></div>
       <div class="h-12 w-12 rounded-xl bg-functional-green/10 flex items-center justify-center text-functional-green shrink-0 relative z-10">
         <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
       </div>
       <div>
         <div class="text-sm font-medium text-vibrant-muted">今日修复</div>
         <div class="text-2xl font-extrabold text-vibrant-main">4.5k</div>
       </div>
    </div>
  </div>
</template>