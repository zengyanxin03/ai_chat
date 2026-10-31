package com.agent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rometools.utils.IO;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FileBasedChatMemory implements ChatMemory {

    private final String BASE_DIR;

    private static final Kryo kryo = new Kryo();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        kryo.setRegistrationRequired(false);
        // 设置实例化策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    // 构造对象时,指定文件保存目录
    public FileBasedChatMemory(String dir) {
        this.BASE_DIR = dir;
        File baseDir = new File(dir);
        if(!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    @Override
    public void add(String conversationId, Message message) {
        saveConversation(conversationId, List.of(message));
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        // 获取往期的消息
        List<Message> messageList = getOrCreateConversation(conversationId);
        // 再保存新消息
        messageList.addAll(messages);
        saveConversation(conversationId, messageList);
    }

    @Override
    public List<Message> get(String conversationId) {
        List<Message> messageList = getOrCreateConversation(conversationId);
        /* 获取某会话最后几条消息
        return messageList.stream()
                .skip(Math.max(0, messageList.size()) - lastN)
                .toList();*/
        // 获取某会话全部消息
        return messageList;
    }

    @Override
    public void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if(file.exists()){
            file.delete();
        }
    }

    private void saveConversation(String conversationId, List<Message> messages) {
        try {
            File file = getConversationFile(conversationId);
            objectMapper.writeValue(file, messages);
        }catch (IOException e) {
            throw new RuntimeException("保存对话记忆失败",e);
        }
    }



    /**
     * 获取或创建会话消息的列表
     * @param conversationId
     * @return
     */
    private List<Message> getOrCreateConversation(String conversationId) {
        File file = getConversationFile(conversationId);
        if(!file.exists()) {
            return new ArrayList<>();
        }
        try {
            List<Message> messages = objectMapper.readValue(file, new TypeReference<List<Message>>() {
            });
            return messages;
        }catch (IOException e) {
            // 文件损坏或格式不兼容时，返回空列表或备份后重建
            return new ArrayList<>();
        }
    }

    /**
     * 每个会话文件单独保存
     * @param conversationId
     * @return
     */
    private File getConversationFile(String conversationId) {
        return new File(BASE_DIR, conversationId + ".kryo");
    }
}
