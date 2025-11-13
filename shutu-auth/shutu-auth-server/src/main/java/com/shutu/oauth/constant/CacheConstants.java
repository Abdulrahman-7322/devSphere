package com.shutu.oauth.constant;

public final class CacheConstants {

    /**
     * OAuth2.0 state 在 Redis 中的存储Key前缀
     * 完整 Key 的格式: oauth:state:{state}
     */
    public static final String OAUTH_STATE_KEY_PREFIX = "oauth:state:";

    /**
     * 用户登录分布式锁在 Redis 中的存储Key前缀
     * 完整 Key 的格式: lock:user:login:{platform}:{uniqueId}
     */
    public static final String LOGIN_USER_LOCK_KEY_PREFIX = "lock:user:login:";

    /**
     * Ticket 换取 Token 的 Redis Key 前缀
     * 完整 Key 的格式: oauth:ticket:{ticket}
     */
    public static final String OAUTH_TICKET_KEY_PREFIX = "oauth:ticket:";

    private CacheConstants() {}
}