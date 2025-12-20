package com.shutu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shutu.mapper.MessageArchiveMapper;
import com.shutu.model.entity.MessageArchive;
import com.shutu.service.MessageArchiveService;
import org.springframework.stereotype.Service;

/**
 * 消息归档服务 实现类
 */
@Service
public class MessageArchiveServiceImpl extends ServiceImpl<MessageArchiveMapper, MessageArchive>
        implements MessageArchiveService {
}
