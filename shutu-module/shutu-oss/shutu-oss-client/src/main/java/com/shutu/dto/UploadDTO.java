

package com.shutu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 上传信息
 *
 * @author jiujingz@126.com
 * @since 1.1.0
 */
@Data
@Schema(description = "上传信息")
public class UploadDTO {
    @Schema(description = "文件URL")
    private String url;
    @Schema(description = "文件大小，单位字节")
    private Long size;

}
