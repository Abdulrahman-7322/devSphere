package com.shutu.common.listener;

import cn.hutool.json.JSONUtil;
import com.shutu.model.dto.chat.RouteMessageDTO;
import com.shutu.model.vo.ws.response.WSBaseResp;
import com.shutu.websocket.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * 跨节点路由消息监听器
 * 负责接收 Redis Pub/Sub 广播过来的路由消息，并将消息推送到本机连接的目标用户
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RouteMessageListener implements MessageListener {

    private final WebSocketService webSocketService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String body = new String(message.getBody());

        log.debug("收到路由消息: channel={}, body={}", channel, body);

        try {
            RouteMessageDTO routeMsg = JSONUtil.toBean(body, RouteMessageDTO.class);
            if (routeMsg != null && routeMsg.getTargetUid() != null) {
                // 反序列化内部消息体
                WSBaseResp wsResp = JSONUtil.toBean(routeMsg.getMessageJson(), WSBaseResp.class);

                webSocketService.sendToLocalUid(wsResp, routeMsg.getTargetUid());
            }
        } catch (Exception e) {
            log.error("路由消息处理失败", e);
        }
    }
}
