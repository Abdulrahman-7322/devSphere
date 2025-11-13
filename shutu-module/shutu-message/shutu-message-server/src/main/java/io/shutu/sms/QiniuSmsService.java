package io.shutu.sms;

import com.qiniu.http.Response;
import com.qiniu.sms.SmsManager;
import com.qiniu.util.Auth;
import com.shutu.commons.tools.constant.Constant;
import com.shutu.commons.tools.exception.CommonException;
import com.shutu.commons.tools.utils.SpringContextUtils;
import io.shutu.dto.SmsConfig;
import io.shutu.enums.PlatformEnum;
import io.shutu.exception.ModuleErrorCode;
import io.shutu.service.SysSmsLogService;

import java.util.LinkedHashMap;

/**
 * 七牛短信服务
 *
 * @author jiujingz@126.com
 */
public class QiniuSmsService extends AbstractSmsService {
    private SmsManager smsManager;

    public QiniuSmsService(SmsConfig config){
        this.config = config;

        //初始化
        init();
    }


    private void init(){
        Auth auth = Auth.create(config.getQiniuAccessKey(), config.getQiniuSecretKey());
        smsManager = new SmsManager(auth);
    }

    @Override
    public void sendSms(String smsCode, String mobile, LinkedHashMap<String, String> params) {
        this.sendSms(smsCode, mobile, params, null, config.getQiniuTemplateId());
    }

    @Override
    public void sendSms(String smsCode, String mobile, LinkedHashMap<String, String> params, String signName, String template) {
        Response response;
        try {
            response = smsManager.sendSingleMessage(template, mobile, params);
        } catch (Exception e) {
            throw new CommonException("", e, ModuleErrorCode.SEND_SMS_ERROR);
        }

        int status = Constant.SUCCESS;
        if(!response.isOK()){
            status = Constant.FAIL;
        }

        //保存短信记录
        SysSmsLogService sysSmsLogService = SpringContextUtils.getBean(SysSmsLogService.class);
        sysSmsLogService.save(smsCode, PlatformEnum.QINIU.value(), mobile, params, status);

        if(status == Constant.FAIL){
            throw new CommonException(response.error, ModuleErrorCode.SEND_SMS_ERROR);
        }
    }
}
