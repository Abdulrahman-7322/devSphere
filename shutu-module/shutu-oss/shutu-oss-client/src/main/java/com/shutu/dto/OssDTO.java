package com.shutu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shutu.commons.tools.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 文件上传
 * 
 * @author jiujingz@126.com
 */
@Data
@Schema(description = "文件上传")
public class OssDTO implements Serializable{
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */

	private Long id;

	/**
	 * URL地址
	 */
	private String url;

	/**
	 * 创建者
	 */
	private Long creator;
	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
	private Date createDate;

}