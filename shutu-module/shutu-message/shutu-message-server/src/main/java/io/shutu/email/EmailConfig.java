package io.shutu.email;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 邮件配置信息
 *
 * @author jiujingz@126.com
 */
@Data
@Schema(description = "邮件配置信息")
public class EmailConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "SMTP")
    @NotBlank(message="{email.smtp.require}")
    private String smtp;

    @Schema(description = "端口号")
    @NotNull(message="{email.port.require}")
    private Integer port;

    @Schema(description = "邮箱账号")
    @NotBlank(message="{email.username.require}")
    private String username;

    @Schema(description = "邮箱密码")
    @NotBlank(message="{email.password.require}")
    private String password;

    @Schema(description = "发件人昵称")
    @NotBlank(message="{email.password.require}")
    private String sendNickName;

    public String getSmtp() {
        return smtp;
    }

    public void setSmtp(String smtp) {
        this.smtp = smtp;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSendNickName() {
        return sendNickName;
    }

    public void setSendNickName(String sendNickName) {
        this.sendNickName = sendNickName;
    }
}