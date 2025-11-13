package com.shutu.dao;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shutu.model.entity.SysRoleMenuEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 角色菜单关系
 */
@Mapper
public interface SysRoleMenuDao extends BaseMapper<SysRoleMenuEntity> {

    /**
     * 根据角色ID，获取菜单ID列表
     */
    List<Long> getMenuIdList(Long roleId);

    /**
     * 根据角色id，删除角色菜单关系
     * @param roleId 角色id
     */
    void deleteByRoleId(Long roleId);

    /**
     * 根据菜单id，删除角色菜单关系
     * @param menuId 菜单id
     */
    void deleteByMenuId(Long menuId);
}