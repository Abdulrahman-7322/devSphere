package com.shutu.commons.security.utils;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Token 工具类
 */
public class TokenUtils {

    /**
     * 生成 AccessToken
     */
    public static String generator() {
        return UUID.fastUUID().toString(true);
    }

    /**
     * 获取 AccessToken (已修改)
     * * 兼容三种方式获取：
     * 1. Header: Authorization: <token>
     * 2. URL Param: ?access_token=<token>
     * 3. URL Param: ?accessToken=<token> (用于 SSE 和 WebSocket)
     */
    public static String getAccessToken(HttpServletRequest request) {
        // 1. 优先从 Header 获取
        String accessToken = request.getHeader("Authorization");

        // (可选) 如果你使用了 "Bearer " 前缀，在这里去除
        if (StrUtil.isNotBlank(accessToken) && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7); // "Bearer ".length() == 7
        }

        // 2. 如果 Header 为空，尝试从 URL 参数 "access_token" (下划线) 获取
        if (StrUtil.isBlank(accessToken)) {
            accessToken = request.getParameter("access_token");
        }

        // 3. 如果 "access_token" 也为空，尝试从 "accessToken" (驼峰) 获取
        if (StrUtil.isBlank(accessToken)) {
            accessToken = request.getParameter("accessToken");
        }

        return accessToken;
    }
}