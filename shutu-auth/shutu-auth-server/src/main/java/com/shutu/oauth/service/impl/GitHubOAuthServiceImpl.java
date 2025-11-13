package com.shutu.oauth.service.impl;

import com.shutu.oauth.config.OAuthProperties;
import com.shutu.oauth.model.entity.GitHubUserInfo;
import com.shutu.oauth.model.entity.OAuthUserInfo;
import com.shutu.oauth.service.OAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubOAuthServiceImpl implements OAuthService {

    private final WebClient webClient;
    private final OAuthProperties oAuthProperties;

    @Override
    public String buildAuthorizeUrl(String state) {
        OAuthProperties.ClientProperties client = getClientProperties();
        return UriComponentsBuilder.fromHttpUrl(client.getAuthorizeUrl())
                .queryParam("client_id", client.getClientId())
                .queryParam("redirect_uri", client.getRedirectUri())
                .queryParam("scope", client.getScope())
                .queryParam("state", state)
                .build(true)
                .toUriString();
    }

    @Override
    public OAuthUserInfo getUserInfo(String code) {
        String accessToken = getAccessToken(code);
        
        GitHubUserInfo userInfo = webClient.get()
                .uri(getClientProperties().getUserInfoUrl())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(GitHubUserInfo.class)
                .doOnError(e -> log.error("获取GitHub用户信息失败", e))
                .block();

        Assert.notNull(userInfo, "获取GitHub用户信息失败");
        return userInfo;
    }

    private String getAccessToken(String code) {
        OAuthProperties.ClientProperties client = getClientProperties();
        String url = UriComponentsBuilder.fromHttpUrl(client.getTokenUrl())
                .queryParam("client_id", client.getClientId())
                .queryParam("client_secret", client.getClientSecret())
                .queryParam("code", code)
                .queryParam("redirect_uri", client.getRedirectUri())
                .build(true)
                .toUriString();

        Map<String, Object> response = webClient.post()
                .uri(url)
                // GitHub要求在header中指定接受JSON格式，否则默认返回url-encoded字符串
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(e -> log.error("获取GitHub AccessToken失败", e))
                .block();

        Assert.notNull(response, "获取GitHub AccessToken失败");
        return (String) response.get("access_token");
    }

    @Override
    public String getPlatform() {
        return "github";
    }

    private OAuthProperties.ClientProperties getClientProperties() {
        return oAuthProperties.getClients().get(getPlatform());
    }
}