package com.shutu.devSphere.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shutu.devSphere.mapper.MessageErrorLogMapper;
import com.shutu.devSphere.model.entity.MessageErrorLog;
import com.shutu.devSphere.service.MessageErrorLogService;
import org.springframework.stereotype.Service;

/**
 * 消息死信日志服务实现
 */
@Service
public class MessageErrorLogServiceImpl extends ServiceImpl<MessageErrorLogMapper, MessageErrorLog>
        implements MessageErrorLogService {
}