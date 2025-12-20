package com.shutu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shutu.model.entity.MessageArchive;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息归档表 Mapper 接口
 */
@Mapper
public interface MessageArchiveMapper extends BaseMapper<MessageArchive> {
}
