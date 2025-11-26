import { createRouter, createWebHistory } from 'vue-router'
// 👇 1. 引入新的 store hook
import { useUserStore } from '../stores/userStore'
import DashboardView from '../views/DashboardView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: DashboardView
    },
    {
      path: '/war-room/:id',
      name: 'war-room',
      component: () => import('../views/WarRoomView.vue')
    },
    {
      path: '/interview/:id',
      name: 'interview',
      component: () => import('../views/InterviewView.vue')
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/chat',
      name: 'chat',
      component: () => import('../views/ChatView.vue'),
      meta: { requiresAuth: true } // 聊天必须登录
    },
    {
      path: '/moments',
      name: 'moments',
      component: () => import('../views/MomentView.vue'),
      meta: { requiresAuth: true }
    }
  ]
})

// === 全局路由守卫 ===
router.beforeEach((to, from, next) => {
  // 👇 2. 在守卫内部获取 store 实例
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    console.log(`[路由守卫] 拦截未登录访问: ${to.fullPath}`)
    next({
      path: '/',
      query: { auth_redirect: to.fullPath }
    })
  } else {
    next()
  }
})

export default router