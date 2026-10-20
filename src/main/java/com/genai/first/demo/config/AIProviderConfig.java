package com.genai.first.demo.config;

import com.genai.first.demo.rag.config.data.PgVectorStoreConfigData;
import com.genai.first.demo.rag.config.data.RagConfigData;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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

    @Bean("ollamaPgRAGChatClient")
    ChatClient ollamaPgRAGChatClient(OllamaChatModel ollamaChatModel,
                                     @Qualifier("ragVectorStore") VectorStore ragVectorStore,
                                     SimpleLoggerAdvisor simpleLoggerAdvisor,
                                     RagConfigData ragConfigData) {
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(
                        new QuestionAnswerAdvisor(ragVectorStore, SearchRequest.builder()
                                .topK(ragConfigData.getTopK())
                                .similarityThreshold(ragConfigData.getSimilarityThreshold())
                                .build()),
                        simpleLoggerAdvisor
                ).build();
    }

    @Bean("chatMemoryVectorStore")
    VectorStore chatMemoryVectorStore(JdbcTemplate jdbcTemplate,
                                      PgVectorStoreConfigData pgVectorStoreConfigData,
                                      @Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel) {
        return pgVectorStore(jdbcTemplate, pgVectorStoreConfigData, embeddingModel,
                pgVectorStoreConfigData.getTableNameForChatMemory());
    }

    @Bean("ragVectorStore")
    VectorStore ragVectorStore(JdbcTemplate jdbcTemplate,
                               PgVectorStoreConfigData pgVectorStoreConfigData,
                               @Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel) {
        return pgVectorStore(jdbcTemplate, pgVectorStoreConfigData, embeddingModel,
                pgVectorStoreConfigData.getTableNameForRag());
    }

    private PgVectorStore pgVectorStore(JdbcTemplate jdbcTemplate,
                                        PgVectorStoreConfigData config,
                                        EmbeddingModel embeddingModel,
                                        String tableName) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorTableName(tableName)
                .initializeSchema(config.isInitializeSchema())
                .dimensions(config.getDimensions())
                .distanceType(PgVectorStore.PgDistanceType.valueOf(config.getDistanceType()))
                .indexType(PgVectorStore.PgIndexType.valueOf(config.getIndexType()))
                .maxDocumentBatchSize(config.getMaxDocumentBatchSize())
                .build();
    }

    @Bean("traceableWatchLoopExecutor")
    Executor traceableWatchLoopExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Bean("traceableScheduledExecutorService")
    ScheduledExecutorService traceableScheduledExecutorService() {
        return Executors.newSingleThreadScheduledExecutor();
    }
}
