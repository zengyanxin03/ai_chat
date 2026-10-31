package com.agent.app;

import com.agent.advisor.MyLoggerAdvisor;
import com.agent.advisor.ReReadingAdvisor;
import com.agent.chatmemory.FileBasedChatMemory;
import com.agent.rag.LawAppRagCustomAdvisorFactory;
import com.agent.rag.MyQueryRewriter;
import com.alibaba.cloud.ai.advisor.RetrievalRerankAdvisor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class LawApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT_LAW = "# 角色定位\n" +
            "你是一名资深的「合同审查与起草智能体」，扮演具备中国执业经验的企业法务与合规顾问角色。\n" +
            "你的目标是帮助用户完成合同的风险审查、起草与条款优化，输出专业、准确、可落地的法务意见。\n" +
            "你熟悉《中华人民共和国民法典》合同编及相关司法解释、常见商事合同范本与行业监管要求。\n";

    public LawApp(ChatModel dashscopeChatModel){
        // 初始化基于内存的对话记忆
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();

        chatClient = ChatClient.builder(dashscopeChatModel)
                // 设置系统预设
                .defaultSystem(SYSTEM_PROMPT_LAW)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // 自定义拦截器 Advisor,可按需开启
                        new MyLoggerAdvisor()
                        // 自定义推理增强 Advisor,可按需开启
//                       ,new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * AI基础对话(支持多轮记忆对话)
     * @param message 用户输入的问题
     * @param chatId 当前对话的id,不同的用户得到的消息是隔离开的,一个房间一个对话id,每个房间的消息是不一样的
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                // 指定当前这个对话关联的历史上下文是哪个对话的
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatId)
                        // 要获取历史消息的条数 => 指定关联10条历史会话
                        .param(ChatMemory.CONVERSATION_ID, 10))
                .call()
// 将AI输出转换为自定义Java类
//                .entity(User.class)
// 将AI模型输出转换为包含数字列表的Map
//                .entity(new ParameterizedTypeReference<Map<String, Object>>() {})
// 将AI模型输出转换为字符串列表
//                .entity(new ListOutputConverter(new DefaultConversionService()))
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }

    /**
     * AI基础对话(SSE流式传输)
     * @param message 用户输入的问题
     * @param chatId 当前对话的id,不同的用户得到的消息是隔离开的,一个房间一个对话id,每个房间的消息是不一样的
     * @return
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        Flux<String> stringFlux = chatClient.prompt()
                .user(message)
                // 指定当前这个对话关联的历史上下文是哪个对话的
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatId)
                        // 要获取历史消息的条数 => 指定关联10条历史会话
                        .param(ChatMemory.CONVERSATION_ID, 10))
                .stream()
                .content();
        return stringFlux;
    }

    //JDK21
    record LawReport(String title, List<String> contents){

    }

    /**
     * AI 法务咨询报告功能(实战结构化输出)
     * @param message
     * @param chatId
     * @return
     */
    public LawReport doChatWithReport(String message, String chatId) {
        LawReport lawReport = chatClient.prompt()
                .system(SYSTEM_PROMPT_LAW + "每次对话后都要生成法务咨询结果，标题为{用户名}的法务咨询报告，内容为咨询内容大纲")
                .user(message)
                // 当前要取哪个对话的上下文 => 取当前chatId的上下文
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatId)
                        // 要获取历史消息的条数 => 指定关联10条历史会话
                        .param(ChatMemory.CONVERSATION_ID, 10))
                .call()
                .entity(LawReport.class);
        log.info("lawReport: {}", lawReport);
        return lawReport;
    }

    // 基于名称去注入,减少冲突
    @Resource
    private VectorStore lawAppVectorStore;

//    @Resource
//    private Advisor lawAppRagCloudAdvisor;

    @Resource
    private PgVectorStore pgVectorVectorStore;

    @Resource
    private MyQueryRewriter queryRewriter;

    /**
     * 使用RAG 添加上下文
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {
        // 用户查询重写
        String rewrittenMessage = queryRewriter.doQueryRewriter(message);

        ChatResponse chatResponse = chatClient.prompt()
//                .user(message)
                .user(rewrittenMessage)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatId)
                        .param(ChatMemory.CONVERSATION_ID, 10))
                // 使用日志过滤器,开启日志,便于观察结果
                .advisors(new MyLoggerAdvisor())
                // 使用RAG 添加上下文在用户输入的提示词后面
                .advisors(new QuestionAnswerAdvisor(lawAppVectorStore))
                // 应用 RAG 检索增强服务（基于云知识库服务）
//                .advisors(lawAppRagCloudAdvisor)
                // 应用 RAG 检索增强服务（基于 PgVector 服务）
                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                // 应用自定义的 RAG 检索增强服务（基于RetrievalAugmentationAdvisor过滤器实现RAG知识库检索功能）
                .advisors(
                        LawAppRagCustomAdvisorFactory.createlawAppRagCustomAdvisor(
                                pgVectorVectorStore, "知识产权法"
                        )
                )
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }

    // AI 调用工具能力
    @Resource
    private ToolCallback[] allTools;

    /**
     * AI 法务咨询报告功能(结构化输出+支持工具调用)
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithTools(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .system(SYSTEM_PROMPT_LAW + "每次对话后都要生成法务咨询结果，标题为{用户名}的法务咨询报告，内容为咨询内容大纲")
                .user(message)
                // 当前要取哪个对话的上下文 => 取当前chatId的上下文
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatId)
                        // 要获取历史消息的条数 => 指定关联10条历史会话
                        .param(ChatMemory.CONVERSATION_ID, 10))
                // 开启日志
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }

    @Resource
    private ToolCallbackProvider toolCallbackProvider;


    public String doChatWithMcp(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .system(SYSTEM_PROMPT_LAW + "每次对话后都要生成法务咨询结果，标题为{用户名}的法务咨询报告，内容为咨询内容大纲")
                .user(message)
                // 当前要取哪个对话的上下文 => 取当前chatId的上下文
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatId)
                        // 要获取历史消息的条数 => 指定关联10条历史会话
                        .param(ChatMemory.CONVERSATION_ID, 10))
                // 开启日志
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }

}
