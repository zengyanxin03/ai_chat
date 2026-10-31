package com.agent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;

public class LangChainAiInvoke {
    public static void main(String[] args) {
        QwenChatModel chatModel = QwenChatModel.builder()
                .apiKey("apikey")
                .modelName("qwen3.8-max")
                .isMultimodalModel(true)
                .build();
        String answer = chatModel.chat("你是谁?");
        System.out.println(answer);

    }
}
