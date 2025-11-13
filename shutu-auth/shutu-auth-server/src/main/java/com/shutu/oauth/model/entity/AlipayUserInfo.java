package com.shutu.oauth.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlipayUserInfo implements OAuthUserInfo {

    // 支付宝用户的唯一userId
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("nick_name")
    private String nickname;

    @JsonProperty("avatar")
    private String avatarUrl;

    private String rawUserInfo;

    @Override
    public String getUniqueId() {
        return this.userId;
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
        return "alipay";
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