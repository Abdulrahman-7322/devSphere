package com.shutu.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户Token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户Token")
public class UserTokenDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "access_token")
    @JsonProperty(value = "access_token")
    private String accessToken;

    @Schema(description = "refresh_token")
    @JsonProperty(value = "refresh_token")
    private String refreshToken;

    @Schema(description = "access_token过期时间")
    @JsonProperty(value = "access_token_expire")
    private Date accessTokenExpire;

    @Schema(description = "refresh_token过期时间")
    @JsonProperty(value = "refresh_token_expire")
    private Date refreshTokenExpire;

}
