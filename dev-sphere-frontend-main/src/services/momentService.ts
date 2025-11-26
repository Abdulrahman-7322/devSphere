import request from '../utils/request'

// === DTO/VO Definitions ===

export interface UserVo {
    userId: string  // 修改: uid -> userId
    username: string
    avatar: string
}

export interface CommentVo {
    id: string
    postId: string
    userId: string
    username: string
    avatar: string
    content: string
    replyToCommentId?: string  // 修改: replyToUserId -> replyToCommentId
    replyToUsername?: string
    createdAt: string  // 修改: createTime -> createdAt
}

export interface Moment {
    id: string
    userId: string
    content: string
    imageUrls: string[]
    likeCount: number
    commentCount: number
    createdAt: string
    user: UserVo
    likes: UserVo[]
    comments: CommentVo[]
    isLiked: boolean
}

export interface MomentListParams {
    page: number
    size: number
}

export interface CommentParams {
    content: string
    replyTo?: string
}

export const momentService = {
    /**
     * 获取朋友圈列表
     * GET /api/moments/list
     */
    async getMomentList(params: MomentListParams): Promise<Moment[]> {
        const res = await request.get('/devSphere/api/moments/list', { params })
        // request拦截器已经返回了res.data,所以这里直接返回res
        return (res as any) || []
    },

    /**
     * 点赞
     * POST /api/moments/{momentId}/like
     */
    async likeMoment(momentId: string): Promise<void> {
        return request.post(`/devSphere/api/moments/${momentId}/like`)
    },

    /**
     * 取消点赞
     * POST /api/moments/{momentId}/unlike
     */
    async unlikeMoment(momentId: string): Promise<void> {
        return request.post(`/devSphere/api/moments/${momentId}/unlike`)
    },

    /**
     * 评论
     * POST /api/moments/{momentId}/comment
     */
    async addComment(momentId: string, params: CommentParams): Promise<void> {
        return request.post(`/devSphere/api/moments/${momentId}/comment`, null, {
            params
        })
    },

    /**
     * 删除动态
     * DELETE /api/moments/{momentId}
     */
    async deleteMoment(momentId: string): Promise<void> {
        return request.delete(`/devSphere/api/moments/${momentId}`)
    },

    /**
     * 发布动态
     * POST /api/moments/create
     */
    async publishMoment(data: { content: string; imageUrls: string[]; visibility?: number }): Promise<void> {
        return request.post('/devSphere/api/moments/create', data)
    },
}
