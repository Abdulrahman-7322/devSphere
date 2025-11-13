package com.shutu.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * QQ第三方登录工具类
 */
public class QQLoginUtil {

    //-------------------------------------QQ第三方登录信息------------------------------------------------//
    
    /**
     * 获取Authorization Code的URL
     * 文档：https://wiki.connect.qq.com/使用authorization_code获取access_token
     */
    public static String AUTHORIZATION_CODE_URL = "https://graph.qq.com/oauth2.0/authorize?response_type=code&client_id=APPID&redirect_uri=REDIRECT_URI&state=STATE";
    
    /**
     * 获取Access Token的URL
     */
    public static String ACCESS_TOKEN_URL = "https://graph.qq.com/oauth2.0/token?grant_type=authorization_code&client_id=APPID&client_secret=APPKEY&code=CODE&redirect_uri=REDIRECT_URI";
    
    /**
     * 获取用户OpenID的URL
     */
    public static String OPENID_URL = "https://graph.qq.com/oauth2.0/me?access_token=ACCESS_TOKEN";
    
    /**
     * 获取用户信息的URL
     */
    public static String USER_INFO_URL = "https://graph.qq.com/user/get_user_info?access_token=ACCESS_TOKEN&oauth_consumer_key=APPID&openid=OPENID";
    
    /**
     * 对URL进行编码
     */
    public static String encodeURL(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return url;
        }
    }
} 