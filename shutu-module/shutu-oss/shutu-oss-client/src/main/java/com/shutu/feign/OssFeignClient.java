package com.shutu.feign;

import com.shutu.commons.tools.utils.Result;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import com.shutu.dto.OssDTO;
import com.shutu.dto.UploadDTO;
import com.shutu.feign.fallback.OssFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import static com.shutu.commons.tools.constant.ServiceConstant.SHUTU_OSS_SERVER;


/**
 * OSS
 */
@FeignClient(name = SHUTU_OSS_SERVER,  contextId = "OssFeignClient",fallbackFactory = OssFeignClientFallbackFactory.class, configuration = OssFeignClient.MultipartSupportConfig.class)
public interface OssFeignClient {
    /**
     * 文件上传
     * @param file 文件
     * @return 返回路径
     */
    @PostMapping(value = "oss/file/upload", produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<UploadDTO> upload(@RequestPart("file") MultipartFile file);

    class MultipartSupportConfig {
        @Bean
        public Encoder feignFormEncoder() {
            return new SpringFormEncoder();
        }
    }

    @GetMapping(value = "oss/file/{id}")
    Result<OssDTO> selectInfoById(@PathVariable("id") String id);

}
