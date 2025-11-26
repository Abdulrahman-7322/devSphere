// src/utils/config.ts

// 你的后端网关地址 (用于 HTTP 和 SSE)
// const API_BASE_URL = 'http://10.102.228.17:8081'
const API_BASE_URL = 'http://192.168.2.115:8081'

// 你的 Netty 服务器地址 (用于 WebSocket)
// const WS_BASE_URL = 'ws://10.102.228.17:9000'
const WS_BASE_URL = 'ws://192.168.2.115:9000'

export default {
  // Axios 使用的 API 基础路径 (如果你还想保留 Axios 代理，可以单独配置)
  API_BASE_PATH: '/api',

  // HTTP/SSE 绝对路径
  API_ABSOLUTE_URL: API_BASE_URL,

  // WebSocket 绝对路径
  WEBSOCKET_URL: `${WS_BASE_URL}/ws`,

  // SSE 连接绝对路径
  SSE_URL: `${API_BASE_URL}/devSphere/notice/userConnect`
}