package com.agent.rag;

import com.agent.advisor.MyLoggerAdvisor;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MyMultiQueryExpander {

    private final ChatClient.Builder chatClientBuilder;

    public MyMultiQueryExpander(ChatModel dashscopeChatModel) {
        this.chatClientBuilder = ChatClient.builder(dashscopeChatModel);
    }

    public List<Query> expand(Query query) {

        MultiQueryExpander queryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(chatClientBuilder)
                .numberOfQueries(3)
                .build();

        List<Query> queryList = queryExpander.expand(query);
        return queryList;
    }
}
