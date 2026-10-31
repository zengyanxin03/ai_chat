package com.agent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.NONE;

@Configuration
public class PgVectorVectorStoreConfig {

    @Resource
    private LawAppDocumentLoader lawAppDocumentLoader;

    @Resource
    private MyKeyWordEnricher myKeyWordEnricher;

//    @Bean
    public PgVectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        PgVectorStore pgVectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
//                .dimensions(1536)                    // Optional: defaults to model dimensions or 1536
                .distanceType(COSINE_DISTANCE)       // Optional: defaults to COSINE_DISTANCE
                .indexType(NONE)                     // Optional: defaults to HNSW
                .initializeSchema(true)              // Optional: defaults to false
                .schemaName("public")                // Optional: defaults to "public"
                .vectorTableName("vector_store")     // Optional: defaults to "vector_store"
                // 最大批量插入的文档数
                .maxDocumentBatchSize(1000)         // Optional: defaults to 1000
                .build();

        List<Document> documents = lawAppDocumentLoader.loadMarkdowns();
        // 自动补充每篇文档元信息
        List<Document> enrichedDocuments = myKeyWordEnricher.enrichDocuments(documents);

        pgVectorStore.doAdd(enrichedDocuments);

        return pgVectorStore;
    }
}
