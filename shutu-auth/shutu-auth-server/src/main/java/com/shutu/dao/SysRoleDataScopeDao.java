package com.shutu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shutu.model.entity.SysRoleDataScopeEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 角色数据权限
 */
@Mapper
public interface SysRoleDataScopeDao extends BaseMapper<SysRoleDataScopeEntity> {

    /**
     * 根据角色ID，获取部门ID列表
     */
    List<Long> getDeptIdList(Long roleId);

    /**
     * 获取用户的部门数据权限列表
     */
    List<Long> getDataScopeList(Long userId);

    /**
     * 根据角色id，删除角色数据权限关系
     * @param roleId 角色id
     */
    void deleteByRoleId(Long roleId);
}