package com.agent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileOperationToolTest {

    @Test
    void readFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String fileName = "ai_agent.txt";
        String result = fileOperationTool.readFile(fileName);
        System.out.println(result);
        Assertions.assertNotNull(result);
    }

    @Test
    void writeFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String fileName = "ai_agent.txt";
        String content = "本人正在找工作，希望面试官给个机会";
        String result = fileOperationTool.writeFile(fileName, content);
        Assertions.assertNotNull(result);
    }
}