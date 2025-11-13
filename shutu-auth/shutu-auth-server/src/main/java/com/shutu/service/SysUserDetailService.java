package com.shutu.service;

import com.shutu.commons.security.user.UserDetail;

/**
 * UserDetail Service
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface SysUserDetailService {
    /**
     * 根据用户ID，获取用户详情
     */
    UserDetail getUserDetailById(Long id);

    /**
     * 根据用户名，获取用户详情
     */
    UserDetail getUserDetailByUsername(String username);

    /**
     * 根据邮箱，获取用户详情
     */
    UserDetail getUserDetailByEmail(String email);

    /**
     * 根据用户名或邮箱，获取用户详情
     * @param email
     * @return
     */
    UserDetail getUserDetailByUsernameOrEmail(String email);
}
