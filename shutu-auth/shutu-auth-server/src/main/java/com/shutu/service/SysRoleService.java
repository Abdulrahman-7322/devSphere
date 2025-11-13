package com.shutu.service;

import com.shutu.commons.tools.page.PageData;
import com.shutu.model.dto.SysRoleDTO;
import com.shutu.model.entity.SysRoleEntity;
import com.shutu.commons.mybatis.service.BaseService;

import java.util.List;
import java.util.Map;

/**
 * 角色管理
 */
public interface SysRoleService extends BaseService<SysRoleEntity> {

    PageData<SysRoleDTO> page(Map<String, Object> params);

    List<SysRoleDTO> list(Map<String, Object> params);

    SysRoleDTO get(Long id);

    void save(SysRoleDTO dto);

    void update(SysRoleDTO dto);

    void delete(Long[] ids);

}