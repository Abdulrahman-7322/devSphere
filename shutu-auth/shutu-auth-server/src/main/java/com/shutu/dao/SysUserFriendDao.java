package com.shutu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shutu.model.entity.SysUserFriendEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 好友管理
 
 */
@Mapper
public interface SysUserFriendDao extends BaseMapper<SysUserFriendEntity> {
    @Select("select friend_id from sys_user_friend where user_id = #{userId}")
    List<Long> getFriendId(@Param("userId") Long userId);

}































