package io.shutu.service.impl;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shutu.commons.mybatis.service.impl.CrudServiceImpl;
import io.shutu.dao.SysSmsLogDao;
import io.shutu.dto.SysSmsLogDTO;
import io.shutu.entity.SysSmsLogEntity;
import io.shutu.service.SysSmsLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 短信日志
 *
 * @author jiujingz@126.com
 */
@Service
public class SysSmsLogServiceImpl extends CrudServiceImpl<SysSmsLogDao, SysSmsLogEntity, SysSmsLogDTO> implements SysSmsLogService {

    @Override
    public QueryWrapper<SysSmsLogEntity> getWrapper(Map<String, Object> params){
        String smsCode = (String)params.get("smsCode");
        String mobile = (String)params.get("mobile");
        String status = (String)params.get("status");

        QueryWrapper<SysSmsLogEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(smsCode), "sms_code", smsCode);
        wrapper.like(StringUtils.isNotBlank(mobile), "mobile", mobile);
        wrapper.eq(StringUtils.isNotBlank(status), "status", status);

        return wrapper;
    }

    @Override
    public void save(String smsCode, Integer platform, String mobile, LinkedHashMap<String, String> params, Integer status) {

        SysSmsLogEntity smsLog = new SysSmsLogEntity();
        smsLog.setSmsCode(smsCode);
        smsLog.setPlatform(platform);
        smsLog.setMobile(mobile);

        //设置短信参数
        if(MapUtil.isNotEmpty(params)){
            int index = 1;
            for(String value : params.values()){
                if(index == 1){
                    smsLog.setParams1(value);
                }else if(index == 2){
                    smsLog.setParams2(value);
                }else if(index == 3){
                    smsLog.setParams3(value);
                }else if(index == 4){
                    smsLog.setParams4(value);
                }
                index++;
            }
        }

        smsLog.setStatus(status);

        baseDao.insert(smsLog);
    }
}