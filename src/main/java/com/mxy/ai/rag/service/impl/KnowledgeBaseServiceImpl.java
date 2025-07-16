package com.mxy.ai.rag.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.mxy.ai.rag.service.KnowledgeBaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库服务实现类
 * @author Mxy
 */
@Service
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseServiceImpl.class);
    
    /**
     * 系统提示词：指导AI智能地处理不同类型的问题
     */
    private static final String SYSTEM_PROMPT = "你是一个智能助手。请始终使用中文回答用户的问题。当回答用户问题时，请遵循以下策略：\n" +
            "1. 对于基础知识问题（如数学计算、常识问题等），直接使用你的通用知识准确回答\n" +
            "2. 对于专业或特定领域的问题，优先从向量数据库中检索相关知识来回答\n" +
            "3. 如果向量数据库中没有找到相关信息，请从聊天记忆中寻找之前讨论过的相关内容\n" +
            "4. 如果以上都没有相关信息，请基于你的通用知识给出准确、有帮助的回答\n" +
            "5. 只有在确实无法回答时，才诚实地告知用户并建议他们提供更多信息\n" +
            "请确保回答准确、相关且有帮助。不要因为向量数据库中没有信息就拒绝回答基础问题。所有回答都必须使用中文。";

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    /**
     * 构造函数：初始化知识库服务
     * 不再使用Spring AI的MessageChatMemoryAdvisor，改为手动管理聊天记忆
     * 
     * @param vectorStore 向量存储
     * @param chatModel 聊天模型
     * @param messageWindowChatMemory 消息窗口聊天记忆
     */
    public KnowledgeBaseServiceImpl(VectorStore vectorStore, @Qualifier("dashscopeChatModel")ChatModel chatModel,
                                    MessageWindowChatMemory messageWindowChatMemory) {
        this.vectorStore = vectorStore;
                
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build(),MessageChatMemoryAdvisor.builder(messageWindowChatMemory).build())
                .defaultOptions(DashScopeChatOptions.builder().withTopP(0.7).build())
                .build();
    }

    /**
     * 相似性搜索
     * @param query 查询字符串
     * @param topK 返回的相似文档数量
     * @return
     */
    @Override
    public List<Document> similaritySearch(String query, int topK) {
        Assert.hasText(query, "查询不能为空");

        logger.info("执行相似性搜索: query={}, businessType={}, topK={}", query, topK);

        // 创建业务类型过滤器
        SearchRequest searchRequest = SearchRequest.builder().query(query).topK(topK).build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);
        logger.info("相似性搜索完成，找到 {} 个相关文档", results.size());

        return results;
    }


    /**
     * 将文本内容插入到向量存储中。
     *
     * @param content      要插入的文本内容
     */
    @Override
    public void insertTextContent(String content) {
        Assert.hasText(content, "文本内容不能为空");
        logger.info("插入文本内容到向量存储: contentLength={}",  content.length());
        // 创建文档并设置ID和元数据
        Document document = new Document(content);
        // 使用文本分割器处理长文本
        List<Document> splitDocuments = new TokenTextSplitter().apply(List.of(document));

        // 分批添加到向量存储（每批最多10个文档）
        addDocumentsInBatches(splitDocuments);

        logger.info("文本内容插入完成: 生成文档片段数: {}",  splitDocuments.size());
    }

    /**
     * 根据文件类型加载文件到向量存储中。
     *
     * @param file         要上传的文件
     * @return 处理结果消息
     */
    @Override
    public String loadFileByType(MultipartFile file) {
        Assert.notNull(file, "文件不能为空");

        logger.info("开始处理文件上传: fileName={}, fileSize={}", file.getOriginalFilename(),  file.getSize());

        try {
            // 创建临时文件
            Path tempFile = Files.createTempFile("upload_", "_" + file.getOriginalFilename());
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            List<Document> documents;
            String fileName = file.getOriginalFilename();

            // 根据文件类型选择合适的文档读取器
            if (fileName.toLowerCase().endsWith(".pdf")) {
                // 使用PDF读取器
                PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(tempFile.toUri().toString());
                documents = pdfReader.get();
                logger.info("使用PDF读取器处理文件: {}", fileName);
            } else {
                // 使用Tika读取器处理其他类型文件
                TikaDocumentReader tikaReader = new TikaDocumentReader(tempFile.toUri().toString());
                documents = tikaReader.get();
                logger.info("使用Tika读取器处理文件: {}", fileName);
            }
            // 分批添加文档到向量存储（每批最多10个文档）
            addDocumentsInBatches(documents);

            // 清理临时文件
            Files.deleteIfExists(tempFile);

            logger.info("文件处理完成: fileName={}, documentsCount={}", fileName,  documents.size());

            return String.format("成功处理文件 %s，共生成 %d 个文档片段", fileName, documents.size());

        } catch (IOException e) {
            logger.error("文件处理失败: fileName={}, error={}", file.getOriginalFilename(),  e.getMessage(), e);
            return "文件处理失败: " + e.getMessage();
        }
    }

    /**
     * 与知识库进行对话
     * 手动管理聊天记忆，构建完整的对话历史传给大模型
     * 
     * @param query 用户查询
     * @param conversationId 对话ID
     * @param topK 检索文档数量
     * @return 回答内容
     */
    @Override
    public String chatWithKnowledge(String query, String conversationId, int topK) {
        Assert.hasText(query, "查询问题不能为空");
        logger.info("开始知识库对话，查询: '{}', conversationId: {}", query, conversationId);
        
        try {
            String prompt = getRagStr(query, topK);
            
            // 3. 调用LLM生成回答
            String answer = chatClient.prompt(prompt)
                    .system(SYSTEM_PROMPT)
                    .user(query)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call().content();

            logger.info("知识库对话完成，查询: '{}'", query);
            return answer;
            
        } catch (Exception e) {
            logger.error("知识库对话失败，查询: '{}'", query, e);
            return "对话过程中发生错误: " + e.getMessage();
        }
    }

    /**
     * 流式知识库对话
     * 手动管理聊天记忆，构建完整的对话历史传给大模型
     * 
     * @param query 用户查询
     * @param conversationId 对话ID
     * @param topK 检索文档数量
     * @return 流式回答内容
     */
    @Override
    public Flux<String> chatWithKnowledgeStream(String query, String conversationId, int topK) {
        Assert.hasText(query, "查询问题不能为空");
        logger.info("开始流式知识库对话，查询: '{}', conversationId: {}", query, conversationId);

        try {
            String prompt = getRagStr(query, topK);

            return chatClient.prompt(prompt)
                    .system(SYSTEM_PROMPT)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .user(query)
                    .stream()
                    .content();
        } catch (Exception e) {
             logger.error("流式知识库对话失败，查询: '{}'", query, e);
             return Flux.just("对话过程中发生错误: " + e.getMessage());
         }
    }

    /**
     * 生成会话标题
     * 基于对话内容智能生成简洁、相关的会话标题
     *
     * @param conversationContent 会话内容，包含用户问题和AI回答
     * @return 生成的会话标题，长度不超过20个字符
     * @throws IllegalArgumentException 当会话内容为空时抛出
     */
    @Override
    public String generateSessionTitle(StringBuilder conversationContent) {
        // 参数验证
        if (conversationContent == null || conversationContent.isEmpty()) {
            logger.warn("会话内容为空，无法生成标题");
            throw new IllegalArgumentException("会话内容不能为空");
        }

        logger.info("开始生成会话标题，内容长度: {}", conversationContent.length());

        try {
            // 构建优化的提示词，确保生成高质量的标题
            String prompt = String.format(
                    "请根据以下对话内容生成一个简洁、准确的会话标题。要求：\n" +
                            "1. 标题长度不超过20个字符\n" +
                            "2. 准确概括对话主题\n" +
                            "3. 使用简洁明了的语言\n" +
                            "4. 不要包含标点符号\n" +
                            "5. 直接返回标题内容，不要其他说明\n\n" +
                            "对话内容：\n%s",
                    conversationContent
            );

            // 调用AI生成标题
            String generatedTitle = chatClient.prompt(prompt)
                    .options(DashScopeChatOptions.builder()
                            .withTemperature(0.3) // 降低温度以获得更稳定的结果
                            .withMaxToken(50)    // 限制输出长度
                            .build())
                    .call()
                    .content();

            // 清理和验证生成的标题
            logger.info("会话标题生成成功: '{}'", generatedTitle);
            return generatedTitle;

        } catch (Exception e) {
            logger.error("生成会话标题失败", e);
            // 返回默认标题而不是抛出异常，确保系统稳定性
            return "新对话";
        }
    }

    /**
     * 分批添加文档到向量存储
     * 每批最多处理10个文档，避免超过API限制
     *
     * @param documents 要添加的文档列表
     */
    private void addDocumentsInBatches(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        final int BATCH_SIZE = 10; // 每批最多10个文档
        int totalDocuments = documents.size();
        int batchCount = (totalDocuments + BATCH_SIZE - 1) / BATCH_SIZE; // 向上取整

        logger.info("开始分批添加文档到向量存储: 总文档数={}, 批次数={}, 每批大小={}", 
                   totalDocuments, batchCount, BATCH_SIZE);

        for (int i = 0; i < totalDocuments; i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, totalDocuments);
            List<Document> batch = documents.subList(i, endIndex);
            
            try {
                vectorStore.add(batch);
                logger.info("成功添加第 {}/{} 批文档: 文档数={}", 
                           (i / BATCH_SIZE) + 1, batchCount, batch.size());
            } catch (Exception e) {
                logger.error("添加第 {}/{} 批文档失败: 文档数={}, 错误={}", 
                            (i / BATCH_SIZE) + 1, batchCount, batch.size(), e.getMessage(), e);
                throw new RuntimeException("向量存储批处理失败: " + e.getMessage(), e);
            }
        }

        logger.info("所有文档批次添加完成: 总文档数={}", totalDocuments);
    }

    /**
     * 获取RAG提示词
     *
     * @param query 用户查询
     * @param topK 检索文档数量
     * @return 提示词
     */
    private String getRagStr(String query, int topK) {
        List<Document> documents = similaritySearch(query, topK);
        String prompt = "";
        if (documents != null && !documents.isEmpty()){
            // 构建提示词
            String context = documents.stream().map(Document::getText).collect(Collectors.joining("\n\n"));
            prompt = String.format( "知识库内容：\n%s\n\n" , context);
        }
        return prompt;
    }

}