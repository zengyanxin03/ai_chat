package com.agent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebScrapingToolTest {

    @Test
    void scrapeWebPage() {
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        String url = "https://blog.csdn.net/cv_dashi/article/details/139805642";
        String result = webScrapingTool.scrapeWebPage(url);
        System.out.println(result);
        Assertions.assertNotNull(result);
    }
}