<script setup lang="ts">
import { ref, nextTick, watch, onMounted, onUnmounted } from 'vue'
import { useChatStore, type ChatMessage, MsgType } from '../../stores/chatStore'
import { MessageContentType } from '../../services/chatService'
import { useUserStore } from '../../stores/userStore'
import ChatDetailModal from './ChatDetailModal.vue'
import GroupDetailModal from './GroupDetailModal.vue'

const chatStore = useChatStore()
const userStore = useUserStore()
const messageInput = ref('')
const messagesContainer = ref<HTMLElement | null>(null)

// [V7] 状态：是否正在加载更多历史消息
const isLoadingMore = ref(false)

// [V6] 宽限期活动通知
const handleActivity = () => {
  chatStore.handleUserActivity()
}

// [V7] 滚动到顶部的处理函数（加载历史）
const handleScrollTop = async () => {
  const el = messagesContainer.value
  if (!el) return

  // 如果已经在加载中，直接返回
  if (isLoadingMore.value) return

  // 小于 5px 视为到顶（更稳妥），避免精度问题
  if (el.scrollTop > 5) return

  const currentRoomId = chatStore.activeRoomId
  if (!currentRoomId) return

  // 检查 store 中是否还有更多数据
  const pagination = chatStore.roomPagination[currentRoomId]
  if (pagination && !pagination.hasMore) {
    // 没有更多了
    return
  }

  // 开始加载
  isLoadingMore.value = true

  // 记录加载前的高度
  const oldScrollHeight = el.scrollHeight

  try {
    // 加载历史（store 内会把新消息 prepend 到前面）
    await chatStore.loadMoreMessages(currentRoomId)

    // 等待 DOM 更新（确保消息插入）
    await nextTick()

    // 等到浏览器布局稳定后恢复滚动
    requestAnimationFrame(() => {
      const newScrollHeight = el.scrollHeight
      // 恢复到用户看到的位置（新高度 - 老高度）
      el.scrollTop = newScrollHeight - oldScrollHeight
    })
  } catch (error) {
    console.error('加载更多历史消息失败:', error)
  } finally {
    // 稍微延迟以避免抖动
    setTimeout(() => {
      isLoadingMore.value = false
    }, 120)
  }
}





// Helper: Detect Message Type
// Helper: Detect Message Type
const isImageUrl = (content: string) => {
  if (!content) return false
  // Add support for svg and dicebear api
  return /\.(jpg|jpeg|png|gif|webp|bmp|svg)$/i.test(content) || 
         content.startsWith('blob:') || 
         content.includes('api.dicebear.com')
}

const isFileUrl = (content: string) => {
  if (!content) return false
  // Simple check: if it looks like a URL but not an image
  return (content.startsWith('http') || content.startsWith('/')) && !isImageUrl(content)
}

const getFileName = (url: string) => {
  try {
    return url.substring(url.lastIndexOf('/') + 1)
  } catch {
    return '未知文件'
  }
}

// Chat Image Preview
import ImageViewer from '../ImageViewer.vue'
const showImageViewer = ref(false)
const previewImages = ref<string[]>([])

const previewChatImage = (url: string) => {
  previewImages.value = [url]
  showImageViewer.value = true
}

// ---------- 滚动/加载交互逻辑 ----------

// [V8] 初始加载状态 (用于隐藏滚动过程)
// [V8] 初始加载状态 (用于隐藏滚动过程)
const isInitialLoad = ref(false)

// Pending Attachment State
interface PendingAttachment {
  type: 'image' | 'file'
  url: string
  name?: string
  file?: File
}
const pendingAttachment = ref<PendingAttachment | null>(null)

// ---------- 滚动/加载交互逻辑 ----------

// 1) 监听 activeRoomId
watch(
  () => chatStore.activeRoomId,
  (newId) => {
    if (!newId) return
    
    // 切换房间时，先隐藏内容，避免看到从上往下滚动的过程
    isInitialLoad.value = true

    // Clear pending attachment when switching rooms
    pendingAttachment.value = null

    if (chatStore.lastOpenedLoadedRoomId === newId) {
      scrollToBottom('auto')
      chatStore.lastOpenedLoadedRoomId = null as any
      return
    }
    const stop = watch(
      () => chatStore.lastOpenedLoadedRoomId,
      (val) => {
        if (val === newId) {
          scrollToBottom('auto')
          chatStore.lastOpenedLoadedRoomId = null as any
          stop()
        }
      },
      { immediate: true }
    )
  },
  { immediate: true }
)

// 2) 监听消息长度变化
watch(
  () => chatStore.activeMessages.length,
  async (newLength, oldLength) => {
    if (newLength <= oldLength) return
    if (isLoadingMore.value) return
    if (chatStore.lastMessageDirection === 'append') {
      await scrollToBottom('smooth')
      chatStore.lastMessageDirection = null as any
    }
  }
)
// Upload Logic
import { ossService } from '../../services/ossService'

const imageInput = ref<HTMLInputElement | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)

const triggerImageUpload = () => imageInput.value?.click()
const triggerFileUpload = () => fileInput.value?.click()

const handleImageUpload = async (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  
  try {
    const res = await ossService.upload(file)
    insertHtmlAtCursor(`<img src="${res.url}" class="w-20 h-20 object-cover inline-block align-middle mx-1 rounded select-none" contenteditable="false" />`)
  } catch (error) {
    console.error('Image upload failed:', error)
    alert('图片上传失败')
  } finally {
    if (imageInput.value) imageInput.value.value = ''
  }
}

const handleFileUpload = async (e: Event) => {
  // File upload still sends immediately or we could implement a file card in editor?
  // For now, let's keep file upload as immediate send or maybe just text link?
  // User asked for "mixed text and image", didn't specify file.
  // Let's keep file upload as immediate send for now to avoid complexity of rendering file card in contenteditable.
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return

  try {
    const res = await ossService.upload(file)
    chatStore.sendMessage(res.url, MessageContentType.FILE)
  } catch (error) {
    console.error('File upload failed:', error)
    alert('文件上传失败')
  } finally {
    if (fileInput.value) fileInput.value.value = ''
  }
}

const handlePaste = async (e: ClipboardEvent) => {
  const items = e.clipboardData?.items
  if (!items) return

  for (const item of items) {
    if (item.type.indexOf('image') !== -1) {
      const file = item.getAsFile()
      if (file) {
        e.preventDefault()
        try {
          const res = await ossService.upload(file)
          insertHtmlAtCursor(`<img src="${res.url}" class="w-20 h-20 object-cover inline-block align-middle mx-1 rounded select-none" contenteditable="false" />`)
        } catch (error) {
          console.error('Paste upload failed:', error)
          alert('图片粘贴上传失败')
        }
      }
      return
    }
  }
}
// Call Logic
import webRTCService from '../../services/WebRTCService'

// Emoji & Sticker Logic
const showEmojiPicker = ref(false)
const activeEmojiTab = ref<'emoji' | 'sticker'>('emoji')

const emojiList = [
  '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂', '🙂', '🙃',
  '😉', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗', '😚', '😙',
  '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔',
  '🤐', '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '🤥',
  '😌', '😔', '😪', '🤤', '😴', '😷', '🤒', '🤕', '🤢', '🤮',
  '🤧', '🥵', '🥶', '🥴', '😵', '🤯', '🤠', '🥳', '😎', '🤓',
  '🧐', '😕', '😟', '🙁', '😮', '😯', '😲', '😳', '🥺', '😦',
  '😧', '😨', '😰', '😥', '😢', '😭', '😱', '😖', '😣', '😞',
  '😓', '😩', '😫', '🥱', '😤', '😡', '😠', '🤬', '😈', '👿',
  '💀', '☠', '💩', '🤡', '👹', '👺', '👻', '👽', '👾', '🤖',
  '👍', '👎', '👌', '✌', '🤞', '🤟', '🤘', '🤙', '👈', '👉',
  '👆', '👇', '✋', '🤚', '🖐', '🖖', '👋', '🤙', '💪', '🖕',
  '✍', '🙏', '💍', '💄', '💋', '👄', '👅', '👂', '👃', '👣',
  '👁', '👀', '🧠', '🦴', '🦷', '🗣', '👤', '👥', '🫂', '👶',
  '❤', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔',
  '❣', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '💟', '☮',
]

const stickerList = [
  'https://api.dicebear.com/7.x/fun-emoji/svg?seed=Felix',
  'https://api.dicebear.com/7.x/fun-emoji/svg?seed=Aneka',
  'https://api.dicebear.com/7.x/fun-emoji/svg?seed=Bella',
  'https://api.dicebear.com/7.x/fun-emoji/svg?seed=Caleb',
  'https://api.dicebear.com/7.x/fun-emoji/svg?seed=Dylan',
  'https://api.dicebear.com/7.x/fun-emoji/svg?seed=Eliza',
  'https://api.dicebear.com/7.x/fun-emoji/svg?seed=Fiona',
  'https://api.dicebear.com/7.x/fun-emoji/svg?seed=George',
  'https://api.dicebear.com/7.x/fun-emoji/svg?seed=Hannah',
]
// 发送消息 (Rich Text Handler)
const editorRef = ref<HTMLElement | null>(null)

// Helper: Insert HTML at cursor
const insertHtmlAtCursor = (html: string) => {
  const sel = window.getSelection()
  if (sel && sel.rangeCount > 0) {
    const range = sel.getRangeAt(0)
    // Ensure the range is within our editor
    if (editorRef.value && editorRef.value.contains(range.commonAncestorContainer)) {
      range.deleteContents()
      const el = document.createElement('div')
      el.innerHTML = html
      const frag = document.createDocumentFragment()
      let node: Node | null
      let lastNode: Node | null = null
      while ((node = el.firstChild)) {
        lastNode = frag.appendChild(node)
      }
      range.insertNode(frag)
      if (lastNode) {
        range.setStartAfter(lastNode)
        range.collapse(true)
        sel.removeAllRanges()
        sel.addRange(range)
      }
      return
    }
  }
  // Fallback: Append to end
  if (editorRef.value) {
    editorRef.value.innerHTML += html
    // Move cursor to end
    const range = document.createRange()
    range.selectNodeContents(editorRef.value)
    range.collapse(false)
    const sel = window.getSelection()
    sel?.removeAllRanges()
    sel?.addRange(range)
  }
}

const handleSend = async (e?: KeyboardEvent) => {
  if (e && e.shiftKey) return // Allow Shift+Enter for new line
  if (e) e.preventDefault() // Prevent default div newline behavior on Enter

  if (!editorRef.value) return
  const nodes = editorRef.value.childNodes
  if (nodes.length === 0) return

  // Parse and send sequence
  let currentText = ''
  
  const sendText = async () => {
    if (currentText.trim()) {
      await chatStore.sendMessage(currentText)
      currentText = ''
    } else {
      currentText = '' // Clear whitespace
    }
  }

  for (const node of Array.from(nodes)) {
    if (node.nodeType === Node.TEXT_NODE) {
      currentText += node.textContent
    } else if (node.nodeType === Node.ELEMENT_NODE) {
      const el = node as HTMLElement
      if (el.tagName === 'IMG') {
        // Send accumulated text first
        await sendText()
        // Send Image
        const src = (el as HTMLImageElement).getAttribute('src')
        if (src) {
          await chatStore.sendMessage(src, MessageContentType.IMAGE)
        }
      } else if (el.tagName === 'BR') {
        currentText += '\n'
      } else {
        // Handle other elements (like divs from copy-paste) as text
        currentText += el.textContent
      }
    }
  }
  // Send remaining text
  await sendText()

  // Clear editor
  editorRef.value.innerHTML = ''
  scrollToBottom('auto')
}

const handleMenuClick = () => {
  if (!chatStore.activeConversation) return
  if (chatStore.activeConversation.type === MsgType.GROUP) {
    chatStore.openGroupDetailModal(chatStore.activeConversation.id)
  } else {
    chatStore.openChatDetailModal()
  }
}

// 滚动到底部
const scrollToBottom = async (behavior: 'smooth' | 'auto' = 'smooth') => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTo({
      top: messagesContainer.value.scrollHeight,
      behavior,
    })
    
    // 如果是初始加载（auto），滚动完成后显示内容
    if (behavior === 'auto' && isInitialLoad.value) {
      // 稍微延迟一帧确保渲染完成
      requestAnimationFrame(() => {
        isInitialLoad.value = false
      })
    }
  }
}

// 判断是否需要显示时间戳（5 分钟间隔）
const shouldShowTime = (currentMsg: ChatMessage, index: number) => {
  if (index === 0) return true
  const prevMsg = chatStore.activeMessages[index - 1]
  if (!prevMsg || !prevMsg.time || !currentMsg.time) return true
  
  const currTime = currentMsg.time instanceof Date ? currentMsg.time.getTime() : new Date(currentMsg.time).getTime()
  const prevTime = prevMsg.time instanceof Date ? prevMsg.time.getTime() : new Date(prevMsg.time).getTime()
  
  return currTime - prevTime > 5 * 60 * 1000 // 5 分钟
}

// 格式化时间戳，健壮处理无效日期
const formatMessageTime = (date: Date | string | null) => {
  const d = date ? (date instanceof Date ? date : new Date(date)) : new Date()
  if (isNaN(d.getTime())) return ''
  const now = new Date()
  if (d.toDateString() === now.toDateString()) {
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }
  return d.toLocaleString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

// 辅助：消息 key 生成（保证唯一且稳定）
const messageKey = (msg: ChatMessage, index: number) => {
  if (msg.id) return String(msg.id)
  if (msg.tempId) return msg.tempId
  return `idx_${index}`
}

// Emoji & Sticker Logic


const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

const insertEmoji = (emoji: string) => {
  insertHtmlAtCursor(emoji)
}

const sendSticker = (url: string) => {
  insertHtmlAtCursor(`<img src="${url}" class="w-10 h-10 inline-block align-middle mx-1 select-none" contenteditable="false" />`)
  showEmojiPicker.value = false
}

// Close picker when clicking outside
const closePicker = () => {
  showEmojiPicker.value = false
}

onMounted(() => {
  document.addEventListener('click', closePicker)
})

onUnmounted(() => {
  document.removeEventListener('click', closePicker)
})

const handleVoiceCall = () => {
  const conv = chatStore.activeConversation
  if (!conv || conv.type !== MsgType.PRIVATE) return
  
  // Start call
  webRTCService.startCall(conv.targetId, {
    id: conv.targetId,
    name: conv.name,
    avatar: conv.avatar
  }, 'audio')
}

const handleVideoCall = () => {
  const conv = chatStore.activeConversation
  if (!conv || conv.type !== MsgType.PRIVATE) return
  
  // Start call
  webRTCService.startCall(conv.targetId, {
    id: conv.targetId,
    name: conv.name,
    avatar: conv.avatar
  }, 'video')
}
