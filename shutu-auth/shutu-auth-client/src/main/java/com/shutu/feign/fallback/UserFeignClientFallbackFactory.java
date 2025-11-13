/**
 * Copyright (c) 2016-2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package com.shutu.feign.fallback;

import com.shutu.commons.security.user.UserDetail;
import com.shutu.commons.tools.utils.Result;
import com.shutu.dto.SysUserDTO;
import com.shutu.feign.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户接口 FallbackFactory
 *
 * @author jiujingz@126.com
 */
@Slf4j
@Component
public class UserFeignClientFallbackFactory implements FallbackFactory<UserFeignClient> {
    @Override
    public UserFeignClient create(Throwable throwable) {
        log.error("{}", throwable);

        return new UserFeignClient() {
            @Override
            public Result<UserDetail> getById(Long id) {
                return new Result<>();
            }

            @Override
            public Result<List<Long>> getRoleIdList(Long userId) {
                return new Result<>();
            }

            @Override
            public Result<List<Long>> getUserIdListByRoleIdList(List<Long> ids) {
                return new Result<>();
            }

            @Override
            public Result<List<Long>> getUserIdListByPostIdList(List<Long> ids) {
                return new Result<>();
            }

            @Override
            public Result<List<Long>> getLeaderIdListByDeptIdList(List<Long> ids) {
                return new Result<>();
            }

            @Override
            public Result<Long> getLeaderIdListByUserId(Long userId) {
                return new Result<>();
            }

            @Override
            public Result<List<SysUserDTO>> listByIds(List<Long> ids) {
                return new Result<>();
            }

            @Override
            public Result<SysUserDTO> getByUsername(String query) {
                return new Result<>();
            }
        };
    }
}
