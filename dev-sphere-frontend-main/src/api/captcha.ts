// src/api/captcha.ts
import request from '../utils/request'

// 获取验证码接口
export function captchaGet(data: any) {
  return request({
    url: '/auth/captcha/get', 
    method: 'post',
    data
  })
}

// 校验验证码接口
export function captchaCheck(data: any) {
  return request({
    url: '/auth/captcha/check', 
    method: 'post',
    data
  })
}