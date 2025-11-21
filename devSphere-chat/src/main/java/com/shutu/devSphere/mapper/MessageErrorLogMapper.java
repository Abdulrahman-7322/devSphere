package com.shutu.devSphere.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shutu.devSphere.model.entity.MessageErrorLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息死信日志 Mapper
 */
@Mapper
public interface MessageErrorLogMapper extends BaseMapper<MessageErrorLog> {
}