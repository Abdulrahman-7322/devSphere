

package io.shutu.dto;

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

/**
 * 邮件模板
 *
 * @author jiujingz@126.com
 */
@Data
@Schema(description = "邮件模板")
public class SysMailTemplateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @Null(message = "{id.null}", groups = AddGroup.class)
    @NotNull(message = "{id.require}", groups = UpdateGroup.class)
    private Long id;

    @Schema(description = "模板名称")
    @NotBlank(message = "{mail.name.require}", groups = DefaultGroup.class)
    private String name;

    @Schema(description = "邮件主题")
    @NotBlank(message = "{mail.subject.require}", groups = DefaultGroup.class)
    private String subject;

    @Schema(description = "邮件正文")
    @NotBlank(message = "{mail.content.require}", groups = DefaultGroup.class)
    private String content;

    @Schema(description = "创建时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;

}