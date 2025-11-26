import request from '../utils/request'

export interface UserUpdateDTO {
    id?: string | number
    username?: string
    realName?: string
    headUrl?: string
    gender?: number
    mobile?: string
    email?: string
}

export const userService = {
    /**
     * 修改用户信息
     * PUT /auth/user
     */
    async updateUserInfo(dto: UserUpdateDTO): Promise<void> {
        return request.put('/devSphere/auth/user', dto)
    }
}
