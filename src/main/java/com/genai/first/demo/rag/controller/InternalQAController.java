package com.genai.first.demo.rag.controller;

import com.genai.first.demo.rag.service.AdvancedRagAdvisorFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/internal-qa")
public class InternalQAController {

    private static final Pattern THINK_BLOCK = Pattern.compile("(?s)<think>.*?</think>\\s*");

    private static final String SYSTEM_PROMPT = "You are an internal-docs assistant. " +
            "Provide clear and concise solutions, troubleshooting steps, and recommendations. " +
            "Use ONLY the provided CONTEXT. If not found, say I don't know. " +
            "Each CONTEXT passage starts with a header like [source:filename.pdf, page_number:12]. " +
            "When citing, copy the header's values exactly (do not simplify to [source:12] or change labels).";

    private final ChatClient chatClient;
    private final AdvancedRagAdvisorFactory advisorFactory;

    public InternalQAController(@Qualifier("ollamaAdvancedRAGChatClient") ChatClient chatClient,
                                AdvancedRagAdvisorFactory advisorFactory) {
        this.chatClient = chatClient;
        this.advisorFactory = advisorFactory;
    }

    /**
     * @param filter optional metadata filter, e.g. {@code source == 'scope_roles.pdf'}
     */
    @PostMapping("/ask")
    public String getInformationFromInternalDocs(@RequestBody String message,
                                                 @RequestParam(value = "filter", required = false) String filter) {
        try {
            String content = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .advisors(advisorFactory.create(filter))
                    .user(message)
                    .call()
                    .content();
            return content == null ? null : THINK_BLOCK.matcher(content).replaceAll("").strip();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}
