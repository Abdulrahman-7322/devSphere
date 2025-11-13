package com.shutu.oauth.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QQUserInfo implements OAuthUserInfo {

    // QQ返回码，0为成功
    @JsonProperty("ret")
    private int ret;

    // 错误信息
    @JsonProperty("msg")
    private String msg;

    @JsonProperty("openid")
    private String openid;

    @JsonProperty("nickname")
    private String nickname;

    // QQ高清头像
    @JsonProperty("figureurl_qq_2")
    private String avatarUrl;

    private String rawUserInfo;

    @Override
    public String getUniqueId() {
        return this.openid;
    }

    @Override
    public String getNickname() {
        return this.nickname;
    }

    @Override
    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    @Override
    public String getPlatform() {
        return "qq";
    }

    @SneakyThrows
    @Override
    public String getRawUserInfo() {
        if (this.rawUserInfo == null) {
            this.rawUserInfo = new ObjectMapper().writeValueAsString(this);
        }
        return this.rawUserInfo;
    }
}