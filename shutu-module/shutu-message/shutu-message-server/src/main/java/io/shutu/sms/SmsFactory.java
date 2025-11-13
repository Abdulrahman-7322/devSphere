package io.shutu.sms;

import com.shutu.commons.tools.utils.JsonUtils;
import com.shutu.commons.tools.utils.SpringContextUtils;
import io.shutu.dto.SmsConfig;
import io.shutu.entity.SysSmsEntity;
import io.shutu.enums.PlatformEnum;
import io.shutu.service.SysSmsService;

/**
 * 短信Factory
 * @author jiujingz@126.com
 */
public class SmsFactory {
    private static SysSmsService sysSmsService;

    static {
        SmsFactory.sysSmsService = SpringContextUtils.getBean(SysSmsService.class);
    }

    public static AbstractSmsService build(String smsCode){
        //获取短信配置信息
        SysSmsEntity smsEntity = sysSmsService.getBySmsCode(smsCode);
        SmsConfig config = JsonUtils.parseObject(smsEntity.getSmsConfig(), SmsConfig.class);

        if(smsEntity.getPlatform() == PlatformEnum.ALIYUN.value()){
            return new AliyunSmsService(config);
        }else if(smsEntity.getPlatform() == PlatformEnum.QCLOUD.value()){
            return new QcloudSmsService(config);
        }else if(smsEntity.getPlatform() == PlatformEnum.QINIU.value()){
            return new QiniuSmsService(config);
        }

        return null;
    }
}