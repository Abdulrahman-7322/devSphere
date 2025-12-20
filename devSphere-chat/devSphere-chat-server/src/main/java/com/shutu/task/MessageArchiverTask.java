package com.shutu.task;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shutu.model.entity.Message;
import com.shutu.model.entity.MessageArchive;
import com.shutu.service.MessageArchiveService;
import com.shutu.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息归档任务
 * 职责：按天将旧消息搬运到归档表，保持主表轻量高效
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageArchiverTask {

    private final MessageService messageService;
    private final MessageArchiveService messageArchiveService;
    private final TransactionTemplate transactionTemplate;

    // 归档阈值：3个月
    private static final int ARCHIVE_MONTHS = 3;
    // 单批搬运数量
    private static final int BATCH_SIZE = 1000;

    /**
     * 定时任务：每天凌晨 3:00 执行
     * 逻辑：扫描 -> 复制 -> 删除 (事务保证)
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void archiveOldMessages() {
        log.info("[消息归档] 任务启动...");
        Date thresholdDate = DateUtil.offsetMonth(new Date(), -ARCHIVE_MONTHS);
        log.info("[消息归档] 归档截止时间: {}", DateUtil.formatDateTime(thresholdDate));

        long totalArchived = 0;
        while (true) {
            // 1. 扫描待归档消息 (主表)
            List<Message> oldMessages = messageService.list(new LambdaQueryWrapper<Message>()
                    .lt(Message::getCreateTime, thresholdDate)
                    .last("LIMIT " + BATCH_SIZE));

            if (CollUtil.isEmpty(oldMessages)) {
                log.info("[消息归档] 扫描完毕，无更多过期消息");
                break;
            }

            // 2. 转换为归档实体
            List<MessageArchive> archiveList = oldMessages.stream()
                    .map(msg -> {
                        MessageArchive archive = new MessageArchive();
                        BeanUtil.copyProperties(msg, archive);
                        return archive;
                    }).collect(Collectors.toList());

            // 3. 事务执行：搬运 + 清理
            boolean success = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
                try {
                    // 插入归档表
                    messageArchiveService.saveBatch(archiveList);
                    // 删除主表
                    List<Long> ids = oldMessages.stream().map(Message::getId).collect(Collectors.toList());
                    messageService.removeByIds(ids);
                    return true;
                } catch (Exception e) {
                    log.error("[消息归档] 批次处理失败", e);
                    status.setRollbackOnly();
                    return false;
                }
            }));

            if (success) {
                totalArchived += oldMessages.size();
                log.info("[消息归档] 已成功迁移 {} 条消息...", totalArchived);
            } else {
                log.error("[消息归档] 事务回滚，任务异常终止");
                break;
            }

            // 避免长时间占用 CPU 和 IO，每批次稍作休眠
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
        log.info("[消息归档] 任务结束，本次共归档: {} 条", totalArchived);
    }
}
