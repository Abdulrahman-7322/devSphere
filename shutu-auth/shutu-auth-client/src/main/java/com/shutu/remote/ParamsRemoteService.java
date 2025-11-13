

package com.shutu.remote;

import com.shutu.commons.tools.exception.ErrorCode;
import com.shutu.commons.tools.exception.ZException;
import com.shutu.commons.tools.utils.JsonUtils;
import com.shutu.feign.ParamsFeignClient;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
 
/**
 * 参数
 *
 * @author jiujingz@126.com
 */
@Component
public class ParamsRemoteService {
    @Resource
    private ParamsFeignClient paramsFeignClient;

    /**
     * 根据参数编码，获取value的Object对象
     *
     * @param paramCode 参数编码
     * @param clazz     Object对象
     */
    public <T> T getValueObject(String paramCode, Class<T> clazz) {
        String paramValue = paramsFeignClient.getValue(paramCode);
        if (StringUtils.isNotBlank(paramValue)) {
            return JsonUtils.parseObject(paramValue, clazz);
        }

        try {
            return clazz.newInstance();
        } catch (ZException | InstantiationException | IllegalAccessException e) {
            throw new ZException(ErrorCode.PARAMS_GET_ERROR);
        }
    }

    /**
     * 根据参数编码，更新value
     *
     * @param paramCode  参数编码
     * @param paramValue 参数值
     */
    public void updateValueByCode(String paramCode, String paramValue) {
        paramsFeignClient.updateValueByCode(paramCode, paramValue);
    }

}