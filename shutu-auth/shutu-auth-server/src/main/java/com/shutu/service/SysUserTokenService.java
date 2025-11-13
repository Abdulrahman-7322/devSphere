package com.shutu.service;


import com.shutu.model.dto.UserTokenDTO;
import com.shutu.model.entity.SysUserTokenEntity;
import com.shutu.commons.mybatis.service.BaseService;

/**
 * 用户 Token
 * @author jiujingz@126.com
 */

public interface SysUserTokenService extends BaseService<SysUserTokenEntity> {

    /**
     * 根据用户ID，生成用户Token
     *
     * @param userId 用户ID
     * @return 用户Token
     */
    UserTokenDTO createToken(Long userId);

    /**
     * 根据refreshToken，生成新Token
     *
     * @param refreshToken refreshToken
     * @return 用户Token
     */
    UserTokenDTO refreshToken(String refreshToken);

    /**
     * Token过期
     *
     * @param userId 用户ID
     */
    void expireToken(Long userId);

    /**
     * 根据角色ID，更新用户缓存权限
     *
     * @param roleId 角色ID
     */
    void updateCacheAuthByRoleId(Long roleId);

    /**
     * 根据用户ID，更新用户缓存权限
     *
     * @param userId 用户ID
     */
    void updateCacheAuthByUserId(Long userId);

    Boolean isSave(String username);
}