package com.agent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PDFGenerationToolTest {

    @Test
    void generatePDF() {
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        String fileName = "法律知识.pdf";
        String content = "问AI吧，它能给你更多你需要的哟~~";
        String result = pdfGenerationTool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}