

package io.shutu.enums;

/**
 * 平台枚举
 *
 * @author jiujingz@126.com
 */
public enum PlatformEnum {
    /**
     * 阿里云
     */
    ALIYUN(1),
    /**
     * 腾讯云
     */
    QCLOUD(2),
    /**
     * 七牛云
     */
    QINIU(3);

    private final int value;

    PlatformEnum(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}