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

    @Schema(description = "用户ID", example = "user123")
    @NotBlank(message = "用户ID不能为空")
    @Size(max = 50, message = "用户ID长度不能超过50个字符")
    private String userId;

    @Schema(description = "用户问题", example = "什么是人工智能？")
    @NotBlank(message = "问题内容不能为空")
    @Size(max = 2000, message = "问题内容长度不能超过2000个字符")
    private String question;

    @Schema(description = "检索文档数量", example = "5")
    @Min(value = 1, message = "检索文档数量不能小于1")
    @Max(value = 20, message = "检索文档数量不能大于20")
    private Integer topK = 5;

    @Schema(description = "是否使用知识库增强", example = "true")
    private Boolean useKnowledgeBase = true;

    @Schema(description = "模型温度参数（0.0-1.0）", example = "0.7")
    @DecimalMin(value = "0.0", message = "温度参数不能小于0.0")
    @DecimalMax(value = "1.0", message = "温度参数不能大于1.0")
    private Double temperature = 0.7;

    @Schema(description = "最大Token数量", example = "2000")
    @Min(value = 100, message = "最大Token数量不能小于100")
    @Max(value = 4000, message = "最大Token数量不能大于4000")
    private Integer maxTokens = 2000;
    
    /**
     * Spring AI 对话ID（可选）
     */
    @Schema(description = "Spring AI 对话ID", example = "conv_123456")
    private String conversationId;
    
    /**
     * 是否启用记忆功能（可选，默认true）
     */
    @Schema(description = "是否启用记忆功能", example = "true")
    private Boolean enableMemory = true;
    
    /**
     * 上下文权重（可选，默认1.0）
     */
    @Schema(description = "上下文权重", example = "1.0")
    @DecimalMin(value = "0.0", message = "上下文权重不能小于0.0")
    @DecimalMax(value = "2.0", message = "上下文权重不能大于2.0")
    private Double contextWeight = 1.0;
}