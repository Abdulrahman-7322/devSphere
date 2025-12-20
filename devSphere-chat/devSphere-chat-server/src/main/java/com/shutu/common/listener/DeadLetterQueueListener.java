package com.shutu.common.listener;

import com.shutu.config.NodeConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * 死信队列消费者
 * 负责：监听 im:message:dlq，记录异常日志并报警
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterQueueListener implements StreamListener<String, MapRecord<String, String, String>> {

    private final StringRedisTemplate redisTemplate;
    private final NodeConfig nodeConfig;
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer;

    // 死信队列 Key
    private static final String DLQ_STREAM_KEY = "im:message:dlq";
    private static final String DLQ_GROUP = "im_dlq_group";

    @PostConstruct
    public void start() {
        try {
            // 1. 创建消费者组 (如果不存在)
            try {
                redisTemplate.opsForStream().createGroup(DLQ_STREAM_KEY, DLQ_GROUP);
            } catch (Exception e) {
                // Group 已存在，忽略
            }

            // 2. 注册监听容器
            streamMessageListenerContainer.receive(
                    Consumer.from(DLQ_GROUP, "dlq_consumer_" + nodeConfig.getNodeId()),
                    StreamOffset.create(DLQ_STREAM_KEY, ReadOffset.lastConsumed()),
                    this);

            // 容器已在 RedisStreamConfig 中启动，这里只需注册
            log.info("[DLQ] 死信队列监听器已启动: group={}", DLQ_GROUP);

        } catch (Exception e) {
            log.error("[DLQ] 启动失败", e);
        }
    }

    @Override
    public void onMessage(MapRecord<String, String, String> record) {
        String msgId = record.getId().getValue();
        java.util.Map<String, String> value = record.getValue();
        String tempId = value.get("tempId");

        // 1. 打印报警日志 (这里可以对接钉钉/飞书)
        log.error("[死信告警] 发现无法处理的死信消息! ID={}, tempId={}, Content={}", msgId, tempId, value.get("content"));

        // 2. 简单的 ACK 处理，避免死信堆积 (实际业务可能需要做人工归档表)
        redisTemplate.opsForStream().acknowledge(DLQ_STREAM_KEY, DLQ_GROUP, record.getId());
    }
}
