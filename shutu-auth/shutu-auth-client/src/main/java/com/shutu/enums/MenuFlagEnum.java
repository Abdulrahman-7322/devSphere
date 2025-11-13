

package com.shutu.enums;

/**
 * 菜单资源标识
 
 */
public enum MenuFlagEnum {
    /**
     * 菜单资源
     */
    YES(1),
    /**
     * 非菜单资源
     */
    NO(0);

    private final int value;

    MenuFlagEnum(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}