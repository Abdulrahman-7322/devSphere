import axios from 'axios'
import { useUserStore } from '../stores/userStore'
import config from './config'

// 创建 axios 实例
const service = axios.create({
  baseURL: config.API_ABSOLUTE_URL,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json;charset=utf-8' }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 在发送请求之前做些什么
    const userStore = useUserStore()
    if (userStore.token) {
      // 如果有 token，把它放到 header 中
      // 注意：根据你的后端 LoginController，它可能是从参数取的，也可能是从 header 取的
      // 通常标准做法是放在 Authorization 头中
      config.headers['Authorization'] = userStore.token
      // 如果你的后端是自定义 header，比如 'access_token'，请在这里修改
      config.headers['access_token'] = userStore.token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 如果 code 不为  200，则视为错误
    if (res.code !== 200) {
      // 这里可以统一弹出错误提示，比如使用 ElementPlus 的 ElMessage
      console.error('[API Error]', res.msg || 'Error')

      // 如果是 401 未登录错误，可以自动登出
      // 如果是 401 未登录错误，可以自动登出
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        // 刷新页面，触发路由守卫，从而弹出登录框
        location.reload()
        return Promise.reject(new Error(res.msg || 'Unauthorized'))
      }
      return Promise.reject(new Error(res.msg || 'Error'))
    } else {
      return res.data
    }
  },
  (error) => {
    console.error('[Network Error]', error)
    // 处理 HTTP 状态码为 401 的情况
    if (error.response && error.response.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      location.reload()
    }
    return Promise.reject(error)
  }
)

export default service