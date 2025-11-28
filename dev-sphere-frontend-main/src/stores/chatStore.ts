// stores/chatStore.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import wsService, { WSReqType } from '../services/WebSocketService'
import {
  chatService,
  type RoomVo,
  type ChatMessageResp,
  type GroupDetailVo,
  type GroupMemberVo,
  type FriendContentVo,
  type CursorPage
} from '../services/chatService'
import { useUserStore } from './userStore'

// utility: ensure a value is a Date object
function toDate(v: Date | string | number): Date {
  return v instanceof Date ? v : new Date(v);
}

export const MsgType = {
  GROUP: 1,
  PRIVATE: 2,
} as const

export type MsgType = typeof MsgType[keyof typeof MsgType]

// 前端会话模型
export interface Conversation {
  id: number
  type: MsgType
  targetId: string
  name: string
  avatar: string
  lastMessage: string
  lastTime: Date
  unreadCount: number
  isOnline?: boolean
  memberCount?: number
}

// 前端消息模型
export interface ChatMessage {
  id: string            // 如果是临时消息，这里也使用 tempId（保证唯一）
  tempId?: string       // 如果本条是临时消息，保存 tempId（便于后续匹配）
  roomId: number
  senderId: string
  senderName: string
  senderAvatar: string
  content: string
  time: Date
  status: 'sending' | 'sent' | 'error'
  messageType?: number // [Added] Content Type
}

// (发送用) 对应后端 ChatMessageVo
interface ChatMessageVo {
  type: MsgType
  content: string
  tempId?: string
  messageType?: number // Add messageType
}

// ACK 后端返回结构（前端使用）
export interface WSACK {
  tempId: string
  serverMsgId?: string
  serverTs?: string | number // 可为时间戳或可解析字符串
}

export const useChatStore = defineStore('chat', () => {
  const userStore = useUserStore()

  const activeRoomId = ref<number | null>(null)

  // [重构] 使用 Map 存储会话，提高查找效率
  const conversationsMap = ref(new Map<number, Conversation>())

  // [重构] 通过 computed 自动生成排序后的会话列表，使用 toDate 保证比较正确
  const conversations = computed(() =>
    Array.from(conversationsMap.value.values())
      .sort((a, b) => toDate(b.lastTime).getTime() - toDate(a.lastTime).getTime())
  )

  // messageMap: key = roomId, value = ordered messages (oldest -> newest)
  const messageMap = ref<Record<number, ChatMessage[]>>({})
  const roomPagination = ref<Record<number, { nextCursor: string | null; hasMore: boolean }>>({})

  const isGroupDetailOpen = ref(false)
  const isChatDetailOpen = ref(false) // 私聊详情
  const currentGroupDetail = ref<GroupDetailVo | null>(null)
  const currentGroupMembers = ref<GroupMemberVo[]>([])
  const isLoadingGroupDetail = ref(false)

  const contactGroups = ref<FriendContentVo[]>([])
  const isLoadingContacts = ref(false)

  // [V6] 宽限期状态
  const activityTimer = ref<any>(null)
  const isGracePeriodActive = ref(false)
  const GRACE_PERIOD_MS = 5000 // 5 秒宽限期

  // [V7] 页面大小
  const HISTORY_PAGE_SIZE = 30

  // lastMessageDirection: 'append' 表示有新消息添加到末尾（下方）
  // 'prepend' 表示加载历史消息（在上方插入）。组件根据它判断是否滚到底部
  const lastMessageDirection = ref<'append' | 'prepend' | null>(null)

  // 标记某房间是否已经做过首屏初始加载（true 表示首次加载完成）
  const loadedInitialMessages = ref<Record<number, boolean>>({})

  // 当 setActiveRoom 完成首次加载时，记录该 roomId（供组件滚动判断）
  const lastOpenedLoadedRoomId = ref<number | null>(null)

  // 每个房间上次调用 markAsRead 的时间戳（ms）
  const lastMarkTs = ref<Record<number, number>>({})

  // 节流阈值（调用 markAsRead 的最小间隔）
  const READ_THROTTLE_MS = 5000 // 5s

  const activeConversation = computed(() => {
    if (!activeRoomId.value) return null
    return conversationsMap.value.get(activeRoomId.value) || null
  })

  const activeMessages = computed(() => {
    if (!activeRoomId.value) return []
    return messageMap.value[activeRoomId.value] || []
  })

  const totalUnreadCount = computed(() => {
    return Array.from(conversationsMap.value.values())
      .reduce((total, conv) => total + conv.unreadCount, 0)
  })

  // -------------------------
  // Pending / timers for ACK
  // -------------------------
  // pendingAckTimers: tempId -> timerId
  const pendingAckTimers = ref<Record<string, number>>({})

  // optional mapping from tempId -> serverMsgId (server-side generated id before DB persisted)
  const tempToServerMsgId = ref<Record<string, string>>({})

  // 超时时间（毫秒），发送后如果超过此时间没有收到 ACK，就把本地消息标记 error
  const SEND_ACK_TIMEOUT_MS = 3000 // 3s，可按需调整

  // -------------------------
  // 内部辅助函数
  // -------------------------

  function startAckTimer(tempId: string, roomId: number) {
    // 清理旧定时器（保险）
    clearAckTimer(tempId)

    const timer = window.setTimeout(() => {
      // 超时处理：把本地对应消息标记为 error
      try {
        const arr = messageMap.value[roomId]
        if (!arr) return
        for (let i = arr.length - 1; i >= 0; i--) {
          const m = arr[i]
          if (m && ((m.tempId && m.tempId === tempId) || m.id === tempId)) {
            m.status = 'error'
            break
          }
        }
      } finally {
        // 清除 pending
        delete pendingAckTimers.value[tempId]
      }
    }, SEND_ACK_TIMEOUT_MS)

    pendingAckTimers.value[tempId] = timer
  }

  function clearAckTimer(tempId: string) {
    const t = pendingAckTimers.value[tempId]
    if (t) {
      clearTimeout(t)
      delete pendingAckTimers.value[tempId]
    }
  }

  // -------------------------
  // Actions (核心修改)
  // -------------------------

  /**
   * 发送消息（改造）
   * - 生成 tempId
   * - 本地 push 一条临时消息（status = 'sending'）
   * - 通过 wsService.send 发送（payload 包含 tempId）
   * - 启动 ACK 超时定时器
   */
  function sendMessage(content: string, messageType: number = 1) { // Default to TEXT (1)
    const conv = activeConversation.value
    const userInfo = userStore.userInfo
    if (!conv || !userInfo) return

    const tempId = 'temp_' + Date.now() + '_' + Math.floor(Math.random() * 10000)
    const newMessage: ChatMessage = {
      id: tempId,
      tempId: tempId,
      roomId: conv.id,
      senderId: String(userInfo.id),
      senderName: userStore.displayName ?? (userInfo.username || '我'),
      senderAvatar: userStore.userAvatar ?? (userInfo.avatar || ''),
      content: content,
      time: new Date(),
      status: 'sending',
      messageType: messageType, // [Added]
    }

    if (!messageMap.value[conv.id]) {
      messageMap.value[conv.id] = []
    }
    // append 到末尾（UI 已按顺序渲染）
    if (!messageMap.value[conv.id]) {
      messageMap.value[conv.id] = []
    }
    messageMap.value[conv.id].push(newMessage)

    const chatVo: ChatMessageVo = {
      type: conv.type,
      content: content,
      tempId: tempId,
      messageType: messageType, // Pass messageType
    }

    const targetId = conv.type === MsgType.PRIVATE ? conv.targetId : String(conv.id)

    try {
      wsService.send({
        type: WSReqType.CHAT,
        userId: String(targetId),
        data: JSON.stringify(chatVo),
      })

      // 启动 ACK 超时定时器：如果在超时时间内未收到 ACK，则标记为 error（UI 将显示重试按钮）
      startAckTimer(tempId, conv.id)
    } catch (err) {
      // 发送失败（WS 未就绪等），立即标记 error
      console.error('WS 发送失败:', err)
      const arr = messageMap.value[conv.id]
      if (arr) {
        for (let i = arr.length - 1; i >= 0; i--) {
          const m = arr[i]
          if (m && m.id === tempId) {
            m.status = 'error'
            break
          }
        }
      }
    }

    conv.lastMessage = content
    conv.lastTime = new Date()
    // [关键修复] 显式重新 set 以触发 computed 重新排序
    conversationsMap.value.set(conv.id, conv)

    // 标记方向为 append（新消息）
    lastMessageDirection.value = 'append'

    // 发送也视为用户活动（重置宽限期）
    handleUserActivity()
  }

  /**
   * 发送通话状态消息（专门用于 WebRTCService）
   * - 查找目标会话
   * - 本地插入消息
   * - 发送 WS
   */
  async function sendCallMessage(targetId: string, content: string, messageType: number) {
    const userInfo = userStore.userInfo
    if (!userInfo) return

    // 1. 查找会话
    let conv = Array.from(conversationsMap.value.values()).find(c => {
      if (c.type === MsgType.PRIVATE) {
        return String(c.targetId) === String(targetId)
      } else {
        // For group, targetId is the roomId
        return String(c.id) === String(targetId)
      }
    })

    // 如果本地没有会话，尝试简单的 WS 发送（不存本地），或者忽略
    // 为了保证一致性，建议至少发送 WS
    if (!conv) {
      console.warn('未找到会话，仅发送 WS 消息', targetId)
      // We need to guess the type or pass it in. For now, let's assume if it looks like a room ID it might be group?
      // Actually, WebRTCService knows the mode. But here we only have targetId.
      // Let's try to infer or default to PRIVATE if unknown, but better to rely on caller passing context if possible.
      // However, to keep signature simple, let's check if targetId matches an existing group in some other way or just default.
      // Since we can't easily know, we might default to PRIVATE but this is risky for groups.
      // BETTER APPROACH: The caller (WebRTCService) knows if it's a group call.
      // But `sendCallMessage` signature is (targetId, content, messageType).
      // Let's try to find if it's a group ID from `conversationsMap` even if not "active"? No, we just did that.

      // Fallback: Send as PRIVATE by default if not found, or maybe we should just fail?
      // Let's send as is.
      const chatVo = {
        type: MsgType.PRIVATE, // Potential bug if it's actually a group but not in list.
        content: content,
        tempId: 'call_' + Date.now(),
        messageType: messageType
      }
      wsService.send({
        type: WSReqType.CHAT,
        userId: String(targetId),
        data: JSON.stringify(chatVo)
      })
      return
    }

    // 2. 本地插入
    const tempId = 'temp_call_' + Date.now() + '_' + Math.floor(Math.random() * 1000)
    const newMessage: ChatMessage = {
      id: tempId,
      tempId: tempId,
      roomId: conv.id,
      senderId: String(userInfo.id),
      senderName: userStore.displayName ?? (userInfo.username || '我'),
      senderAvatar: userStore.userAvatar ?? (userInfo.avatar || ''),
      content: content,
      time: new Date(),
      status: 'sending',
      messageType: messageType,
    }

    if (!messageMap.value[conv.id]) {
      messageMap.value[conv.id] = []
    }
    messageMap.value[conv.id].push(newMessage)

    // 3. 发送 WS
    const chatVo = {
      type: conv.type,
      content: content,
      tempId: tempId,
      messageType: messageType,
    }

    try {
      wsService.send({
        type: WSReqType.CHAT,
        userId: String(conv.targetId),
        data: JSON.stringify(chatVo),
      })
      startAckTimer(tempId, conv.id)
    } catch (err) {
      console.error('WS 发送通话消息失败:', err)
      newMessage.status = 'error'
    }

    // 4. 更新会话状态
    conv.lastMessage = content // 这里可能显示为代码，但 ChatBox 会格式化，列表页可能需要单独处理（暂不处理列表页格式化）
    conv.lastTime = new Date()
    conversationsMap.value.set(conv.id, conv)

    if (activeRoomId.value === conv.id) {
      lastMessageDirection.value = 'append'
    }
  }

  /**
   * 处理 Server 的 ACK（必须由 WS 层分发到这里）
   * - ACK 包含 tempId, serverMsgId, serverTs
   * - 将本地对应临时消息标记为 'sent'（表示服务器已接收并写入队列）
   * - 取消超时定时器
   */
  function ackMessage(ack: WSACK) {
    if (!ack || !ack.tempId) return
    const tempId = ack.tempId

    // 记录 serverMsgId（可选）
    if (ack.serverMsgId) {
      tempToServerMsgId.value[tempId] = ack.serverMsgId
    }

    // 取消 ACK 超时定时器
    clearAckTimer(tempId)

    // 将本地对应 temp 消息标记为 sent（仍保留 tempId，等待最终 ChatMessageResp）
    // 我们以 roomMap 中遍历查找（从尾部查找更快）
    for (const roomIdStr in messageMap.value) {
      const roomId = Number(roomIdStr)
      const arr = messageMap.value[roomId]
      if (!arr) continue
      for (let i = arr.length - 1; i >= 0; i--) {
        const m = arr[i]
        if (m && ((m.tempId && m.tempId === tempId) || m.id === tempId)) {
          // 标记为已被服务器接收（但尚未持久化）
          m.status = 'sent'
          // optionally attach serverTs as time if available (注意：最终会由 ChatMessageResp 覆盖)
          if (ack.serverTs) {
            const ts = typeof ack.serverTs === 'number' ? new Date(ack.serverTs) : new Date(String(ack.serverTs))
            if (!isNaN(ts.getTime())) {
              m.time = ts
            }
          }
          return
        }
      }
    }
  }

  /**
   * 处理后端的 ChatMessageResp（最终推送）
   * - 如果 resp.tempId 存在并能匹配到本地临时消息：更新那条消息（替换 id、time、status、清除 tempId）
   * - 否则：把该消息 push 到 room
   */
  function receiveMessage(data: ChatMessageResp) {
    if (!data) return
    const roomId = Number(data.roomId ?? null) as number
    // compatibility: ChatMessageResp in your backend has roomId, fromUser, message, tempId
    const senderId = data.fromUser?.uid ? String(data.fromUser.uid) : ''
    const tempId = data.tempId
    const msgObj = data.message ?? {}

    // normalize
    const persistMsgId = msgObj.id !== undefined && msgObj.id !== null ? String(msgObj.id) : undefined
    const sendTime = msgObj.sendTime ? new Date(msgObj.sendTime) : new Date()
    const content = msgObj.content ?? ''

    // First: if tempId provided, try to match local temp message and update it (avoid duplicate)
    if (tempId) {
      const arr = messageMap.value[roomId]
      if (arr) {
        for (let i = arr.length - 1; i >= 0; i--) {
          const m = arr[i]
          if (m && ((m.tempId && m.tempId === tempId) || m.id === tempId)) {
            // Update the local message in-place:
            // - set real id if provided by server push (数据库自增 id)
            if (persistMsgId) m.id = persistMsgId
            // - clear tempId
            delete m.tempId
            // - ensure status = sent
            m.status = 'sent'
            // - update time & content (server authoritative)
            m.time = sendTime
            m.content = content
            // cancel any pending ack timer just in case
            clearAckTimer(tempId)
            // done — do not push a duplicate
            // update conversation lastMessage/time
            const conv = conversationsMap.value.get(roomId)
            if (conv) {
              conv.lastMessage = m.content
              conv.lastTime = m.time
              // [重构] 不再需要手动置顶，computed 会自动根据 lastTime 排序
            }
            lastMessageDirection.value = 'append'
            return
          }
        }
      }
    }

    // If no matching temp message found, insert as a new received message
    const newMessage = mapRespToChatMessage(data, roomId)
    if (!messageMap.value[roomId]) messageMap.value[roomId] = []
    messageMap.value[roomId].push(newMessage)

    // update conversation view & unread logic
    // 确保 roomId 为数字
    const numericRoomId = Number(roomId)
    let conv = conversationsMap.value.get(numericRoomId)

    if (conv) {
      // 会话存在：直接更新属性
      conv.lastMessage = newMessage.content
      conv.lastTime = toDate(newMessage.time)

      if (activeRoomId.value === numericRoomId) {
        markRoomAsReadInternal(numericRoomId)
      } else {
        conv.unreadCount = (conv.unreadCount || 0) + 1
      }

      // [关键修复] 显式重新 set 以触发 computed 重新排序
      conversationsMap.value.set(numericRoomId, conv)
    } else {
      // 会话不存在：立即创建临时会话（同步），防止多条消息并发导致重复创建
      const tempConv: Conversation = {
        id: numericRoomId,
        type: data.message?.type === 2 ? MsgType.GROUP : MsgType.PRIVATE,
        targetId: newMessage.senderId,
        name: newMessage.senderName || '新会话',
        avatar: newMessage.senderAvatar || `https://api.dicebear.com/7.x/identicon/svg?seed=${numericRoomId}`,
        lastMessage: newMessage.content,
        lastTime: toDate(newMessage.time),
        unreadCount: 1,
        isOnline: false,
      }

      // 1. 立即写入 Map，UI 即时更新
      conversationsMap.value.set(numericRoomId, tempConv)

        // 2. 异步获取完整信息补全
        ; (async () => {
          try {
            const roomVo = await chatService.getRoomDetail(numericRoomId)
            if (roomVo) {
              const fullConv = mapRoomVoToConversation(roomVo)
              // 保持引用，只更新属性
              Object.assign(tempConv, {
                ...fullConv,
                lastTime: toDate(fullConv.lastTime)
              })
              // 再次 set 触发更新（确保名字/头像变化被渲染）
              conversationsMap.value.set(numericRoomId, tempConv)
            }
          } catch (e) {
            console.error('获取会话详情失败:', e)
          }
        })()
    }

    lastMessageDirection.value = 'append'
  }


  /**
   * 标记会话为已读（内部）
   * - UI 立即把 unreadCount 置 0（互动即时反馈）
   * - 只有当 conv.unreadCount > 0 且超过节流阈值时才调用后端
   */
  function markRoomAsReadInternal(roomId: number) {
    const conv = conversationsMap.value.get(roomId)
    if (!conv) return

    const hadUnread = (conv.unreadCount ?? 0) > 0
    conv.unreadCount = 0
    if (!hadUnread) return

    const lastTs = lastMarkTs.value[roomId] ?? 0
    const now = Date.now()
    if (now - lastTs < READ_THROTTLE_MS) {
      lastMarkTs.value[roomId] = now
      return
    }

    lastMarkTs.value[roomId] = now

    chatService
      .markAsRead(roomId)
      .then(() => {
        // success
      })
      .catch((err) => {
        console.error(`Failed to mark room ${roomId} as read:`, err)
      })
  }

  function handleUserActivity() {
    if (!activeRoomId.value) return
    markRoomAsReadInternal(activeRoomId.value)

    isGracePeriodActive.value = true
    if (activityTimer.value) {
      clearTimeout(activityTimer.value)
    }
    activityTimer.value = setTimeout(() => {
      isGracePeriodActive.value = false
      activityTimer.value = null
    }, GRACE_PERIOD_MS)
  }

  // [V8] 会话列表加载状态
  const isSessionListLoading = ref(false)

  async function loadSessionList() {
    if (isSessionListLoading.value) return
    isSessionListLoading.value = true
    try {
      const res = await chatService.getRoomList({ page: 1, limit: 50 })
      const newRooms = res.records.map((room) => mapRoomVoToConversation(room))

      // [重构] 直接更新 Map，保留现有对象引用
      for (const newConv of newRooms) {
        const numericId = Number(newConv.id)
        const existing = conversationsMap.value.get(numericId)

        // 确保时间为 Date
        const convWithDate = { ...newConv, id: numericId, lastTime: toDate(newConv.lastTime) }

        if (existing) {
          // 会话已存在：更新属性（保持引用）
          Object.assign(existing, convWithDate)
          // [关键修复] 显式 set 触发更新
          conversationsMap.value.set(numericId, existing)
        } else {
          // 新会话：添加到 Map
          conversationsMap.value.set(numericId, convWithDate)
        }
      }
    } catch (error) {
      console.error('加载会话列表失败:', error)
    } finally {
      isSessionListLoading.value = false
    }
  }

  async function setActiveRoom(rawRoomId: number | string) {
    const roomId = Number(rawRoomId)
    if (activityTimer.value) {
      clearTimeout(activityTimer.value)
      activityTimer.value = null
    }
    isGracePeriodActive.value = false

    // 检查会话是否存在，不存在则尝试获取
    let conv = conversationsMap.value.get(roomId)
    if (!conv) {
      try {
        const roomVo = await chatService.getRoomDetail(roomId)
        if (roomVo) {
          conv = mapRoomVoToConversation(roomVo)
          conversationsMap.value.set(roomId, conv) // 添加到 Map
        }
      } catch (error) {
        console.error('获取会话详情失败:', error)
        // 如果获取失败，可能无法进入房间，或者需要提示用户
      }
    }

    activeRoomId.value = roomId
    handleUserActivity()

    if (!messageMap.value[roomId] || messageMap.value[roomId].length === 0) {
      if (!roomPagination.value[roomId]) {
        roomPagination.value[roomId] = { nextCursor: null, hasMore: true }
      }
      await loadMoreMessages(roomId)
      lastOpenedLoadedRoomId.value = roomId
      loadedInitialMessages.value[roomId] = true
    } else {
      lastOpenedLoadedRoomId.value = roomId
      loadedInitialMessages.value[roomId] = true
    }
  }

  async function loadMoreMessages(roomId: number) {
    const pagination = roomPagination.value[roomId] ?? { nextCursor: null, hasMore: true }
    if (!pagination.hasMore) return

    try {
      const res: CursorPage<ChatMessageResp> = await chatService.getMessageList({
        roomId,
        cursor: pagination.nextCursor,
        pageSize: HISTORY_PAGE_SIZE,
      })

      roomPagination.value[roomId] = {
        nextCursor: res.nextCursor,
        hasMore: res.hasMore,
      }

      const newMessages = res.records.map((msg) => mapRespToChatMessage(msg, roomId))

      if (!messageMap.value[roomId]) {
        messageMap.value[roomId] = []
      }

      messageMap.value[roomId] = [...newMessages, ...messageMap.value[roomId]]

      lastMessageDirection.value = 'prepend'

      if (!loadedInitialMessages.value[roomId]) {
        loadedInitialMessages.value[roomId] = true
      }
    } catch (error) {
      console.error('加载历史消息失败:', error)
      const old = roomPagination.value[roomId] ?? { nextCursor: null, hasMore: true }
      roomPagination.value[roomId] = { ...old, hasMore: false }
    }
  }

  // mapRoomVoToConversation / mapRespToChatMessage 保持原有实现（略微兼容）
  function mapRoomVoToConversation(room: RoomVo): Conversation {
    let type: MsgType
    if (room.type === 1) {
      type = MsgType.GROUP
    } else if (room.type === 2) {
      type = MsgType.PRIVATE
    } else {
      type = MsgType.GROUP
    }

    return {
      id: Number(room.id), // [关键修复] 强制转为 Number，防止字符串 ID 导致重复
      type,
      targetId: String(room.userId),
      name: room.roomName || '未知会话',
      avatar: room.avatar || 'https://api.dicebear.com/7.x/identicon/svg?seed=' + room.id,
      lastMessage: room.content || '...',
      lastTime: new Date(room.activeTime),
      unreadCount: room.unreadNum ?? 0,
      isOnline: false,
      memberCount: room.memberCount,
    }
  }

  function mapRespToChatMessage(resp: ChatMessageResp, defaultRoomId: number): ChatMessage {
    const roomId = resp.roomId ?? defaultRoomId
    const msgId = resp.message?.id ? String(resp.message.id) : `hist_${roomId}_${Math.random().toString(36).slice(2, 9)}`
    const sendTime = resp.message?.sendTime ? new Date(resp.message.sendTime) : new Date()

    return {
      id: msgId,
      roomId: roomId,
      senderId: String(resp.fromUser?.uid ?? ''),
      senderName: resp.fromUser?.username ?? '匿名用户',
      senderAvatar: resp.fromUser?.avatar ?? 'https://api.dicebear.com/7.x/initials/svg?seed=' + (resp.fromUser?.username ?? 'X'),
      content: resp.message?.content ?? '',
      time: sendTime,
      status: 'sent',
      messageType: resp.message?.type, // [Added] Assuming 'type' is content type
    }
  }

  // 群组详情模态框相关函数
  async function openGroupDetailModal(roomId: number) {
    isGroupDetailOpen.value = true
    isLoadingGroupDetail.value = true
    try {
      const [detail, members] = await Promise.all([
        chatService.getGroupDetail(roomId),
        chatService.getGroupMembers(roomId)
      ])
      currentGroupDetail.value = detail
      currentGroupMembers.value = members
    } catch (error) {
      console.error('获取群组详情失败:', error)
    } finally {
      isLoadingGroupDetail.value = false
    }
  }



  function closeGroupDetailModal() {
    isGroupDetailOpen.value = false
    currentGroupDetail.value = null
    currentGroupMembers.value = []
  }

  function openChatDetailModal() {
    isChatDetailOpen.value = true
  }

  function closeChatDetailModal() {
    isChatDetailOpen.value = false
  }

  async function updateGroupName(roomId: number, newName: string) {
    try {
      await chatService.updateGroupInfo({ roomId, name: newName })
      // 更新本地状态
      if (currentGroupDetail.value) {
        currentGroupDetail.value.name = newName
      }
      // 更新会话列表中的群名
      const conv = conversations.value.find(c => c.id === roomId)
      if (conv) {
        conv.name = newName
      }
    } catch (error) {
      console.error('更新群名失败:', error)
      throw error
    }
  }

  // 联系人相关函数
  async function loadContactList() {
    isLoadingContacts.value = true
    try {
      const res = await chatService.getFriendList()
      contactGroups.value = res
    } catch (error) {
      console.error('加载联系人列表失败:', error)
    } finally {
      isLoadingContacts.value = false
    }
  }

  async function deleteFriend(friendId: string | number) {
    try {
      await chatService.deleteFriend({ friendId })
      // 从联系人列表中移除
      contactGroups.value = contactGroups.value.map(group => ({
        ...group,
        content: group.content.filter(friend => friend.uid !== friendId)
      })).filter(group => group.content.length > 0)
    } catch (error) {
      console.error('删除好友失败:', error)
      throw error
    }
  }

  async function updateFriendRemark(friendId: string | number, remark: string) {
    try {
      await chatService.updateFriendRemark({ friendId, remark })
      // 更新联系人列表中的备注
      contactGroups.value = contactGroups.value.map(group => ({
        ...group,
        content: group.content.map(friend =>
          friend.uid === friendId ? { ...friend, name: remark } : friend
        )
      }))
    } catch (error) {
      console.error('更新好友备注失败:', error)
      throw error
    }
  }

  async function inviteToGroup(roomId: number, userIds: string[]) {
    try {
      await chatService.inviteToGroup(roomId, userIds)
      // Refresh members
      const members = await chatService.getGroupMembers(roomId)
      currentGroupMembers.value = members
      if (currentGroupDetail.value) {
        currentGroupDetail.value.memberCount = members.length
      }
    } catch (error) {
      console.error('邀请进群失败:', error)
      throw error
    }
  }

  async function kickFromGroup(roomId: number, uid: string) {
    try {
      await chatService.kickFromGroup(roomId, uid)
      // Refresh members
      const members = await chatService.getGroupMembers(roomId)
      currentGroupMembers.value = members
      if (currentGroupDetail.value) {
        currentGroupDetail.value.memberCount = members.length
      }
    } catch (error) {
      console.error('移出群成员失败:', error)
      throw error
    }
  }

  // 隐藏会话
  async function hideConversation(roomId: number) {
    try {
      await chatService.hideConversation(roomId)
      // 从 Map 中移除
      conversationsMap.value.delete(roomId)
      if (activeRoomId.value === roomId) {
        activeRoomId.value = null
      }
    } catch (error) {
      console.error('隐藏会话失败:', error)
    }
  }

  // 退出群聊
  async function quitGroup(roomId: number) {
    try {
      await chatService.quitGroup(roomId)
      // 从 Map 中移除
      conversationsMap.value.delete(roomId)
      // 清除消息缓存
      delete messageMap.value[roomId]
      delete roomPagination.value[roomId]

      if (activeRoomId.value === roomId) {
        activeRoomId.value = null
      }
      closeGroupDetailModal()
    } catch (error) {
      console.error('退出群聊失败:', error)
      throw error
    }
  }

  // 删除会话
  async function deleteConversation(roomId: number) {
    try {
      await chatService.deleteConversation(roomId)
      // 从 Map 中移除
      conversationsMap.value.delete(roomId)
      // 清除消息缓存
      delete messageMap.value[roomId]
      delete roomPagination.value[roomId]

      if (activeRoomId.value === roomId) {
        activeRoomId.value = null
      }
    } catch (error) {
      console.error('删除会话失败:', error)
    }
  }

  async function clearHistory(roomId: number) {
    try {
      await chatService.clearHistory(roomId)
      // 清空本地消息
      messageMap.value[roomId] = []
      roomPagination.value[roomId] = { nextCursor: null, hasMore: false }
    } catch (error) {
      console.error('清空聊天记录失败:', error)
      throw error
    }
  }

  // 公开的方法导出（包含 ackMessage 以便 WebSocket 层调用）
  return {
    // state
    conversations,
    activeRoomId,
    activeConversation,
    activeMessages,
    totalUnreadCount,

    roomPagination,
    isSessionListLoading, // 导出加载状态

    // actions
    loadSessionList,
    setActiveRoom,
    loadMoreMessages,
    sendMessage,
    sendCallMessage, // [Added]
    receiveMessage,
    ackMessage, // <-- 新增，需要 WS 层在收到 ACK 时调用

    // group modal
    isGroupDetailOpen,
    currentGroupDetail,
    currentGroupMembers,
    isLoadingGroupDetail,
    openGroupDetailModal,
    closeGroupDetailModal,
    isChatDetailOpen,
    openChatDetailModal,
    closeChatDetailModal,
    updateGroupName,
    deleteFriend,
    clearHistory,

    // contacts
    contactGroups,
    isLoadingContacts,
    loadContactList,
    deleteFriend,
    updateFriendRemark,
    inviteToGroup,
    kickFromGroup,
    hideConversation,
    deleteConversation,
    quitGroup,

    // activity & flags
    handleUserActivity,
    isGracePeriodActive,
    lastMessageDirection,
    loadedInitialMessages,
    lastOpenedLoadedRoomId,
  }
})
