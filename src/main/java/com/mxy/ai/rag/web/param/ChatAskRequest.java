package com.mxy.ai.rag.web.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 智能问答请求参数
 * 用于接收智能问答接口的请求参数
 *
 * @author Mxy
 */
@Data
@Schema(description = "智能问答请求参数")
public class ChatAskRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "会话ID", example = "1")
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    @Schema(description = "用户问题", example = "什么是人工智能？")
    @NotBlank(message = "问题内容不能为空")
    @Size(max = 2000, message = "问题内容长度不能超过2000个字符")
    private String question;

    @Schema(description = "检索文档数量", example = "5")
    @Min(value = 1, message = "检索文档数量不能小于1")
    @Max(value = 20, message = "检索文档数量不能大于20")
    private Integer topK = 5;
}