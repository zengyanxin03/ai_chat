package com.agent.controller;

import com.agent.app.LawApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {
    @Resource
    private LawApp lawApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 同步调用 AI 法务咨询应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/law_app/chat/sync")
    public String doChatWithLawAppSync(String message, String chatId) {
        return lawApp.doChat(message, chatId);
    }

    /**
     * SSE 流式调用 AI 法务咨询应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/law_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLawAppSSE(String message, String chatId) {
        return lawApp.doChatByStream(message, chatId);
    }

    /**
     * SSE 流式调用 AI 法务咨询应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/law_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithLawAppServerSentEvent(String message, String chatId) {
        return lawApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }


    /**
     * SSE 流式调用 AI 法律咨询应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/law_app/chat/sse_emitter")
    public SseEmitter doChatWithLawAppServerSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter sseEmitter = new SseEmitter(180000L); // 3 分钟超时
        lawApp.doChatByStream(message, chatId)
                .subscribe(chunk -> {
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                }, sseEmitter::completeWithError, sseEmitter::complete);
        // 返回
        return sseEmitter;
    }

}
