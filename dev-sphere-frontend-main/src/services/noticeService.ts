import request from '../utils/request'
import type { UserInfo } from '../stores/userStore' // 假设 UserInfo 导入路径

// === 1. DTO/VO 类型定义 (对应后端) ===

// 对应后端 FriendAddRequest
export interface FriendAddRequest {
  userId: number;  // 要添加的好友ID
  remark: string;  // 备注/验证信息
}

// 对应后端 MessageNoticeUpdateRequest
export interface NoticeHandleRequest {
  id: number; // 通知ID
  noticeType: number; // (新增) 必须传入通知类型
  processResult: number; // 处理结果：1=同意, 2=拒绝 (对应 ProcessResultTypeEnum)
}

// 对应后端 NoticeMessageVo
export interface NoticeMessageVo {
  id: number;
  userId: number; // 发送申请的人
  toUserId: number; // 接收申请的人 (即我)
  noticeType: number; // 1=系统, 2=群聊, 3=好友
  noticeContent: string; // 申请备注
  readTarget: number; // 0=未读, 1=已读
  processResult: string; // "已同意", "已拒绝", 或 null/空 (后端这里存的是 desc，不是 code)
  createTime: string; // 日期字符串
  
  // VO 额外填充的字段
  avatar: string;
  name: string; // 发送者名称
  title: string; // 组装好的标题
}

// 对应后端 MessageNumVo
export interface MessageNumVo {
  noticeNum: number;
}

// === 2. API Service ===

export const noticeService = {
  /**
   * 发送添加好友请求
   * POST /notice/add/friend
   */
  async addFriend(params: FriendAddRequest): Promise<boolean> {
    return request.post('/devSphere/notice/add/friend', params)
  },

  /**
   * 获取消息数量
   * GET /notice/messageNum
   */
  async getMessageNum(): Promise<MessageNumVo> {
    return request.get('/devSphere/notice/messageNum')
  },

  /**
   * 获取消息通知列表
   * GET /notice/messageNotice/list
   */
  async getNoticeList(): Promise<NoticeMessageVo[]> {
    return request.get('/devSphere/notice/messageNotice/list')
  },

  /**
   * 消息已读
   * GET /notice/messageNotice/read
   */
  async readNotice(id: number): Promise<boolean> {
    return request.get('/devSphere/notice/messageNotice/read', { params: { id } })
  },

  /**
   * 处理通知 (同意/拒绝)
   * POST /notice/messageNotice/handle
   */
  async handleNotice(params: NoticeHandleRequest): Promise<string> {
    return request.post('/devSphere/notice/messageNotice/handle', params)
  }
}