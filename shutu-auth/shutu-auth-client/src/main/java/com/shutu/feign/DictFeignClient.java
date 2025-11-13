
package com.shutu.feign;
import com.shutu.commons.tools.constant.ServiceConstant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

///**
// * 字典接口
// *
// * @author jiujingz@126.com
// */
//@FeignClient(name = ServiceConstant.RENREN_ADMIN_SERVER, contextId = "DictFeignClient", fallbackFactory = DictFeignClientFallbackFactory.class)
//public interface DictFeignClient {
//
//    /**
//     * 字典类型列表
//     */
//    @GetMapping("sys/dict/type/list")
//    Result<List<SysDictTypeDTO>> getDictTypeList();
//
//}