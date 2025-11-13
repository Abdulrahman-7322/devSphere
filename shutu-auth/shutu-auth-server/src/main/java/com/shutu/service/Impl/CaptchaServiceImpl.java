package com.shutu.service.Impl;

import com.shutu.commons.tools.redis.RedisKeys;

import com.shutu.commons.tools.redis.RedisUtils;
import com.shutu.service.CaptchaService;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.IOException;

/**
 * 验证码
 */
@Service
@AllArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {
    private final RedisUtils redisUtils;

    @Override
    public void create(HttpServletResponse response, String uuid) throws IOException {
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        //生成验证码
        SpecCaptcha captcha = new SpecCaptcha(150, 40);
        captcha.setLen(5);
        captcha.setCharType(Captcha.TYPE_DEFAULT);
        captcha.out(response.getOutputStream());

        //保存验证码
        setCache(uuid, captcha.text());
    }

    @Override
    public boolean validate(String uuid, String code) {
        String captcha = getCache(uuid);

        //验证码是否正确
        return code.equalsIgnoreCase(captcha);
    }

    private void setCache(String uuid, String captcha) {
        String key = RedisKeys.getLoginCaptchaKey(uuid);

        redisUtils.set(key, captcha, 60 * 5L);
    }

    private String getCache(String uuid) {
        String key = RedisKeys.getLoginCaptchaKey(uuid);
        String captcha = (String) redisUtils.get(key);

        // 删除验证码
        if (captcha != null) {
            redisUtils.delete(key);
        }

        return captcha;
    }
}