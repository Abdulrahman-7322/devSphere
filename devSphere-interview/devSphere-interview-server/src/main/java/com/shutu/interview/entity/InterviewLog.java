package com.shutu.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.shutu.commons.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 面试问答日志表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dev_sphere_interview_logs")
public class InterviewLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 关联面试ID
     */
    private Long interviewId;

    /**
     * 轮次 (第几题)
     */
    private Integer round;

    /**
     * 当时的问题文本
     */
    private String questionText;

    /**
     * 回答录音地址 (OSS)
     */
    private String answerAudioUrl;

    /**
     * 回答转录文本 (ASR)
     */
    private String answerText;

    /**
     * 单题得分
     */
    private BigDecimal score;

    /**
     * 单题评价/建议
     */
    private String evaluation;
}
