import { ref } from 'vue'
import { useChatStore } from '../stores/chatStore'

// === 对接后端枚举 ===
export const WSReqType = {
  LOGIN: 1,
  CHAT: 2,
  AUTHORIZE: 3,
  HEARTBEAT: 4,
  RTC_SIGNAL: 10,
} as const

export type WSReqType = typeof WSReqType[keyof typeof WSReqType]

export interface WSBaseReq {
  type: WSReqType
  userId?: string
  data?: string
}

export interface WSBaseResp<T = any> {
  type: WSReqType
  data: T
}

export const WSStatus = {
  CONNECTING: 0,
  OPEN: 1,
  CLOSING: 2,
  CLOSED: 3,
} as const

export type WSStatusType = typeof WSStatus[keyof typeof WSStatus]

class WebSocketService {
  private ws: WebSocket | null = null
  private url: string = ''
  private token: string = ''
  private pingInterval: any = null
  private reconnectTimeout: any = null
  public status = ref<WSStatusType>(WSStatus.CLOSED)

  // Handler for RTC signals (to avoid circular dependency)
  private rtcHandler: ((data: any) => void) | null = null

  public registerRtcHandler(handler: (data: any) => void) {
    this.rtcHandler = handler
  }

  connect(url: string, token: string) {
    this.url = url
    this.token = token
    if (this.ws) this.close()
    this.status.value = WSStatus.CONNECTING

    try {
      // 使用 URL 参数传递 Token
      // 使用 URL 参数传递 Token
      let wsUrl = url
      if (url.startsWith('/')) {
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
        wsUrl = `${protocol}//${window.location.host}${url}`
      }
      wsUrl = `${wsUrl}?accessToken=${encodeURIComponent(token)}`
      console.log('[WS] 准备连接真实地址:', wsUrl)
      this.ws = new WebSocket(wsUrl)

      this.initListeners()
    } catch (e) {
      console.error('[WS] 连接创建失败:', e)
      this.reconnect()
    }
  }

  private initListeners() {
    if (!this.ws) return

    this.ws.onopen = () => {
      console.log('[WS] 连接成功！')
      this.status.value = WSStatus.OPEN
      // 30s 后端超时，我们 10s 发一次心跳足够了
      this.startHeartbeat(10000)
    }

    this.ws.onmessage = (event) => {
      try {
        // 心跳响应可能只是简单字符串，需要兼容判断
        if (event.data === 'PONG') return

        const resp: WSBaseResp = JSON.parse(event.data)
        this.handleMessage(resp)
      } catch (e) {
        // console.warn('[WS] 非标准JSON消息:', event.data)
      }
    }

    this.ws.onclose = (e) => {
      console.log(`[WS] 连接关闭 (Code: ${e.code}, Reason: ${e.reason})`)
      this.status.value = WSStatus.CLOSED
      this.stopHeartbeat()
      if (e.code !== 1000) this.reconnect()
    }

    this.ws.onerror = (e) => {
      console.error('[WS] 连接错误', e)
      this.status.value = WSStatus.CLOSED
    }
  }

  // 统一消息处理分发
  private handleMessage(resp: WSBaseResp) {
    const chatStore = useChatStore()
    switch (resp.type) {
      case WSReqType.CHAT:
        // 交给 Pinia Store 处理具体的聊天消息
        chatStore.receiveMessage(resp.data)
        break
      case WSReqType.HEARTBEAT:
        // console.debug('[WS] 心跳响应')
        break
      case WSReqType.RTC_SIGNAL:
        // Handle RTC Signal
        let signalData = resp.data
        if (typeof signalData === 'string') {
          try {
            signalData = JSON.parse(signalData)
          } catch (e) {
            console.error('Failed to parse RTC signal', e)
            return
          }
        }
        if (this.rtcHandler) {
          this.rtcHandler(signalData)
        }
        break
    }
  }

  // 发送消息 (核心方法)
  send(req: WSBaseReq) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(req))
    } else {
      console.warn('[WS] 发送失败，连接未就绪', {
        wsExists: !!this.ws,
        readyState: this.ws?.readyState,
        status: this.status.value
      })
    }
  }

  // 心跳
  private startHeartbeat(interval: number) {
    this.stopHeartbeat()
    this.pingInterval = setInterval(() => {
      // 发送心跳包，类型为 4
      this.send({ type: WSReqType.HEARTBEAT })
    }, interval)
  }

  private stopHeartbeat() {
    if (this.pingInterval) {
      clearInterval(this.pingInterval)
      this.pingInterval = null
    }
  }

  // 断线重连
  private reconnect() {
    this.stopHeartbeat()
    if (this.reconnectTimeout) return
    // 5秒后重连
    this.reconnectTimeout = setTimeout(() => {
      this.reconnectTimeout = null
      if (this.url && this.token) {
        console.log('[WS] 尝试重连...')
        this.connect(this.url, this.token)
      }
    }, 5000)
  }

  public ensureConnected() {
    if (this.status.value !== WSStatus.OPEN && this.url && this.token) {
      console.log('[WS] ensureConnected triggers reconnect')
      this.connect(this.url, this.token)
    }
  }

  // 主动关闭
  close() {
    this.stopHeartbeat()
    clearTimeout(this.reconnectTimeout)
    this.url = ''
    this.token = ''
    if (this.ws) {
      this.ws.close(1000) // 正常关闭
      this.ws = null
    }
  }
}

export default new WebSocketService()