

package com.shutu.enums;

/**
 * OSS类型枚举
 *
 * @author jiujingz@126.com
 * @since 1.1.0
 */
public enum OssTypeEnum {

    /**
     * 本地
     */
    LOCAL(5),
    /**
     * MinIO
     */
    MINIO(6);

    private int value;

    OssTypeEnum(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}