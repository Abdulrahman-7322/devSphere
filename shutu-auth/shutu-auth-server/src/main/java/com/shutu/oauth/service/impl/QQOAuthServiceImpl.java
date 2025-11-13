package com.shutu.oauth.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shutu.commons.tools.exception.CommonException;
import com.shutu.oauth.config.OAuthProperties;
import com.shutu.oauth.model.entity.OAuthUserInfo;
import com.shutu.oauth.model.entity.QQUserInfo;
import com.shutu.oauth.service.OAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class QQOAuthServiceImpl implements OAuthService {

    private final OAuthProperties oAuthProperties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    // QQ返回的AccessToken是字符串而不是JSON，需要正则提取
    private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile("access_token=([^&]+)");

    @Override
    public String buildAuthorizeUrl(String state) {
        OAuthProperties.ClientProperties client = getClientProperties();
        String redirectUri = URLEncoder.encode(client.getRedirectUri(), StandardCharsets.UTF_8);
        return UriComponentsBuilder.fromHttpUrl(client.getAuthorizeUrl())
                .queryParam("response_type", "code")
                .queryParam("client_id", client.getClientId())
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .build().toUriString();
    }

    @Override
    public OAuthUserInfo getUserInfo(String code) {
        String accessToken = getAccessToken(code);
        String openId = getOpenId(accessToken);

        OAuthProperties.ClientProperties client = getClientProperties();
        String url = UriComponentsBuilder.fromHttpUrl(client.getUserInfoUrl())
                .queryParam("access_token", accessToken)
                .queryParam("oauth_consumer_key", client.getClientId())
                .queryParam("openid", openId)
                .build().toUriString();

        String response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            QQUserInfo userInfo = objectMapper.readValue(response, QQUserInfo.class);
            Assert.notNull(userInfo, "解析QQ用户信息失败");
            if (userInfo.getRet() != 0) {
                log.error("获取QQ用户信息失败: {}", userInfo.getMsg());
                throw new CommonException("获取QQ用户信息失败: " + userInfo.getMsg());
            }
            userInfo.setOpenid(openId);
            userInfo.setRawUserInfo(response);
            return userInfo;
        } catch (JsonProcessingException e) {
            log.error("解析QQ用户信息JSON失败", e);
            throw new CommonException("获取QQ用户信息失败");
        }
    }

    private String getAccessToken(String code) {
        OAuthProperties.ClientProperties client = getClientProperties();
        String url = UriComponentsBuilder.fromHttpUrl(client.getTokenUrl())
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", client.getClientId())
                .queryParam("client_secret", client.getClientSecret())
                .queryParam("code", code)
                .queryParam("redirect_uri", client.getRedirectUri())
                .build().toUriString();

        String response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        Assert.hasText(response, "获取QQ AccessToken响应为空");
        Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new CommonException("无法从QQ响应中提取AccessToken");
    }

    private String getOpenId(String accessToken) {
        OAuthProperties.ClientProperties client = getClientProperties();
        String url = UriComponentsBuilder.fromHttpUrl(client.getOpenIdUrl())
                .queryParam("access_token", accessToken)
                .build().toUriString();

        String response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 响应: callback( {"client_id":"...","openid":"..."} );

        Assert.hasText(response, "获取QQ OpenID响应为空");

        int start = response.indexOf("(");
        int end = response.lastIndexOf(")");
        if (start != -1 && end != -1) {
            String json = response.substring(start + 1, end);
            try {
                Map<String, String> result = objectMapper.readValue(json, Map.class);
                return result.get("openid");
            } catch (JsonProcessingException e) {
                log.error("解析QQ OpenID JSON失败", e);
                throw new CommonException("获取QQ OpenID失败");
            }
        }
        throw new CommonException("无法从QQ响应中提取OpenID");
    }

    @Override
    public String getPlatform() {
        return "qq";
    }

    private OAuthProperties.ClientProperties getClientProperties() {
        return oAuthProperties.getClients().get(getPlatform());
    }
}