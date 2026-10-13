package com.genai.first.demo.config;

import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Embeddings run against their own Ollama instance (local by default), while chat uses
 * spring.ai.ollama.base-url. Spring AI shares a single base-url between the two, so the
 * embedding model is defined here with a dedicated OllamaApi; the auto-configured one backs off.
 */
@Configuration
public class OllamaEmbeddingConfig {

    @Bean
    OllamaEmbeddingModel ollamaEmbeddingModel(
            @Value("${app.ollama.embedding.base-url}") String baseUrl,
            @Value("${spring.ai.ollama.embedding.options.model}") String model) {
        var ollamaApi = new OllamaApi(baseUrl, RestClient.builder(), WebClient.builder());
        return OllamaEmbeddingModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(OllamaOptions.builder().model(model).build())
                .build();
    }
}
