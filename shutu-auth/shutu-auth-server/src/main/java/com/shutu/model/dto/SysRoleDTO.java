package com.shutu.model.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.shutu.commons.tools.utils.DateUtils;
import com.shutu.commons.tools.validator.group.AddGroup;
import com.shutu.commons.tools.validator.group.DefaultGroup;
import com.shutu.commons.tools.validator.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 角色管理
 
 */
@Data
@Schema(description = "角色管理")
public class SysRoleDTO implements Serializable {
    private static final long serialVersionUID = 1L;
 
    @Schema(description = "id")
    @Null(message = "{id.null}", groups = AddGroup.class)
    @NotNull(message = "{id.require}", groups = UpdateGroup.class)
    private Long id;

    @Schema(description = "角色名称")
    @NotBlank(message = "{sysrole.name.require}", groups = DefaultGroup.class)
    private String name;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;

    @Schema(description = "菜单ID列表")
    private List<Long> menuIdList;

    @Schema(description = "部门ID列表")
    private List<Long> deptIdList;

}