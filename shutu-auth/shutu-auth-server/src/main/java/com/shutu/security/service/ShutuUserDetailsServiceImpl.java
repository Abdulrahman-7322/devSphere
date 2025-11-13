package com.shutu.security.service;

import com.shutu.commons.security.enums.UserStatusEnum;
import com.shutu.commons.security.user.UserDetail;
import com.shutu.commons.tools.exception.CommonException;
import com.shutu.commons.tools.exception.ErrorCode;
import com.shutu.service.SysUserDetailService;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UserDetailsService
 *
 */
@Service
public class ShutuUserDetailsServiceImpl implements UserDetailsService {
    @Resource
    private SysUserDetailService sysUserDetailService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 使用邮箱登录
        UserDetail userDetail = sysUserDetailService.getUserDetailByUsername(username);
        if (userDetail == null) {
            throw new CommonException("用户不存在",ErrorCode.ACCOUNT_NOT_EXIST);
        }

        // 账号不可用
        if (userDetail.getStatus() == UserStatusEnum.DISABLE.value()) {
            userDetail.setEnabled(false);
        }

        return userDetail;
    }
}
