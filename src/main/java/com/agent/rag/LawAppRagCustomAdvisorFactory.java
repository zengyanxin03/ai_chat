package com.agent.rag;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

public class LawAppRagCustomAdvisorFactory {

    public static Advisor createlawAppRagCustomAdvisor(VectorStore vectorStore, String lawType) {
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("lawType", lawType)
                .build();

        // 创建文档检索器
        VectorStoreDocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .filterExpression(expression)
                .similarityThreshold(0.5)
                .topK(3)
                .build();

        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(MyContextualQueryAugmenterFactory.createInstance())
                .build();
    }
}
