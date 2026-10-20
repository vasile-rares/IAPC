package com.genai.first.demo.rag.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/pdf-rag")
public class PdfRagController {

    private static final Pattern THINK_BLOCK = Pattern.compile("(?s)<think>.*?</think>\\s*");

    private final ChatClient chatClient;

    public PdfRagController(@Qualifier("ollamaPgRAGChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping("/ask")
    public String ask(@RequestBody String message) {
        String content = chatClient.prompt()
                .user(message)
                .call()
                .content();
        return content == null ? null : THINK_BLOCK.matcher(content).replaceAll("").strip();
    }
}
