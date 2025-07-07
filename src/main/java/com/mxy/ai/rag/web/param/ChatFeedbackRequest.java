package com.mxy.ai.rag.web.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户反馈请求参数
 * 用于接收用户对AI回答的反馈信息
 *
 * @author Mxy
 */
@Data
public class ChatFeedbackRequest {
    @Schema(description = "消息ID", example = "1")
    @NotNull(message = "消息ID不能为空")
    private Long messageId;

    @Schema(description = "用户评分（1-5分）", example = "5")
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分不能小于1分")
    @Max(value = 5, message = "评分不能大于5分")
    private Integer rating;
}
