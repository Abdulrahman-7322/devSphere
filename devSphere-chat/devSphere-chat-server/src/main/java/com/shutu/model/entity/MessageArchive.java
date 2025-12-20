package com.shutu.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息归档表 (冷数据)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("dev_sphere_message_archive")
public class MessageArchive implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.INPUT) // 归档时保留原ID
    private Long id;

    /**
     * 唯一id，客户端可见
     */
    private Long serverMsgId;

    /**
     * 客户端临时ID
     */
    private String tempId;

    /**
     * 会话表id
     */
    private Long roomId;

    /**
     * 消息发送者uid
     */
    private Long fromUid;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 回复的消息内容
     */
    private Long replyMsgId;

    /**
     * 消息状态 0正常 1删除
     */
    private Integer status;

    /**
     * 与回复的消息间隔多少条
     */
    private Integer gapCount;

    /**
     * 消息类型 1正常文本 2.撤回消息 3.图片 4.语音 5.视频 6.文件
     */
    private Integer type;

    /**
     * 扩展信息
     */
    private Object extra;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;
}
