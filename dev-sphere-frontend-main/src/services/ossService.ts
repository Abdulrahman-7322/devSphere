import request from '../utils/request'

export interface UploadDTO {
    url: string
    size: number
}

export const ossService = {
    /**
     * 上传文件
import request from '../utils/request'

export interface UploadDTO {
    url: string
    size: number
}

export const ossService = {
    /**
     * 上传文件
     * POST /devSphere/file/upload
     */
    async upload(file: File): Promise<UploadDTO> {
        const formData = new FormData()
        formData.append('file', file)

        // 使用 OSS 模块上传接口
        const res = await request.post('/oss/file/upload', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        })

        return {
            url: (res as any).url,
            size: (res as any).size
        }
    }
}
