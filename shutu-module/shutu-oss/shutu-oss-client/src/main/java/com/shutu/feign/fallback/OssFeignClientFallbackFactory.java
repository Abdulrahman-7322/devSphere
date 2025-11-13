package com.shutu.feign.fallback;

import com.shutu.commons.tools.utils.Result;
import com.shutu.feign.OssFeignClient;
import com.shutu.dto.OssDTO;
import com.shutu.dto.UploadDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * OSS FallbackFactory
 *
 * @author jiujingz@126.com
 */
@Slf4j
@Component
public class OssFeignClientFallbackFactory implements FallbackFactory<OssFeignClient> {

    @Override
    public OssFeignClient create(Throwable throwable) {
        log.error("{}", throwable);
        return new OssFeignClient() {
            @Override
            public Result<UploadDTO> upload(MultipartFile file) {
                return new Result<>();
            }

            @Override
            public Result<OssDTO> selectInfoById(String id) {
                return new Result<>();
            }
        };
    }
}
