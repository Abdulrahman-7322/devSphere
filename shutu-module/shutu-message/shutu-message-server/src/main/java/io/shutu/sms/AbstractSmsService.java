package io.shutu.sms;

import io.shutu.dto.SmsConfig;

import java.util.LinkedHashMap;

/**
 * 短信
 *
 * @author jiujingz@126.com
 */
public abstract class AbstractSmsService {
    /**
     * 短信配置信息
     */
    SmsConfig config;

    /**
     * 发送短信
     * @param smsCode   短信编码
     * @param mobile 手机号
     * @param params 参数
     */
    public abstract void sendSms(String smsCode, String mobile, LinkedHashMap<String, String> params);

    /**
     *
     * 发送短信
     * @param smsCode   短信编码
     * @param mobile 手机号
     * @param params 参数
     * @param signName  短信签名
     * @param template 短信模板
     */
    public abstract void sendSms(String smsCode, String mobile, LinkedHashMap<String, String> params, String signName, String template);
}
