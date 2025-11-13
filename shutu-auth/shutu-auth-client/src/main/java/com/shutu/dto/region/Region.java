package com.shutu.dto.region;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 地区管理
 *
 * @author jiujingz@126.com
 */
@Data
@Schema(description = "地区管理")
public class Region implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "地区ID")
    private Long id;

    @JsonIgnore
    private Long pid;

    @Schema(description = "名称")
    private String name;
}
