package com.agent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceDownloadToolTest {

    @Test
    void downloadResource() {
        ResourceDownloadTool downloadTool = new ResourceDownloadTool();
        String url = "\thttps://springdoc.cn/spring-ai/_/img/spring-logo.svg";
        String fileName = "spring-logo.svg";
        String result = downloadTool.downloadResource(url, fileName);
        Assertions.assertNotNull(result);
    }
}