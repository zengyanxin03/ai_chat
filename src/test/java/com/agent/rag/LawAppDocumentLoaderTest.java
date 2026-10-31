package com.agent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ImportAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
class LawAppDocumentLoaderTest {

    @Resource
    private LawAppDocumentLoader lawAppDocumentLoader;

    @Test
    void loadMarkdowns() {
        lawAppDocumentLoader.loadMarkdowns();
    }
}