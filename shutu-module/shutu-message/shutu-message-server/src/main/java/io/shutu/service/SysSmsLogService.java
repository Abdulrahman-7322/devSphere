package io.shutu.service;

import com.shutu.commons.mybatis.service.CrudService;
import io.shutu.dto.SysSmsLogDTO;
import io.shutu.entity.SysSmsLogEntity;
import java.util.LinkedHashMap;

/**
 * 短信日志
 *
 * @author jiujingz@126.com
 */
public interface SysSmsLogService extends CrudService<SysSmsLogEntity, SysSmsLogDTO> {

    /**
     * 保存短信发送记录
     * @param smsCode   短信编码
     * @param platform  平台
     * @param mobile    手机号
     * @param params    短信参数
     * @param status    发送状态
     */
    void save(String smsCode, Integer platform, String mobile, LinkedHashMap<String, String> params, Integer status);
}