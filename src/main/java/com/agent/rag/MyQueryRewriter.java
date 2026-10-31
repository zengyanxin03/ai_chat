package com.agent.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

/**
 * 查询重写器
 */
@Component
public class MyQueryRewriter {

    private final QueryTransformer queryTransformer;

    public MyQueryRewriter(ChatModel dashscopeChatModel) {
        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);

        queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();
    }

    /**
     * 执行查询重写
     * @param prompt
     * @return
     */
    public String doQueryRewriter(String prompt) {
        Query query = new Query(prompt);
        Query transformedQuery = queryTransformer.transform(query);
        return transformedQuery.text();
    }
}
