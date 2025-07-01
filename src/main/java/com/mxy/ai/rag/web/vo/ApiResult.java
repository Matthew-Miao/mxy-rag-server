package com.mxy.ai.rag.web.vo;

import lombok.Data;

/**
 * 统一API响应结果类
 * 用于封装所有接口的返回结果
 */
@Data
public class ApiResult<T> {

    /**
     * 响应码（200表示成功，其他表示失败）
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 请求时间戳
     */
    private Long timestamp;

    /**
     * 构造函数
     */
    public ApiResult() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 构造函数
     * @param code 响应码
     * @param message 响应消息
     * @param data 响应数据
     */
    public ApiResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应（无数据）
     * @return ApiResult
     */
    public static <T> ApiResult<T> success() {
        return new ApiResult<>(200, "操作成功", null);
    }

    /**
     * 成功响应（带数据）
     * @param data 响应数据
     * @return ApiResult
     */
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(200, "操作成功", data);
    }

    /**
     * 成功响应（带消息和数据）
     * @param message 响应消息
     * @param data 响应数据
     * @return ApiResult
     */
    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>(200, message, data);
    }

    /**
     * 失败响应
     * @param message 错误消息
     * @return ApiResult
     */
    public static <T> ApiResult<T> error(String message) {
        return new ApiResult<>(500, message, null);
    }

    /**
     * 失败响应（带错误码）
     * @param code 错误码
     * @param message 错误消息
     * @return ApiResult
     */
    public static <T> ApiResult<T> error(Integer code, String message) {
        return new ApiResult<>(code, message, null);
    }

    /**
     * 参数错误响应
     * @param message 错误消息
     * @return ApiResult
     */
    public static <T> ApiResult<T> badRequest(String message) {
        return new ApiResult<>(400, message, null);
    }

    /**
     * 未授权响应
     * @param message 错误消息
     * @return ApiResult
     */
    public static <T> ApiResult<T> unauthorized(String message) {
        return new ApiResult<>(401, message, null);
    }

    /**
     * 禁止访问响应
     * @param message 错误消息
     * @return ApiResult
     */
    public static <T> ApiResult<T> forbidden(String message) {
        return new ApiResult<>(403, message, null);
    }

    /**
     * 资源不存在响应
     * @param message 错误消息
     * @return ApiResult
     */
    public static <T> ApiResult<T> notFound(String message) {
        return new ApiResult<>(404, message, null);
    }
}