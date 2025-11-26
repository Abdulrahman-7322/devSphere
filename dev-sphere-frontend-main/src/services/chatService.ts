import request from '../utils/request'

// === 通用分页响应结构 ===
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface CursorPage<T> {
  records: T[]
  nextCursor: string | null
  hasMore: boolean
}

export interface MessageQueryReq {
  roomId: number;
  cursor?: string | null;
  pageSize?: number;
}

// === 1. 房间 (会话) 相关定义 ===
export interface RoomQueryReq {
  page: number
  limit: number
  roomId?: number // number 也可以，看你后端定义
}

// 对应后端 RoomVo
export interface RoomVo {
  id: number // 房间ID通常不大，可以是 number
  type: number
  activeTime: string
  content: string
  roomName: string
  avatar: string
  userId: string
  unreadNum: number
  memberCount?: number
}


export interface ChatMessageResp {
  fromUser: {
    uid: string
    username: string
    avatar: string
  }
  message: {
    id: string // 消息ID
    sendTime: string
    content: string
    type: number
  }
  roomId: number
  tempId?: string
}

// === 3. 搜索好友/群聊 ===
export interface SearchRequest {
  query: string
}

export interface AddFriendVo {
  roomId: number
  uid: string
  avatar: string
  name: string
  friendTarget: number
  type: number
}

// === 4. 好友列表 ===
export interface FriendVo {
  uid: string
  name: string
  avatar: string
  roomId: number
}

export interface FriendContentVo {
  type: number
  typeName: string
  content: FriendVo[]
}

// === 5. 创建群聊 ===
export interface CreateGroupRequest {
  name: string
  userIds: (number | string)[] // 核心修复
}

// === 群聊详情 ===
export interface GroupDetailVo {
  roomId: number;
  name: string;
  avatar: string;
  ownerId: string | number; // 核心修复
  memberCount: number;
}

export interface GroupMemberVo {
  uid: string | number; // 核心修复
  username: string;
  avatar: string;
  isOwner: number;
}

export interface GroupUpdateDto {
  roomId: number;
  name: string;
}

// 新增好友备注更新 DTO 接口
export interface FriendRemarkUpdateDTO {
  friendId: string | number;
  remark: string;
}

// 新增删除好友 DTO 接口
export interface FriendDeleteDTO {
  friendId: string | number;
}

export const chatService = {
  /**
   * 获取会话列表
   */
  async getRoomList(params: RoomQueryReq): Promise<PageResult<RoomVo>> {
    return request.post('/devSphere/chat/list/page/vo', params)
  },

  /**
   * 获取指定房间的历史消息
   */
  async getMessageList(params: MessageQueryReq): Promise<CursorPage<ChatMessageResp>> {
    return request.post('/devSphere/chat/message/page/vo', params)
  },

  /**
   * 获取单个房间详情 (用于从联系人列表发起会话时，如果会话列表不存在则获取)
   * GET /chat/room/detail
   */
  async getRoomDetail(roomId: number): Promise<RoomVo> {
    return request.get('/devSphere/chat/room/detail', { params: { roomId } })
  },



  /**
   * 搜索好友或群聊 (按用户名或群ID)
   * POST /chat/search/addable
   */
  async searchForAdd(params: SearchRequest): Promise<AddFriendVo> {
    return request.post('/devSphere/chat/search/addable', params)
  },

  /**
   * 获取好友列表 (新增)
   */
  async getFriendList(): Promise<FriendContentVo[]> {
    return request.post('/devSphere/chat/friend/list/vo')
  },

  /**
   * 创建群聊 
   */
  async createGroup(params: CreateGroupRequest): Promise<{ roomId: number }> {
    console.log('向后端发起创建群聊请求:', params)
    // 模拟后端接口
    return request.post('/devSphere/chat/group/create', params)
  },

  /**
   * 获取群聊详情
   * GET /chat/group/detail
   */
  async getGroupDetail(roomId: number): Promise<GroupDetailVo> {
    return request.get('/devSphere/group/detail', { params: { roomId } })
  },

  /**
   * 获取群成员
   * GET /chat/group/members
   */
  async getGroupMembers(roomId: number): Promise<GroupMemberVo[]> {
    return request.get('/devSphere/group/members', { params: { roomId } })
  },

  /**
   * 更新群信息
   * PUT /chat/group/update
   */
  async updateGroupInfo(dto: GroupUpdateDto): Promise<void> {
    return request.put('/devSphere/group/update', dto)
  },

  /**
   *  修改好友备注
   * PUT /friend/remark
   */
  async updateFriendRemark(dto: FriendRemarkUpdateDTO): Promise<void> {
    return request.put('/devSphere/friend/remark', dto)
  },

  /**
   * 删除好友
   * DELETE /friend/delete
   */
  async deleteFriend(dto: FriendDeleteDTO): Promise<void> {
    // DELETE 请求通常用 data (等同于 @RequestBody)
    return request.delete('/devSphere/friend/delete', { data: dto })
  },

  /**
   * 隐藏会话
   * PUT /chat/hide
   */
  async hideConversation(roomId: number): Promise<void> {
    return request.put('/devSphere/chat/hide', null, {
      params: { roomId }
    })
  },

  /**
   * 删除会话
   * DELETE /chat/conversation
   */
  async deleteConversation(roomId: number): Promise<void> {
    return request.delete('/devSphere/chat/conversation', {
      params: { roomId }
    })
  },

  /**
   * 标记消息为已读
   */
  async markAsRead(roomId: number): Promise<void> {
    return request.post('/devSphere/chat/read', null, {
      params: { roomId }
    })
  },

  // 退出群聊
  quitGroup(roomId: number) {
    return request.post('/devSphere/group/quit', null, {
      params: { roomId }
    })
  },

  /**
   * 清空聊天记录
   */
  async clearHistory(roomId: number): Promise<void> {
    return request.post('/devSphere/chat/history/clear', null, {
      params: { roomId }
    })
  },

  /**
   * 搜索聊天记录
   */
  async searchHistory(roomId: number, keyword: string): Promise<ChatMessageResp[]> {
    const res = await request.get('/devSphere/chat/history/search', {
      params: { roomId, keyword }
    })
    return (res as any) || []
  },

  /**
   * 撤回消息
   */
  async recallMessage(messageId: number): Promise<void> {
    return request.post('/devSphere/chat/message/recall', null, {
      params: { messageId }
    })
  }
}
