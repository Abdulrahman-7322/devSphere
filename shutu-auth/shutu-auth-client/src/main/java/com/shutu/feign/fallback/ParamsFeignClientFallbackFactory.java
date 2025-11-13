/**
 * Copyright (c) 2016-2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package com.shutu.feign.fallback;


import com.shutu.feign.ParamsFeignClient;
import org.springframework.stereotype.Component;

/**
 * 参数接口 FallbackFactory
 *
 * @author jiujingz@126.com
 */
@Component
public class ParamsFeignClientFallbackFactory implements ParamsFeignClient {
    @Override
    public String getValue(String paramCode) {
        return null;
    }

    @Override
    public void updateValueByCode(String paramCode, String paramValue) {

    }
}
