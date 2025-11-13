package io.shutu.service;

import com.shutu.commons.mybatis.service.CrudService;
import io.shutu.dto.SysSmsDTO;
import io.shutu.entity.SysSmsEntity;

/**
 * 短信
 *
 * @author jiujingz@126.com
 */
public interface SysSmsService extends CrudService<SysSmsEntity, SysSmsDTO> {

    /**
     * 发送短信
     * @param smsCode   短信编码
     * @param mobile   手机号
     * @param params   短信参数
     */
    void send(String smsCode, String mobile, String params);

    SysSmsEntity getBySmsCode(String smsCode);

}

