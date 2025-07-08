package com.mxy.ai.rag.web.controller;

import com.mxy.ai.rag.dto.UserChangePasswordDTO;
import com.mxy.ai.rag.dto.UserLoginDTO;
import com.mxy.ai.rag.dto.UserRegisterDTO;
import com.mxy.ai.rag.service.UserService;
import com.mxy.ai.rag.util.UserContextUtil;
import com.mxy.ai.rag.web.param.UserChangePasswordRequest;
import com.mxy.ai.rag.web.param.UserLoginRequest;
import com.mxy.ai.rag.web.param.UserRegisterRequest;
import com.mxy.ai.rag.web.vo.ApiResult;
import com.mxy.ai.rag.web.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户管理控制器
 * 提供用户注册、登录、密码修改等REST API接口
 *
 * @author Mxy
 */
@Tag(name = "用户管理", description = "提供用户注册、登录、密码修改等功能")
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param request 注册请求参数
     * @return 注册结果
     */
    @Operation(summary = "用户注册", description = "用户注册接口，创建新用户账号")
    @PostMapping("/register")
    public ApiResult<UserVO> register(
            @Parameter(description = "注册请求参数", required = true)
            @Valid @RequestBody UserRegisterRequest request) {
        try {
            logger.info("接收用户注册请求: username={}", request.getUsername());

            // 转换为DTO
            UserRegisterDTO dto = new UserRegisterDTO();
            BeanUtils.copyProperties(request, dto);

            UserVO result = userService.register(dto);
            return ApiResult.success("注册成功", result);
        } catch (Exception e) {
            logger.error("用户注册失败: {}", e.getMessage(), e);
            return ApiResult.error("用户注册失败: " + e.getMessage());
        }
    }

    /**
     * 用户登录
     *
     * @param request 登录请求参数
     * @return 登录结果
     */
    @Operation(summary = "用户登录", description = "用户登录接口，验证用户身份")
    @PostMapping("/login")
    public ApiResult<UserVO> login(
            @Parameter(description = "登录请求参数", required = true)
            @Valid @RequestBody UserLoginRequest request) {
        try {
            logger.info("接收用户登录请求: username={}", request.getUsername());

            // 转换为DTO
            UserLoginDTO dto = new UserLoginDTO();
            BeanUtils.copyProperties(request, dto);

            UserVO result = userService.login(dto);
            return ApiResult.success("登录成功", result);
        } catch (Exception e) {
            logger.error("用户登录失败: {}", e.getMessage(), e);
            return ApiResult.error("用户登录失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码
     *
     * @param request 修改密码请求参数
     * @return 修改结果
     */
    @Operation(summary = "修改密码", description = "用户修改密码接口")
    @PostMapping("/change-password")
    public ApiResult<Void> changePassword(
            @Parameter(description = "修改密码请求参数", required = true)
            @Valid @RequestBody UserChangePasswordRequest request) {
        try {
            // 从用户上下文获取当前用户ID
            String currentUserId = UserContextUtil.getCurrentUserId();
            logger.info("接收修改密码请求: userId={}", currentUserId);

            // 转换为DTO
            UserChangePasswordDTO dto = new UserChangePasswordDTO();
            BeanUtils.copyProperties(request, dto);
            dto.setUserId(currentUserId);

            userService.changePassword(dto);
            return ApiResult.success();
        } catch (Exception e) {
            logger.error("修改密码失败: {}", e.getMessage(), e);
            return ApiResult.error("修改密码失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的基本信息")
    @GetMapping("/info")
    public ApiResult<UserVO> getUserInfo() {
        try {
            // 从用户上下文获取当前用户ID
            String currentUserId = UserContextUtil.getCurrentUserId();
            logger.info("获取用户信息: userId={}", currentUserId);

            UserVO result = userService.getUserInfo(currentUserId);
            return ApiResult.success(result);
        } catch (Exception e) {
            logger.error("获取用户信息失败: {}", e.getMessage(), e);
            return ApiResult.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户名是否可用
     *
     * @param username 用户名
     * @return 检查结果
     */
    @Operation(summary = "检查用户名", description = "检查用户名是否已被使用")
    @GetMapping("/check-username")
    public ApiResult<Boolean> checkUsername(
            @Parameter(description = "用户名", required = true)
            @RequestParam String username) {
        try {
            logger.info("检查用户名可用性: username={}", username);

            boolean exists = userService.isUsernameExists(username);
            return ApiResult.success("检查完成", !exists); // 返回true表示可用，false表示已存在
        } catch (Exception e) {
            logger.error("检查用户名失败: {}", e.getMessage(), e);
            return ApiResult.error("检查用户名失败: " + e.getMessage());
        }
    }
}