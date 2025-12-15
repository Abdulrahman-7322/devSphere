package com.shutu.model.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 跨节点路由消息传输对象
 * 用于在 Redis Pub/Sub 中传输需要路由到其他节点的消息内容
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteMessageDTO implements Serializable {

    /**
     * 目标用户 ID
     */
    private Long targetUid;

    /**
     * 要推送的消息内容 (JSON 格式的 WSBaseResp)
     */
    private String messageJson;
}
