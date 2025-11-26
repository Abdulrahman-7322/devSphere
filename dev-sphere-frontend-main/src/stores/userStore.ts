import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// (修复) 确保 id 始终按 string 处理
export interface UserInfo {
  id: string // 不再使用 number | string，统一为 string
  username: string
  realName?: string
  headUrl?: string
  avatar?: string
  role?: string
  email?: string
  mobile?: string
  deptId?: number
  gender?: number
  status?: number
  superAdmin?: number
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token') || null)
  const userInfo = ref<UserInfo | null>(
    localStorage.getItem('userInfo') ? JSON.parse(localStorage.getItem('userInfo')!) : null
  )

  const userAvatar = computed(() => {
    return userInfo.value?.headUrl || userInfo.value?.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${userInfo.value?.username || 'default'}`
  })

  const displayName = computed(() => {
    return userInfo.value?.realName || userInfo.value?.username || '极客用户'
  })

  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)

  function login(newToken: string, newUserInfo: UserInfo) {
    token.value = newToken
    // (修复) 确保存入的是 string
    if (newUserInfo) {
      newUserInfo.id = String(newUserInfo.id)
      userInfo.value = newUserInfo
      localStorage.setItem('userInfo', JSON.stringify(newUserInfo))
    }

    localStorage.setItem('token', newToken)
  }

  function logout() {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    userAvatar,
    displayName,
    isLoggedIn,
    login,
    logout
  }
})