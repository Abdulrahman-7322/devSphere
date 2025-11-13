package com.shutu.service;

import com.shutu.commons.mybatis.service.BaseService;
import com.shutu.commons.tools.page.PageData;
import com.shutu.model.entity.SysParamsEntity;
import com.shutu.shutuadminclient.dto.SysParamsDTO;
import java.util.List;
import java.util.Map;

/**
 * 参数管理
 */
public interface SysParamsService extends BaseService<SysParamsEntity> {

    PageData<SysParamsDTO> page(Map<String, Object> params);

    List<SysParamsDTO> list(Map<String, Object> params);

    SysParamsDTO get(Long id);

    void save(SysParamsDTO dto);

    void update(SysParamsDTO dto);

    void delete(Long[] ids);

    /**
     * 根据参数编码，获取参数的value值
     *
     * @param paramCode  参数编码
     */
    String getValue(String paramCode);

    /**
     * 根据参数编码，更新value
     * @param paramCode  参数编码
     * @param paramValue  参数值
     */
    int updateValueByCode(String paramCode, String paramValue);
}
