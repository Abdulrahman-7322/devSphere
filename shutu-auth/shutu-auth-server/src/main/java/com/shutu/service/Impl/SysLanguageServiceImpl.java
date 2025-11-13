package com.shutu.service.Impl;

import com.shutu.dao.SysLanguageDao;
import com.shutu.model.entity.SysLanguageEntity;
import com.shutu.service.SysLanguageService;
import com.shutu.commons.mybatis.service.impl.BaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 国际化
 */
@Service
public class SysLanguageServiceImpl extends BaseServiceImpl<SysLanguageDao, SysLanguageEntity> implements SysLanguageService {

    @Override
    public void saveOrUpdate(String tableName, Long tableId, String fieldName, String fieldValue, String language) {
        SysLanguageEntity entity = new SysLanguageEntity();
        entity.setTableName(tableName);
        entity.setTableId(tableId);
        entity.setFieldName(fieldName);
        entity.setFieldValue(fieldValue);
        entity.setLanguage(language);

        //判断是否有数据
        if(baseDao.getLanguage(entity) == null){
            baseDao.insert(entity);
        }else {
            baseDao.updateLanguage(entity);
        }
    }
}