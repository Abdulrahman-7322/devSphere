package com.shutu.cloud;

import com.shutu.validator.group.LocalGroup;
import com.shutu.validator.group.MinioGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;
import java.io.Serializable;

/**
 * 云存储配置信息
 */
@Data
@Schema(description = "云存储配置信息")
public class CloudStorageConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "类型 1：七牛  2：阿里云  3：腾讯云   4：FastDFS   5：本地上传    6：minio")
    @Range(min = 1, max = 6, message = "{oss.type.range}")
    private Integer type;

    @Schema(description = "本地上传绑定的域名")
    @NotBlank(message = "{local.domain.require}", groups = LocalGroup.class)
    @URL(message = "{local.domain.url}", groups = LocalGroup.class)
    private String localDomain;

    @Schema(description = "本地上传路径前缀")
    private String localPrefix;

    @Schema(description = "本地上传存储目录")
    @NotBlank(message = "{local.path.url}", groups = LocalGroup.class)
    private String localPath;

    @Schema(description = "Minio EndPoint")
    @NotBlank(message = "{minio.endPoint.require}", groups = MinioGroup.class)
    private String minioEndPoint;

    @Schema(description = "accessKey")
    @NotBlank(message = "{minio.accesskey.require}", groups = MinioGroup.class)
    private String minioAccessKey;

    @Schema(description = "secretKey")
    @NotBlank(message = "{minio.secretkey.require}", groups = MinioGroup.class)
    private String minioSecretKey;

    @Schema(description = "BucketName")
    @NotBlank(message = "{minio.bucketname.require}", groups = MinioGroup.class)
    private String minioBucketName;

    @Schema(description = "MinIO上传路径前缀")
    private String minioPrefix;

}
