package com.shutu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shutu.commons.mybatis.service.impl.BaseServiceImpl;
import com.shutu.commons.tools.constant.Constant;
import com.shutu.commons.tools.page.PageData;
import com.shutu.dao.OssDao;
import com.shutu.entity.OssEntity;
import com.shutu.service.OssService;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class OssServiceImpl extends BaseServiceImpl<OssDao, OssEntity> implements OssService {

	@Override
	public PageData<OssEntity> page(Map<String, Object> params) {
		IPage<OssEntity> page = baseDao.selectPage(
			getPage(params, Constant.CREATE_DATE, false),
			new QueryWrapper<>()
		);
		return getPageData(page, OssEntity.class);
	}
}
