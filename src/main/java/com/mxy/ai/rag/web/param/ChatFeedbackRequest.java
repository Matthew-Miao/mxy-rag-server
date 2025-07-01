package com.mxy.ai.rag.web.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 用户反馈请求参数
 * 用于接收用户对AI回答的反馈信息
 * 
 * @author Mxy
 */
@Data
@Schema(description = "用户反馈请求参数")
public class ChatFeedbackRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "消息ID", example = "1")
    @NotNull(message = "消息ID不能为空")
    private Long messageId;

    @Schema(description = "用户ID", example = "user123")
    @NotBlank(message = "用户ID不能为空")
    @Size(max = 50, message = "用户ID长度不能超过50个字符")
    private String userId;

    @Schema(description = "用户评分（1-5分）", example = "5")
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分不能小于1分")
    @Max(value = 5, message = "评分不能大于5分")
    private Integer rating;

    @Schema(description = "反馈内容（可选）", example = "回答很准确，很有帮助")
    @Size(max = 500, message = "反馈内容长度不能超过500个字符")
    private String feedback;
}