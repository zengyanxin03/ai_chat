package com.agent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 元信息增强器（使用AI为文档生成元信息）
 */
@Component
public class MyKeyWordEnricher {

    @Resource
    private ChatModel dashscopeChatModel;

    public List<Document> enrichDocuments(List<Document> documents) {
        // 元信息增强器
        KeywordMetadataEnricher keywordMetadataEnricher = new KeywordMetadataEnricher(dashscopeChatModel, 5);
        List<Document> documentList = keywordMetadataEnricher.apply(documents);
        return documentList;
    }
}
