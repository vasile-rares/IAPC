package com.genai.first.demo.chat.ollama;

import java.util.regex.Pattern;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ollama/chat")
public class OllamaChatController {

    private static final Pattern THINK_BLOCK = Pattern.compile("(?s)<think>.*?</think>\\s*");

    private final ChatClient chatClient;

    public OllamaChatController(@Qualifier("ollamaGeneralChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping("/summarize")
    public String summarize(@RequestBody String message) {
        String content = chatClient.prompt()
                .user(message)
                .call()
                .content();
        return content == null ? null : THINK_BLOCK.matcher(content).replaceAll("").strip();
    }
}
