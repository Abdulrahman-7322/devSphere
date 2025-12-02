package com.shutu.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.shutu.commons.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * 面试题目表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dev_sphere_questions")
public class Question extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 题目内容
     */
    private String content;

    /**
     * 题目向量 (OpenAI/DeepSeek 1536维)
     */
    @TableField(typeHandler = com.shutu.interview.handler.PGVectorTypeHandler.class)
    private List<Double> embedding;

    /**
     * 分类 (如: Java, SystemDesign)
     */
    private String category;

    /**
     * 难度 (1-5)
     */
    private Integer difficulty;

    /**
     * 参考答案
     */
    private String standardAnswer;
}
