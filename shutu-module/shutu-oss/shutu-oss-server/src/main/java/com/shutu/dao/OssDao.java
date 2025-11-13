package com.shutu.dao;

import com.shutu.commons.mybatis.dao.BaseDao;
import com.shutu.entity.OssEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件上传
 */
@Mapper
public interface OssDao extends BaseDao<OssEntity> {
	
}
