package com.shutu.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.shutu.commons.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 评分结果表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dev_sphere_scores")
public class Score extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 关联面试ID
     */
    private Long interviewId;

    /**
     * 评分维度 (如: Knowledge, Logic, Communication)
     */
    private String dimension;

    /**
     * 分数
     */
    private BigDecimal score;

    /**
     * 维度评语
     */
    private String comment;
}
