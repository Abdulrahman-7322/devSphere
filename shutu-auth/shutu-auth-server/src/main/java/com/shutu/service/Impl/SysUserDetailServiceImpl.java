package com.shutu.service.Impl;

import com.shutu.commons.security.user.UserDetail;
import com.shutu.commons.tools.utils.ConvertUtils;
import com.shutu.dao.SysUserDao;
import com.shutu.model.entity.SysUserEntity;
import com.shutu.redis.SysMenuRedis;
import com.shutu.service.SysMenuService;
import com.shutu.service.SysRoleDataScopeService;
import com.shutu.service.SysUserDetailService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

/**
 * UserDetail Service
 */
@Slf4j
@Service
class SysUserDetailServiceImpl implements SysUserDetailService {
    @Resource
    private SysUserDao sysUserDao;
    @Resource
    private SysMenuRedis sysMenuRedis;
    @Resource
    private SysMenuService sysMenuService;
    @Resource
    private SysRoleDataScopeService sysRoleDataScopeService;

    @Override
    public UserDetail getUserDetailById(Long id) {
        SysUserEntity user = sysUserDao.getById(id);

        UserDetail userDetail = ConvertUtils.sourceToTarget(user, UserDetail.class);

        initUserData(userDetail);

        return userDetail;
    }

    @Override
    public UserDetail getUserDetailByUsername(String username) {
        SysUserEntity user = sysUserDao.getByUsername(username);

        UserDetail userDetail = ConvertUtils.sourceToTarget(user, UserDetail.class);
        initUserData(userDetail);

        return userDetail;
    }

    @Override
    public UserDetail getUserDetailByEmail(String email) {
        SysUserEntity user = sysUserDao.getByEmail(email);

        UserDetail userDetail = ConvertUtils.sourceToTarget(user, UserDetail.class);
        initUserData(userDetail);

        return userDetail;
    }

    @Override
    public UserDetail getUserDetailByUsernameOrEmail(String identifier) {
        SysUserEntity user = sysUserDao.getUserDetailByUsernameOrEmail(identifier,identifier);

        UserDetail userDetail = ConvertUtils.sourceToTarget(user, UserDetail.class);
        initUserData(userDetail);

        return userDetail;
    }

    /**
     * 初始化用户数据
     */
    private void initUserData(UserDetail userDetail) {
        if (userDetail == null) {
            return;
        }

        //清空当前用户，菜单导航、权限标识
        sysMenuRedis.delete(userDetail.getId());

        //用户部门数据权限
        List<Long> deptIdList = sysRoleDataScopeService.getDataScopeList(userDetail.getId());
        userDetail.setDeptIdList(deptIdList);

        //获取用户权限标识
        Set<String> authorities = sysMenuService.getUserPermissions(userDetail);
        userDetail.setAuthoritySet(authorities);
    }
}
