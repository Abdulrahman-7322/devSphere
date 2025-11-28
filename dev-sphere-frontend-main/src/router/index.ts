import { createRouter, createWebHistory } from 'vue-router'
// 👇 1. 引入新的 store hook
import { useUserStore } from '../stores/userStore'
import DashboardView from '../views/DashboardView.vue'
import MainLayout from '../layouts/MainLayout.vue'

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
      path: '/ai-interview',
      name: 'ai-interview',
      component: () => import('../views/interview/InterviewLanding.vue')
    },
    {
      path: '/ai-interview/session',
      name: 'ai-interview-session',
      component: () => import('../views/interview/AIInterviewer.vue')
    },
    // App Main Layout Routes
    {
      path: '/',
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'chat',
          name: 'chat',
          component: () => import('../views/ChatView.vue')
        },
        {
          path: 'moments',
          name: 'moments',
          component: () => import('../views/MomentView.vue')
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('../views/ProfileView.vue')
        }
      ]
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