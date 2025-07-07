package com.mxy.ai.rag.web.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author mixaoxiaoyu
 * @date 2025-07-07 17:29
 */
@Data
@Schema(description = "获取消息列表")
public class ChatMessagePageRequest {

    @Schema(description = "页码", example = "1", minimum = "1", defaultValue = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;


    @Schema(description = "每页大小", example = "20", minimum = "1", defaultValue = "20")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 20;

    @Schema(description = "会话ID")
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;
}
