package com.shutu.commons.log;

import lombok.Data;

import java.io.Serializable;

/**
 * Log基类
 */
@Data
public abstract class BaseLog implements Serializable {
    /**
     * 日志类型
     */
    private Integer type;

}