package com.mxy.ai.rag.service.memory;

import com.mxy.ai.rag.datasource.dao.ChatMessagesDAO;
import com.mxy.ai.rag.datasource.dao.ChatSessionsDAO;
import com.mxy.ai.rag.datasource.entity.ChatSessionsDO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.messages.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CustomChatMemoryRepository测试类
 * 验证与Spring AI标准的一致性
 *
 * @author Mxy
 */
class CustomChatMemoryRepositoryTest {

    @Mock
    private ChatSessionsDAO chatSessionsDAO;

    @Mock
    private ChatMessagesDAO chatMessagesDAO;

    @InjectMocks
    private CustomChatMemoryRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveAllWithValidInput() {
        // 准备测试数据
        String conversationId = "test-conversation-123";
        List<Message> messages = Arrays.asList(
                new UserMessage("Hello"),
                new AssistantMessage("Hi there!"),
                new SystemMessage("System prompt"),
                new ToolResponseMessage(List.of())
        );

        // Mock会话存在
        ChatSessionsDO mockSession = new ChatSessionsDO();
        mockSession.setId(1L);
        mockSession.setConversationId(conversationId);
        when(chatSessionsDAO.getOne(any())).thenReturn(mockSession);

        // 执行测试
        assertDoesNotThrow(() -> repository.saveAll(conversationId, messages));

        // 验证调用
        verify(chatMessagesDAO, times(1)).update(any(), any()); // 删除现有消息
        verify(chatMessagesDAO, times(1)).saveBatch(any()); // 保存新消息
        verify(chatSessionsDAO, times(1)).update(any(), any()); // 更新会话活动
    }

    @Test
    void testSaveAllWithNullConversationId() {
        // 测试空对话ID
        List<Message> messages = Arrays.asList(new UserMessage("Hello"));
        
        assertThrows(IllegalArgumentException.class, () -> {
            repository.saveAll(null, messages);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            repository.saveAll("", messages);
        });
    }

    @Test
    void testSaveAllWithNullMessages() {
        // 测试空消息列表
        String conversationId = "test-conversation-123";
        
        assertThrows(IllegalArgumentException.class, () -> {
            repository.saveAll(conversationId, null);
        });
    }

    @Test
    void testFindByConversationIdWithNullId() {
        // 测试空对话ID
        assertThrows(IllegalArgumentException.class, () -> {
            repository.findByConversationId(null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            repository.findByConversationId("");
        });
    }

    @Test
    void testDeleteByConversationIdWithNullId() {
        // 测试空对话ID
        assertThrows(IllegalArgumentException.class, () -> {
            repository.deleteByConversationId(null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            repository.deleteByConversationId("");
        });
    }

    @Test
    void testMessageTypeMapping() {
        // 测试消息类型映射是否正确
        String conversationId = "test-conversation-123";
        List<Message> messages = Arrays.asList(
                new UserMessage("User message"),
                new AssistantMessage("Assistant message"),
                new SystemMessage("System message"),
                new ToolResponseMessage(List.of())
        );

        // Mock会话存在
        ChatSessionsDO mockSession = new ChatSessionsDO();
        mockSession.setId(1L);
        mockSession.setConversationId(conversationId);
        when(chatSessionsDAO.getOne(any())).thenReturn(mockSession);

        // 执行测试
        assertDoesNotThrow(() -> repository.saveAll(conversationId, messages));

        // 验证所有消息类型都被正确处理
        verify(chatMessagesDAO, times(1)).saveBatch(argThat(list -> 
                list.size() == 4 && 
                list.stream().anyMatch(msg -> "USER".equals(msg.getMessageType())) &&
                list.stream().anyMatch(msg -> "ASSISTANT".equals(msg.getMessageType())) &&
                list.stream().anyMatch(msg -> "SYSTEM".equals(msg.getMessageType())) &&
                list.stream().anyMatch(msg -> "TOOL".equals(msg.getMessageType()))
        ));
    }
}