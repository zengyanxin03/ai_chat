package com.agent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


/**
 * 法务咨询助手向量数据库配置(初始化基于内存的向量数据库 Bean)
 */
@Configuration
public class LawAppVectorStoreConfig {

    @Resource
    private LawAppDocumentLoader lawAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeyWordEnricher myKeyWordEnricher;

    @Bean
    VectorStore lawAppVectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        // 加载所有文档
        List<Document> documentList = lawAppDocumentLoader.loadMarkdowns();
        // 基于TokenTextSplitter切分文档(不建议使用，还是选择智能切割)
        List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(documentList);
        simpleVectorStore.doAdd(splitDocuments);
        // 自动增加每篇文档元信息
        List<Document> enrichedDocuments = myKeyWordEnricher.enrichDocuments(documentList);
        simpleVectorStore.doAdd(documentList);
        return simpleVectorStore;
    }
}
