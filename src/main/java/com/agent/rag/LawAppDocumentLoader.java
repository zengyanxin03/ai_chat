package com.agent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 法务咨询应用文档加载器
 */
@Slf4j
@Component
public class LawAppDocumentLoader {

    // 资源解析类: 可以快速加载多个文档 使用构造函数的构造方法来注入
    private final ResourcePatternResolver resourcePatternResolver;

    public LawAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载多篇Markdown文档
     * @return
     */
    public List<Document> loadMarkdowns() {
        List<Document> allDocuments = new ArrayList<>();
        // 加载多篇 Markdown 文档
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");

            for (Resource resource : resources) {
                // 后续可以基于文件名做一些处理:允许在创建文档的过程中指定某篇文档的额外信息Metadata, 这里把文件名作为额外信息保存到Document中,这样如果我们要检索的话,我们不仅可以按照相似度检索,还可以按照文件名-元信息进行检索,相当于给这篇文档打了一个标签
                String filename = resource.getFilename();
                String lawType = filename.substring(filename.length() - 6, filename.length() - 3);
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        // 是否包含代码块
                        .withIncludeCodeBlock(false)
                        // 是否包含引用格式
                        .withIncludeBlockquote(false)
                        // 把每篇文档的文件名作为元信息添加进来
                        .withAdditionalMetadata("filename", filename)
                        .withAdditionalMetadata("lawType",lawType)
                        .build();
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
                List<Document> documents = markdownDocumentReader.get();
                allDocuments.addAll(documents);
            }

        } catch (IOException e) {
            log.error("Markdown 文档加载失败", e);
            throw new RuntimeException(e);
        }
        return allDocuments;
    }
}
