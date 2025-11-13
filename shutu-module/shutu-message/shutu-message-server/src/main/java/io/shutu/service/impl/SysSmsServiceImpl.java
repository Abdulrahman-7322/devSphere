package io.shutu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shutu.commons.mybatis.service.impl.CrudServiceImpl;
import com.shutu.commons.tools.exception.CommonException;
import com.shutu.commons.tools.exception.ErrorCode;
import com.shutu.commons.tools.utils.ConvertUtils;
import com.shutu.commons.tools.utils.JsonUtils;
import io.shutu.dao.SysSmsDao;
import io.shutu.dto.SmsConfig;
import io.shutu.dto.SysSmsDTO;
import io.shutu.entity.SysSmsEntity;
import io.shutu.exception.ModuleErrorCode;
import io.shutu.service.SysSmsService;
import io.shutu.sms.AbstractSmsService;
import io.shutu.sms.SmsFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SysSmsServiceImpl extends CrudServiceImpl<SysSmsDao, SysSmsEntity, SysSmsDTO> implements SysSmsService {

    @Override
    public QueryWrapper<SysSmsEntity> getWrapper(Map<String, Object> params){
        String platform = (String)params.get("platform");

        QueryWrapper<SysSmsEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(platform), "platform", platform);

        return wrapper;
    }

    @Override
    public SysSmsDTO get(Long id) {
        SysSmsEntity entity = baseDao.selectById(id);

        SysSmsDTO dto = ConvertUtils.sourceToTarget(entity, SysSmsDTO.class);
        dto.setConfig(JsonUtils.parseObject(entity.getSmsConfig(), SmsConfig.class));

        return dto;
    }

    @Override
    public void send(String smsCode, String mobile, String params) {
        LinkedHashMap<String, String> map;
        try {
            map = JsonUtils.parseObject(params, LinkedHashMap.class);
        }catch (Exception e){
            throw new CommonException("",ErrorCode.JSON_FORMAT_ERROR);
        }

        //短信服务
        AbstractSmsService service = SmsFactory.build(smsCode);
        if(service == null){
            throw new CommonException("",ModuleErrorCode.SMS_CONFIG);
        }

        //发送短信
        service.sendSms(smsCode, mobile, map);
    }

    @Override
    public SysSmsEntity getBySmsCode(String smsCode) {
        QueryWrapper<SysSmsEntity> query = new QueryWrapper<>();
        query.eq("sms_code", smsCode);

        return baseDao.selectOne(query);
    }

    @Override
    public void save(SysSmsDTO dto) {
        SysSmsEntity entity = ConvertUtils.sourceToTarget(dto, SysSmsEntity.class);
        entity.setSmsConfig(JsonUtils.toJsonString(dto.getConfig()));
        baseDao.insert(entity);
    }

    @Override
    public void update(SysSmsDTO dto) {
        SysSmsEntity entity = ConvertUtils.sourceToTarget(dto, SysSmsEntity.class);
        entity.setSmsConfig(JsonUtils.toJsonString(dto.getConfig()));
        baseDao.updateById(entity);
    }
}
