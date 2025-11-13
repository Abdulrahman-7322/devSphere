package com.shutu.oauth.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shutu.oauth.config.OAuthProperties;
import com.shutu.oauth.model.entity.AlipayUserInfo;
import com.shutu.oauth.model.entity.OAuthUserInfo;
import com.shutu.oauth.service.OAuthService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayOAuthServiceImpl implements OAuthService {

    private final OAuthProperties oAuthProperties;
    private final ObjectMapper objectMapper;
    private AlipayClient alipayClient;

    @PostConstruct
    public void init() {
        OAuthProperties.ClientProperties client = getClientProperties();
        this.alipayClient = new DefaultAlipayClient(
                client.getGatewayUrl(),
                client.getClientId(),
                client.getPrivateKey(),
                "json",
                client.getCharset(),
                client.getAlipayPublicKey(),
                client.getSignType()
        );
    }
    
    @Override
    public String buildAuthorizeUrl(String state) {
        OAuthProperties.ClientProperties client = getClientProperties();
        return UriComponentsBuilder.fromHttpUrl(client.getAuthorizeUrl())
                .queryParam("app_id", client.getClientId())
                .queryParam("scope", client.getScope())
                .queryParam("redirect_uri", client.getRedirectUri())
                .queryParam("state", state)
                .build(true)
                .toUriString();
    }

    @Override
    public OAuthUserInfo getUserInfo(String code) throws AlipayApiException, JsonProcessingException {
        // 1. 使用授权码(code)换取accessToken
        AlipaySystemOauthTokenRequest tokenRequest = new AlipaySystemOauthTokenRequest();
        tokenRequest.setGrantType("authorization_code");
        tokenRequest.setCode(code);
        
        AlipaySystemOauthTokenResponse tokenResponse = alipayClient.execute(tokenRequest);
        if (!tokenResponse.isSuccess()) {
            log.error("支付宝AccessToken获取失败: {}", tokenResponse.getSubMsg());
            throw new AlipayApiException("获取支付宝AccessToken失败: " + tokenResponse.getSubMsg());
        }
        String accessToken = tokenResponse.getAccessToken();

        // 2. 使用accessToken获取用户信息
        AlipayUserInfoShareRequest userInfoRequest = new AlipayUserInfoShareRequest();
        AlipayUserInfoShareResponse userInfoResponse = alipayClient.execute(userInfoRequest, accessToken);

        if (!userInfoResponse.isSuccess()) {
            log.error("支付宝用户信息获取失败: {}", userInfoResponse.getSubMsg());
            throw new AlipayApiException("获取支付宝用户信息失败: " + userInfoResponse.getSubMsg());
        }

        // 3. 将返回的JSON字符串转换为我们的UserInfo对象
        String userInfoJson = userInfoResponse.getBody();
        AlipayUserInfo userInfo = objectMapper.readValue(userInfoJson, AlipayUserInfo.class);
        userInfo.setRawUserInfo(userInfoJson);

        Assert.notNull(userInfo, "解析支付宝用户信息失败");
        return userInfo;
    }

    @Override
    public String getPlatform() {
        return "alipay";
    }

    private OAuthProperties.ClientProperties getClientProperties() {
        return oAuthProperties.getClients().get(getPlatform());
    }
}