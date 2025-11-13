package com.shutu.devSphere.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shutu.devSphere.model.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.jmx.export.annotation.ManagedNotification;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

}




