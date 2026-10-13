package com.genai.first.demo.config;

import com.genai.first.demo.rag.config.data.RagConfigData;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIProviderConfig {

    @Bean("ollamaGeneralChatClient")
    ChatClient ollamaGeneralChatClient(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel).build();
    }

    @Bean
    SimpleLoggerAdvisor simpleLoggerAdvisor() {
        return new SimpleLoggerAdvisor();
    }

    @Bean("ollamaRAGChatClient")
    ChatClient ollamaRAGChatClient(OllamaChatModel ollamaChatModel,
                                   SimpleVectorStore simpleVectorStore,
                                   SimpleLoggerAdvisor simpleLoggerAdvisor,
                                   RagConfigData ragConfigData) {
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(
                        new QuestionAnswerAdvisor(simpleVectorStore, SearchRequest.builder()
                                .topK(ragConfigData.getTopK())
                                .similarityThreshold(ragConfigData.getSimilarityThreshold())
                                .build()),
                        simpleLoggerAdvisor
                ).build();
    }
}
