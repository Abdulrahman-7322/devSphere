import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { noticeService, type NoticeMessageVo } from '../services/noticeService'
import { useUserStore } from './userStore'
import config from '../utils/config'

// 对应后端的 ProcessResultTypeEnum (用于 *发送*)
export const ProcessResultType = {
  AGREE: 1,
  REFUSE: 2,
} as const;

// (新增) 对应后端返回的 ProcessResult *值* (用于 *接收*)
export const ProcessResultValue = {
  PENDING: "0",     // 👈 核心修复：后端返回的是字符串 "0"
  AGREE: "已同意",   // 👈 后端处理后返回的是描述
  REFUSE: "已拒绝",
} as const;

// 对应后端 NoticeTypeEnum
export const NoticeType = {
  SYSTEM: 1,
  GROUP: 2,
  USER: 3, // 好友申请
} as const;

export const useNotificationStore = defineStore('notification', () => {
  // === State ===
  const notifications = ref<NoticeMessageVo[]>([])
  const eventSource = ref<EventSource | null>(null)

  // === Getters ===

  // (核心修复) 筛选出待处理的好友申请
  const pendingFriendRequests = computed(() => {
    return notifications.value.filter(n => 
      n.noticeType === NoticeType.USER && 
      n.processResult === ProcessResultValue.PENDING // 👈 检查 "0"
    )
  })

  // (核心) 计算未读消息总数
  const unreadCount = computed(() => {
    return notifications.value.filter(n => n.readTarget === 0).length
  })

  // === Actions ===

  // 1. 拉取所有通知
  async function fetchNotifications() {
    try {
      const data = await noticeService.getNoticeList()
      notifications.value = data
    } catch (error) {
      console.error("获取通知列表失败:", error)
    }
  }

  // 2. 建立 SSE 连接 (由 App.vue 调用)
  function connectSSE() {
    if (eventSource.value) {
      eventSource.value.close()
    }
    
    const userStore = useUserStore()
    const token = userStore.token

    if (!token) {
      console.warn('[SSE] 无法连接，用户未登录或 Token 无效')
      return
    }

    const sseUrl = `${config.SSE_URL}?accessToken=${encodeURIComponent(token)}`
    
    console.log('[SSE] 准备连接通知服务:', sseUrl)
    eventSource.value = new EventSource(sseUrl)
    
    eventSource.value.onopen = () => {
      console.log('[SSE] 通知服务连接成功')
    }

    eventSource.value.onmessage = (event) => {
      console.log('[SSE] 收到推送:', event.data)
      fetchNotifications()
    }
    
    eventSource.value.onerror = (err) => {
      console.error('SSE 连接错误:', err)
      eventSource.value?.close()
      setTimeout(connectSSE, 5000)
    }
  }
  
  // 3. 关闭 SSE 连接
  function disconnectSSE() {
    if (eventSource.value) {
      console.log('[SSE] 断开连接')
      eventSource.value.close()
      eventSource.value = null
    }
  }

  // 4. 处理好友请求 (同意/拒绝)
  async function handleFriendRequest(notice: NoticeMessageVo, isAccept: boolean) {
    try {
      const action = isAccept ? ProcessResultType.AGREE : ProcessResultType.REFUSE
      
      await noticeService.handleNotice({
        id: notice.id,
        noticeType: notice.noticeType,
        processResult: action
      })
      
      // (乐观更新 UI)
      notice.processResult = isAccept ? ProcessResultValue.AGREE : ProcessResultValue.REFUSE
      notice.readTarget = 1
      
    } catch (error: any) {
      console.error("处理好友请求失败:", error)
    }
  }

  // 5. 标记为已读
  async function markAsRead(noticeId: number) {
     try {
       await noticeService.readNotice(noticeId)
       const notice = notifications.value.find(n => n.id === noticeId)
       if (notice) {
         notice.readTarget = 1
       }
     } catch (error) {
       console.error("标记已读失败:", error)
     }
  }

  return {
    notifications,
    pendingFriendRequests,
    unreadCount,
    fetchNotifications,
    connectSSE,
    disconnectSSE,
    handleFriendRequest,
    markAsRead
  }
})