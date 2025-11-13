package com.shutu.oauth.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubUserInfo implements OAuthUserInfo {

    @JsonProperty("id")
    private String id;

    // GitHub的用户名
    @JsonProperty("login")
    private String username;

    // GitHub的昵称或真实姓名
    @JsonProperty("name")
    private String name;

    @JsonProperty("avatar_url")
    private String avatarUrl;
    
    @JsonProperty("email")
    private String email;

    private String rawUserInfo;

    @Override
    public String getUniqueId() {
        return this.id;
    }

    @Override
    public String getNickname() {
        // 如果 `name` 字段为空，则使用 `login` 字段作为备用
        return this.name != null ? this.name : this.username;
    }

    @Override
    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    @Override
    public String getPlatform() {
        return "github";
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