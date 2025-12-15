package com.shutu.service;

/**
 * 用户位置服务接口
 * 负责在 Redis 中维护 UserID 到 NodeID 的映射关系，用于定位用户所在的服务器节点
 */
public interface UserLocationService {

    /**
     * 注册用户位置（上线/心跳）
     * 
     * @param uid 用户ID
     */
    void register(Long uid);

    /**
     * 移除用户位置（下线）
     * 
     * @param uid 用户ID
     */
    void remove(Long uid);

    /**
     * 获取用户所在的节点 ID
     * 
     * @param uid 用户ID
     * @return 节点ID (IP:Port)
     */
    String getNode(Long uid);

    /**
     * 判断是否是本机
     * 
     * @param targetNodeId 目标节点ID
     * @return true if local
     */
    boolean isLocal(String targetNodeId);
}
