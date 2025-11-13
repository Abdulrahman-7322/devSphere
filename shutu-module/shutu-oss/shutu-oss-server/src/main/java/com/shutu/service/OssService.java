package com.shutu.service;

import com.shutu.commons.mybatis.service.BaseService;
import com.shutu.commons.tools.page.PageData;
import com.shutu.entity.OssEntity;
import java.util.Map;

/**
 * 文件上传
 * 
 * @author jiujingz@126.com
 */
public interface OssService extends BaseService<OssEntity> {

	PageData<OssEntity> page(Map<String, Object> params);
}
