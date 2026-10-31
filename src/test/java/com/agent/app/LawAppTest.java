package com.agent.app;

import cn.hutool.core.lang.UUID;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LawAppTest {

    @Resource
    private LawApp lawApp;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();

        String message = "请用一句话介绍什么是劳动合同?";
        String answer = lawApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);

        message = "企业应该怎样遵循劳动合同?";
        answer = lawApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);

        message = "刚刚我问了什么问题?";
        answer = lawApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);

    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "职场上在签劳动合同的时候应该注意什么?";
        LawApp.LawReport lawReport = lawApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(lawReport);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "试用期可以“先做着看”，不合适再辞退吗?";
        String answer = lawApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithTools() {
        // 测试联网搜索问题的答案
        testMessage(" 口头约定、微信聊天记录算合同吗？效力如何？");

        // 测试网页抓取
        testMessage("合同条款写得“差不多就行”，会有什么后果？");

        // 测试资源下载
        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");

        // 测试终端操作
//        testMessage("执行 Python3 脚本来生成数据分析报告");

        // 测试文件操作
        testMessage("保存我的档案为文件");

        // 测试 PDF 生成
        testMessage("生成一份‘法务咨询大纲’PDF，包含合同法、劳动法和知识产权法等");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = lawApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp() {
        // 测试地图 MCP
        String message = "我在深圳大学读研，请帮我找到深圳大学附近有哪些企业?";
        String chatId = UUID.randomUUID().toString();
        String answer = lawApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }
}