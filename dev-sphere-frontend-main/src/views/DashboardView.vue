<script setup lang="ts">
import HeroStats from '../components/HeroStats.vue'
</script>

<template>
  <main class="flex flex-1 justify-center w-full max-w-[1400px] mx-auto px-4 lg:px-8 py-8 gap-8">
      
      <aside class="hidden lg:flex flex-col w-56 shrink-0 gap-6 sticky top-24 h-fit">
        <div class="flex flex-col gap-2">
          <router-link to="/" class="flex items-center gap-3 px-4 py-3 rounded-xl bg-brand-blue text-white font-bold shadow-sm shadow-brand-blue/20 transition-all">
            <span>🚀</span> 综合动态
          </router-link>

          <router-link to="/chat" class="flex items-center gap-3 px-4 py-3 rounded-xl font-medium text-vibrant-muted hover:bg-white hover:text-brand-blue hover:shadow-card transition-all">
            <span>💬</span> 即时聊天
          </router-link>
          <router-link to="/profile" class="flex items-center gap-3 px-4 py-3 rounded-xl font-medium text-vibrant-muted hover:bg-white hover:text-brand-blue hover:shadow-card transition-all">
            <span>⚔️</span> 我的面试
          </router-link>
          <a href="#" class="flex items-center gap-3 px-4 py-3 rounded-xl font-medium text-vibrant-muted hover:bg-white hover:text-brand-blue hover:shadow-card transition-all">
             <span>🔖</span> 收藏夹
          </a>
        </div>

        <div class="v-card p-5">
           <h3 class="text-xs font-bold text-vibrant-muted/80 uppercase mb-4 tracking-wider flex items-center gap-2">
             <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12h10"/><path d="M9 4v16"/><path d="m3 9 3 3-3 3"/><path d="M14 8V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v4a2 2 0 0 1-2 2h-4a2 2 0 0 1-2-2Z"/><path d="M14 20v-4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v4a2 2 0 0 1-2 2h-4a2 2 0 0 1-2-2Z"/></svg>
             热门技术栈
           </h3>
           <div class="flex flex-wrap gap-2.5">
             <span class="v-tag bg-blue-50 text-blue-600 hover:bg-blue-100">#SpringAI</span>
             <span class="v-tag bg-red-50 text-red-600 hover:bg-red-100">#OOM排查</span>
             <span class="v-tag bg-orange-50 text-orange-600 hover:bg-orange-100">#Netty</span>
             <span class="v-tag bg-purple-50 text-purple-600 hover:bg-purple-100">#MySQL调优</span>
             <span class="v-tag bg-emerald-50 text-emerald-600 hover:bg-emerald-100">#K8s</span>
           </div>
        </div>
      </aside>

      <section class="flex flex-col flex-1 max-w-3xl min-w-0">
        <HeroStats />

        <div class="flex items-center gap-1 mb-6 bg-slate-100/70 p-1.5 rounded-xl w-fit border border-slate-200/50">
          <button class="px-5 py-2 text-sm font-bold bg-white rounded-lg shadow-sm text-brand-blue">推荐</button>
          <button class="px-5 py-2 text-sm font-medium text-vibrant-muted hover:text-vibrant-main transition-colors">最新</button>
          <button class="px-5 py-2 text-sm font-medium text-vibrant-muted hover:text-vibrant-main transition-colors">热榜</button>
        </div>

        <div class="flex flex-col gap-6">
           <router-link to="/war-room/10086" class="relative overflow-hidden rounded-2xl bg-white shadow-card hover:shadow-card-hover transition-all duration-300 group cursor-pointer">
              <div class="h-1.5 w-full bg-functional-red"></div>
              <div class="p-6">
                  <div class="flex items-center justify-between mb-4">
                    <div class="flex items-center gap-2 text-functional-red font-bold">
                      <span class="relative flex h-3 w-3">
                        <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-functional-red opacity-75"></span>
                        <span class="relative inline-flex rounded-full h-3 w-3 bg-functional-red"></span>
                      </span>
                      P0 级事故响应中
                    </div>
                    <span class="text-xs font-mono font-medium bg-functional-red/5 text-functional-red px-2.5 py-1 rounded-md">32人在线</span>
                  </div>
                  <h2 class="text-xl font-bold mb-3 text-vibrant-main group-hover:text-brand-blue transition-colors">
                    核心订单库 CPU 100%，请求大量超时！
                  </h2>
                  <p class="text-vibrant-muted text-sm mb-6 leading-relaxed">
                    初步排查是慢 SQL 导致连接池爆满。需要精通 MySQL 索引优化和分库分表的同学协助排查。
                  </p>
                  <div class="flex items-center justify-between">
                    <div class="flex -space-x-3">
                      <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=1" class="h-9 w-9 rounded-full border-2 border-white bg-slate-50 shadow-sm" />
                      <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=2" class="h-9 w-9 rounded-full border-2 border-white bg-slate-50 shadow-sm" />
                      <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=3" class="h-9 w-9 rounded-full border-2 border-white bg-slate-50 shadow-sm" />
                      <div class="h-9 w-9 rounded-full border-2 border-white bg-slate-50 flex items-center justify-center text-xs font-bold text-slate-500 shadow-sm">+29</div>
                    </div>
                    <button class="px-5 py-2.5 bg-functional-red text-white text-sm font-bold rounded-xl hover:bg-red-700 hover:shadow-lg hover:shadow-functional-red/20 transition-all active:scale-95">
                      立即支援 →
                    </button>
                  </div>
              </div>
           </router-link>

           <div class="v-card p-6 cursor-pointer group">
              <div class="flex items-center justify-between mb-4">
                 <div class="flex items-center gap-3">
                   <img src="https://api.dicebear.com/7.x/identicon/svg?seed=Java" class="h-11 w-11 rounded-full bg-slate-50 border border-slate-100" />
                   <div>
                     <div class="font-bold text-vibrant-main">架构师老张</div>
                     <div class="text-xs text-vibrant-muted mt-0.5">1小时前 · 专栏文章</div>
                   </div>
                 </div>
                 <span class="text-brand-indigo bg-brand-indigo/5 px-3 py-1 rounded-full text-xs font-bold">精选</span>
              </div>
              <h2 class="text-xl font-bold mb-3 group-hover:text-brand-blue transition-colors">
                Netty 堆外内存泄漏排查：一个隐藏了3年的Bug
              </h2>
              <p class="text-vibrant-muted text-sm leading-relaxed mb-5">
                 排查了一周，终于定位到是在处理异常连接关闭时，自定义的 `ByteToMessageDecoder` 没有正确释放 `referenceCount`。导致堆外内存缓慢增长，最终触发系统级 OOM Killer。
              </p>
              <div class="bg-[#1e293b] rounded-xl p-4 mb-5 font-mono text-xs text-slate-300 overflow-hidden relative shadow-inner">
                <div class="absolute right-3 top-3 text-slate-500 text-[10px] font-bold tracking-widest">JAVA</div>
<pre><code><span class="text-purple-400">@Override</span>
<span class="text-blue-400">protected void</span> <span class="text-yellow-400">decode</span>(ChannelHandlerContext ctx, ByteBuf in, List&lt;Object&gt; out) {
    <span class="text-slate-500">// ❌ 致命错误：发生异常分支时忘记 release</span>
    <span class="text-purple-400">if</span> (in.readableBytes() < 4) <span class="text-purple-400">return</span>;
    
    <span class="text-slate-500">// ✅ 修复后：</span>
    <span class="text-slate-500">// ReferenceCountUtil.safeRelease(in);</span>
}</code></pre>
              </div>
              <div class="flex items-center gap-6 text-sm font-medium text-vibrant-muted">
                <span class="flex items-center gap-1.5 hover:text-brand-blue transition-colors"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-slate-400"><path d="M14 9a2 2 0 0 1-2 2H6l-4 4V4c0-1.1.9-2 2-2h8a2 2 0 0 1 2 2z"/><path d="M18 9h2a2 2 0 0 1 2 2v11l-4-4h-6a2 2 0 0 1-2-2v-1"/></svg> 45</span>
                <span class="flex items-center gap-1.5 hover:text-functional-red transition-colors"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-slate-400"><path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/></svg> 128</span>
              </div>
           </div>
        </div>
      </section>

      <aside class="hidden xl:flex flex-col w-72 shrink-0 gap-6 sticky top-24 h-fit">
        <div class="v-card p-6">
           <h3 class="text-sm font-bold mb-5 flex items-center gap-2 text-vibrant-main">
             <span>🏆</span> 社区活跃榜
           </h3>
           <div class="space-y-5">
             <div class="flex items-center gap-4">
               <div class="relative">
                 <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=top1" class="h-12 w-12 rounded-full bg-amber-50 border-2 border-white shadow-sm" />
                 <span class="absolute -bottom-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-functional-amber text-[10px] font-bold text-white ring-2 ring-white">1</span>
               </div>
               <div class="flex-1 min-w-0">
                 <div class="text-sm font-bold truncate text-vibrant-main">CodeMaster_Li</div>
                 <div class="text-xs font-medium text-vibrant-muted mt-0.5">贡献 128 次</div>
               </div>
               <span class="shrink-0 text-xs font-extrabold text-functional-amber bg-amber-100/50 px-2.5 py-1 rounded-lg">Lv.8</span>
             </div>
             <div class="flex items-center gap-4">
               <div class="relative">
                 <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=top2" class="h-12 w-12 rounded-full bg-slate-50 border-2 border-white shadow-sm" />
                 <span class="absolute -bottom-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-slate-400 text-[10px] font-bold text-white ring-2 ring-white">2</span>
               </div>
               <div class="flex-1 min-w-0">
                 <div class="text-sm font-bold truncate text-vibrant-main">BugHunter</div>
                 <div class="text-xs font-medium text-vibrant-muted mt-0.5">解决 56 个问题</div>
               </div>
               <span class="shrink-0 text-xs font-extrabold text-slate-600 bg-slate-100 px-2.5 py-1 rounded-lg">Lv.6</span>
             </div>
             <div class="flex items-center gap-4">
               <div class="relative">
                 <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=top3" class="h-12 w-12 rounded-full bg-orange-50 border-2 border-white shadow-sm" />
                 <span class="absolute -bottom-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-orange-700/50 text-[10px] font-bold text-white ring-2 ring-white">3</span>
               </div>
               <div class="flex-1 min-w-0">
                 <div class="text-sm font-bold truncate text-vibrant-main">NettyBoy</div>
                 <div class="text-xs font-medium text-vibrant-muted mt-0.5">发布 32 篇</div>
               </div>
               <span class="shrink-0 text-xs font-extrabold text-orange-700/70 bg-orange-100/50 px-2.5 py-1 rounded-lg">Lv.5</span>
             </div>
           </div>
        </div>

        <div class="v-card p-1 overflow-hidden border-brand-indigo/30">
           <div class="bg-gradient-to-br from-brand-blue/5 to-brand-indigo/10 p-5 rounded-lg">
             <h3 class="mb-3 text-sm font-bold flex items-center gap-2 text-vibrant-main">
               <span>🤖</span> AI 每日一题
             </h3>
             <div class="rounded-xl bg-vibrant-main p-3 font-mono text-xs text-slate-300 mb-3 shadow-inner overflow-x-auto">
<pre><code class="language-java"><span class="text-brand-indigo">if</span> (user != <span class="text-brand-blue">null</span>) {
  <span class="text-brand-indigo">return</span> user.getName().length();
}
<span class="text-slate-500">// 问：潜在的 NPE 风险在哪？</span></code></pre>
             </div>
             <input type="text" placeholder="输入答案，AI 判卷..." class="w-full rounded-xl border-0 bg-white px-4 py-3 text-xs font-medium shadow-sm placeholder-slate-400 focus:ring-2 focus:ring-brand-indigo/50 transition-all"/>
           </div>
        </div>
      </aside>

  </main>
</template>