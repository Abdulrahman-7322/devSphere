package com.shutu.commons.log.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息相关操作ENUM
 */
public enum UserInfoOperationEnum {

    UPDATE_SUCCESS(0,"修改成功"),
    REGISTER_SUCCESS(0, "注册成功"),

    // 用户名相关
    USERNAME_NULL(1001, "用户名不能为空"),
    USERNAME_EXIST(1002, "用户名已存在"),
    USERNAME_INVALID(1003, "用户名不符合规则"),

    // 密码相关
    PASSWORD_NULL(2001, "密码不能为空"),
    PASSWORD_TOO_SHORT(2002, "密码长度不能小于6位"),
    PASSWORD_TOO_SIMPLE(2003, "密码过于简单，请包含数字和字母"),

    // 邮箱相关
    EMAIL_NULL(3001, "邮箱不能为空"),
    EMAIL_INVALID(3002, "邮箱格式不正确"),
    EMAIL_EXIST(3003, "邮箱已被注册"),

    // 手机号相关
    PHONE_NULL(4001, "手机号不能为空"),
    PHONE_INVALID(4002, "手机号格式不正确"),
    PHONE_EXIST(4003, "手机号已被注册"),

    // 其他
    SYSTEM_ERROR(9000, "系统异常，请稍后再试");

    private final int code;
    private final String message;

    private static final Map<String, Integer> codeMessageMap = new HashMap<>();

    static {
        for (UserInfoOperationEnum e : values()) {
            codeMessageMap.put(e.message, e.code);
        }
    }

    UserInfoOperationEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 通过 code 获取对应的 message
     */
    public static Integer getMessageByCode(String message) {
        return codeMessageMap.getOrDefault(message, 10000);
    }

}
