

package com.shutu.feign;

import com.shutu.commons.security.user.UserDetail;
import com.shutu.commons.tools.constant.ServiceConstant;
import com.shutu.commons.tools.utils.Result;
import com.shutu.dto.SysUserDTO;
import com.shutu.feign.fallback.UserFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

/**
 * 用户接口
 */
@FeignClient(name = ServiceConstant.SHUTU_AUTH_SERVER, contextId = "UserFeignClient", fallbackFactory = UserFeignClientFallbackFactory.class)
public interface UserFeignClient {

    /**
     * 根据用户ID，获取用户信息
     */
    @GetMapping("auth/user/getById")
    Result<UserDetail> getById(@RequestParam("id") Long id);

    /**
     * 根据用户ID，获取角色Id列表
     */
    @GetMapping("auth/role/getRoleIdList")
    Result<List<Long>> getRoleIdList(@RequestParam("userId") Long userId);

    /**
     * 根据角色ID,查询用户ID列表
     */
    @PostMapping("auth/user/getUserIdListByRoleIdList")
    Result<List<Long>> getUserIdListByRoleIdList(@RequestParam List<Long> ids);

    /**
     * 根据岗位ID,查询用户ID列表
     */
    @PostMapping("auth/user/getUserIdListByPostIdList")
    Result<List<Long>> getUserIdListByPostIdList(@RequestParam List<Long> ids);

    /**
     * 根据部门ID,查询部门领导列表
     */
    @PostMapping("auth/user/getLeaderIdListByDeptIdList")
    Result<List<Long>> getLeaderIdListByDeptIdList(@RequestParam List<Long> ids);

    /**
     * 根据用户ID,查询部门领导ID
     */
    @PostMapping("auth/user/getLeaderIdListByUserId")
    Result<Long> getLeaderIdListByUserId(@RequestParam("userId") Long userId);


    /**
     * 根据用户ID列表，批量查询用户信息
     */
    @PostMapping("auth/user/listByIds")
    Result<List<SysUserDTO>> listByIds(@RequestBody List<Long> ids);

    /**
     * 根据用户名查询用户信息
     */
    @PostMapping("auth/user/getByUsername")
    Result<SysUserDTO> getByUsername(@RequestParam("username")String username);
}