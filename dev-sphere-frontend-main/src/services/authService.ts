import request from '../utils/request'
import type { UserInfo } from '../stores/userStore'

// 登录参数接口 (对应后端的 LoginDTO)
export interface LoginParams {
  username: string;
  password: string;
  uuid: string;    // 新增：获取验证码时使用的唯一标识
  captcha: string; // 新增：用户输入的验证码
}

export interface RegisterParams {
  username: string
  password: string
  // 以下字段在 SysUserDTO 的 DefaultGroup 中被标记为 @NotBlank 或 @NotNull
  // 如果后端开放注册接口没有移除这些校验，前端必须传
  realName?: string
  email?: string
  mobile?: string
  deptId?: number
  gender?: number
  status?: number
}

// 登录返回的 Token 结构
export interface UserTokenDTO {
  userId: number;
  access_token: string;
  refresh_token: string;
  access_token_expire: string;
  refresh_token_expire: string;
}

export const authService = {
  /**
   * 登录接口
   * POST /auth/login
   */
  async login(params: LoginParams): Promise<UserTokenDTO> {
    return request.post<any, UserTokenDTO>('/auth/login', params)
  },

  /**
   * 注册接口
   * POST /user/register (假设你上次改成了这个，如果还是 /user 请自行调整)
   */
  async register(params: { username: string; password: string }): Promise<void> {
    // 组装 SysUserDTO 所需的参数
    // 这里填充了一些默认值，以防后端强校验导致注册失败
    // 实际项目中，这些默认值应该由后端处理，或者在前端增加相应的输入框
    const registerData: RegisterParams = {
      username: params.username,
      password: params.password,
      realName: '新用户' + Math.floor(Math.random() * 1000), // 默认昵称
      email: `user_${Date.now()}@example.com`, // 默认邮箱，规避校验
      mobile: '13800000000', // 默认手机号，规避校验
      deptId: 1, // 默认部门ID，需要根据你系统的实际情况修改！
      gender: 2, // 保密
      status: 1  // 正常启用
    }
    return request.post('/auth/user/save', registerData)
  },

  /**
   * 获取当前登录用户信息
   * GET /user/info
   */
  async getUserInfo(): Promise<UserInfo> {
    return request.get<any, UserInfo>('/auth/user/info')
  },

  /**
   * 退出登录
   * POST /auth/logout
   */
  async logout(): Promise<void> {
    return request.post('/auth/logout')
  }
}