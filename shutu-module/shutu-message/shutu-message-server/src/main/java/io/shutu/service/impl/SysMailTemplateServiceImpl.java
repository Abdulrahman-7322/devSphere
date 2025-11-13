package io.shutu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shutu.commons.mybatis.service.impl.CrudServiceImpl;
import com.shutu.commons.tools.exception.CommonException;
import com.shutu.commons.tools.exception.ErrorCode;
import com.shutu.commons.tools.redis.RedisKeys;
import com.shutu.commons.tools.redis.RedisUtils;
import com.shutu.commons.tools.utils.JsonUtils;
import com.shutu.commons.tools.utils.Result;
import io.shutu.dao.SysMailTemplateDao;
import io.shutu.dto.SysMailTemplateDTO;
import io.shutu.email.EmailUtils;
import io.shutu.entity.SysMailTemplateEntity;
import io.shutu.service.SysMailTemplateService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class SysMailTemplateServiceImpl extends CrudServiceImpl<SysMailTemplateDao, SysMailTemplateEntity, SysMailTemplateDTO> implements SysMailTemplateService {
    @Resource
    private EmailUtils emailUtils;
    @Autowired
    private RedisUtils redisUtils;

    @Override
    public QueryWrapper<SysMailTemplateEntity> getWrapper(Map<String, Object> params) {
        String name = (String) params.get("name");

        QueryWrapper<SysMailTemplateEntity> wrapper = new QueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(name), "name", name);

        return wrapper;
    }

    @Override
    public boolean sendMail(Long id, String mailTo, String mailCc, String params) throws Exception {
        Map<String, Object> map = null;
        try {
            if (StringUtils.isNotEmpty(params)) {
                map = JsonUtils.parseObject(params, Map.class);
            }
        } catch (Exception e) {
            throw new CommonException("JSON格式化失败",ErrorCode.JSON_FORMAT_ERROR);
        }
        String[] to = new String[]{mailTo};
        String[] cc = StringUtils.isBlank(mailCc) ? null : new String[]{mailCc};

        return emailUtils.sendMail(id, to, cc, map);
    }


    /**
     * 发送验证码到指定的邮箱地址
     * @param mailTo 邮箱接收人的邮箱地址。该地址将作为邮件的接收方。
     * @return Result 返回一个结果对象。
     * @throws Exception 抛出异常处理邮件发送过程中的错误，例如网络异常或邮件内容构建错误。
     */
    @Override
    public Result<Boolean> sendMail(String mailTo) throws Exception {
        if(StringUtils.isBlank(mailTo)){
            return new Result<Boolean>().error("邮箱不能为空");
        }
        // 初始化变量用于存储随机生成的验证码
        Map<String, Object> map = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        // 生成6位随机数字验证码
        for (int i = 0; i < 6; i++) {
            sb.append(rand.nextInt(10));
        }
        String authCode = sb.toString();
        // 将验证码存储到map中，准备用于邮件内容
        map.put("code", authCode);
        // 设置邮件接收人的邮箱地址
        String[] to = new String[]{mailTo};
        // 将验证码存储到Redis中，设置5分钟的过期时间
        redisUtils.set(RedisKeys.getAuthCode(mailTo), authCode, RedisUtils.FIVE_MINUTE_EXPIRE);
        // 调用邮件工具类发送邮件
        boolean flag = emailUtils.sendMail(1067246875800000145L, to, null, map);
        // 根据邮件发送结果返回对应的结果对象
        if (flag) {
            return new Result<>();
        } else {
            return null;
        }
    }
}
