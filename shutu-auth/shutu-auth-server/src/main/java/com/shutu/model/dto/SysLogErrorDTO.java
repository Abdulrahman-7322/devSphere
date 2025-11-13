package com.shutu.model.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.shutu.commons.tools.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 异常日志
 */
@Data
@Schema(description = "异常日志")
public class SysLogErrorDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;
    @Schema(description = "模块名称，如：sys")
    private String module;
    @Schema(description = "请求URI")
    private String requestUri;
    @Schema(description = "请求方式")
    private String requestMethod;
    @Schema(description = "请求参数")
    private String requestParams;
    @Schema(description = "用户代理")
    private String userAgent;
    @Schema(description = "操作IP")
    private String ip;
    @Schema(description = "异常信息")
    private String errorInfo;
    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;

}