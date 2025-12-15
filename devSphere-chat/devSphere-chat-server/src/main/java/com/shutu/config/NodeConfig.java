package com.shutu.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

/**
 * 节点配置与常量定义类
 * 负责生成当前服务节点的唯一标识，用于分布式路由场景下的节点识别
 */
@Data
@Slf4j
@Component
public class NodeConfig {

    @Value("${devsphere.server.node-id}")
    private String nodeId;

    // Redis Key 模式定义
    public static final String KEY_USER_LOCATION = "im:location:"; // + uid
    public static final String TOPIC_NODE_ROUTE_PREFIX = "im:route:to:"; // + nodeId

    /**
     * 初始化该服务节点的标识
     */
    @PostConstruct
    public void init() {
        if (nodeId == null || nodeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Node ID 必须在配置文件中指定：devsphere.server.node-id");
        }
       log.info(">>> [IM Node Init] 当前 IM 节点 ID: " + this.nodeId);
    }
}
