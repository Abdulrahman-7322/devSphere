import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useUserStore } from './userStore'
import { momentService, type Moment, type UserVo, type CommentVo } from '../services/momentService'

export const useMomentStore = defineStore('moment', () => {
    const moments = ref<Moment[]>([])
    const isLoading = ref(false)
    const page = ref(1)
    const hasMore = ref(true)

    // 获取朋友圈列表
    const fetchMoments = async (isRefresh = false) => {
        if (isRefresh) {
            page.value = 1
            hasMore.value = true
            moments.value = []
        }
        if (!hasMore.value || (isLoading.value && !isRefresh)) return

        isLoading.value = true
        try {
            const list = await momentService.getMomentList({
                page: page.value,
                size: 10
            })

            console.log('📦 原始数据:', list)

            if (list.length < 10) {
                hasMore.value = false
            }
            // 转换 ID 为 string (如果后端返回的是 number)
            const formattedList = list.map((item: any) => ({
                ...item,
                id: String(item.id),
                userId: String(item.userId),
                user: { ...item.user, userId: String(item.user.userId) },
                likes: (item.likes || []).map((l: any) => ({ ...l, userId: String(l.userId) })),
                comments: (item.comments || []).map((c: any) => ({
                    ...c,
                    id: String(c.id),
                    postId: String(c.postId),
                    userId: String(c.userId),
                    replyToCommentId: c.replyToCommentId ? String(c.replyToCommentId) : undefined
                }))
            }))

            console.log('✅ 格式化后的数据:', formattedList)

            if (isRefresh) {
                moments.value = formattedList
            } else {
                moments.value.push(...formattedList)
            }

            console.log('📊 moments.value:', moments.value)
            console.log('📊 moments.value.length:', moments.value.length)

            page.value++
        } catch (error) {
            console.error('❌ Failed to fetch moments:', error)
        } finally {
            isLoading.value = false
        }
    }

    // 点赞
    const likeMoment = async (momentId: string) => {
        const moment = moments.value.find(m => m.id === momentId)
        if (!moment || moment.isLiked) return

        // 乐观更新
        moment.isLiked = true
        moment.likeCount++
        const userStore = useUserStore()
        const currentUser: UserVo = {
            userId: String(userStore.userInfo!.id),
            username: userStore.userInfo!.username,
            avatar: userStore.userAvatar // 使用 computed 属性
        }
        moment.likes.unshift(currentUser)

        try {
            await momentService.likeMoment(momentId)
        } catch (error) {
            // 回滚
            moment.isLiked = false
            moment.likeCount--
            moment.likes.shift()
            console.error('Like failed:', error)
        }
    }

    // 取消点赞
    const unlikeMoment = async (momentId: string) => {
        const moment = moments.value.find(m => m.id === momentId)
        if (!moment || !moment.isLiked) return

        // 乐观更新
        moment.isLiked = false
        moment.likeCount--
        const userStore = useUserStore()
        const idx = moment.likes.findIndex(u => u.userId === String(userStore.userInfo!.id))
        if (idx > -1) moment.likes.splice(idx, 1)

        try {
            await momentService.unlikeMoment(momentId)
        } catch (error) {
            // 回滚
            moment.isLiked = true
            moment.likeCount++
            console.error('Unlike failed:', error)
        }
    }

    // 评论
    const addComment = async (momentId: string, content: string, replyTo?: string) => {
        try {
            await momentService.addComment(momentId, { content, replyTo })
            // 重新获取该条动态详情或者手动构建评论对象插入
            const moment = moments.value.find(m => m.id === momentId)
            if (moment) {
                const userStore = useUserStore()
                const newComment: CommentVo = {
                    id: String(Date.now()), // 临时ID
                    postId: momentId,
                    userId: String(userStore.userInfo!.id),
                    username: userStore.userInfo!.username,
                    avatar: userStore.userAvatar,
                    content,
                    createdAt: new Date().toISOString(),
                    replyToCommentId: replyTo
                }
                moment.comments.push(newComment)
                moment.commentCount++
            }
        } catch (error) {
            console.error('Comment failed:', error)
        }
    }

    // 删除动态
    const deleteMoment = async (momentId: string) => {
        try {
            await momentService.deleteMoment(momentId)
            const idx = moments.value.findIndex(m => m.id === momentId)
            if (idx > -1) moments.value.splice(idx, 1)
        } catch (error) {
            console.error('Delete failed:', error)
        }
    }

    return {
        moments,
        isLoading,
        hasMore,
        fetchMoments,
        likeMoment,
        unlikeMoment,
        addComment,
        deleteMoment
    }
})
