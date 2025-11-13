
package com.shutu.commons.mybatis.enums;

/**
 * 删除标识枚举类
 
 */
public enum DelFlagEnum {
    NORMAL(0),
    DEL(1);

    private int value;

    DelFlagEnum(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
