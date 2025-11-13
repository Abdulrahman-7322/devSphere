package com.shutu.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@TableName("sys_user_friend")
public class SysUserFriendEntity {
    @TableId
    private Long id;

    /**
     * 好友id
     */
    private Long friendId;

    /**
     * 用户id
     */
    private Long userId;
}
