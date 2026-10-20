package com.genai.first.demo.config;


import com.genai.first.demo.rag.config.data.PgVectorStoreConfigData;
import com.genai.first.demo.rag.config.data.RagConfigData;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class AIProviderConfig {

    @Bean
    SimpleLoggerAdvisor simpleLoggerAdvisor() {
        return new SimpleLoggerAdvisor();
    }

    @Bean("openAIGeneralChatClient")
    ChatClient openAIGeneralChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).build();
    }

    @Bean("openAIRAGChatClient")
    ChatClient openAIRAGChatClient(OpenAiChatModel openAiChatModel,
                                   SimpleVectorStore simpleVectorStore,
                                   SimpleLoggerAdvisor simpleLoggerAdvisor,
                                   RagConfigData ragConfigData) {
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(
                        new QuestionAnswerAdvisor(simpleVectorStore, SearchRequest.builder()
                                .topK(ragConfigData.getTopK())
                                .similarityThreshold(ragConfigData.getSimilarityThreshold())
                                .build()),
                        simpleLoggerAdvisor
                ).build();
    }

    @Bean("chatMemoryVectorStore")
    public VectorStore chatMemoryVectorStore(JdbcTemplate jdbcTemplate,
                                             PgVectorStoreConfigData pgVectorStoreConfigData,
                                             @Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorTableName(pgVectorStoreConfigData.getTableNameForChatMemory())
                .initializeSchema(pgVectorStoreConfigData.isInitializeSchema())
                .dimensions(pgVectorStoreConfigData.getDimensions())
                .distanceType(PgVectorStore.PgDistanceType.valueOf(pgVectorStoreConfigData.getDistanceType()))
                .indexType(PgVectorStore.PgIndexType.valueOf(pgVectorStoreConfigData.getIndexType()))
                .maxDocumentBatchSize(pgVectorStoreConfigData.getMaxDocumentBatchSize())
                .build();
    }

    @Bean("ragVectorStore")
    public VectorStore ragVectorStore(JdbcTemplate jdbcTemplate,
                                      PgVectorStoreConfigData pgVectorStoreConfigData,
                                      @Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorTableName(pgVectorStoreConfigData.getTableNameForRag())
                .initializeSchema(pgVectorStoreConfigData.isInitializeSchema())
                .dimensions(pgVectorStoreConfigData.getDimensions())
                .distanceType(PgVectorStore.PgDistanceType.valueOf(pgVectorStoreConfigData.getDistanceType()))
                .indexType(PgVectorStore.PgIndexType.valueOf(pgVectorStoreConfigData.getIndexType()))
                .maxDocumentBatchSize(pgVectorStoreConfigData.getMaxDocumentBatchSize())
                .build();
    }

    @Bean("traceableWatchLoopExecutor")
    public Executor traceableWatchLoopExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Bean("traceableScheduledExecutorService")
    public ScheduledExecutorService traceableScheduledExecutorService() {
        return Executors.newSingleThreadScheduledExecutor();
    }


    @Bean
    RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(SimpleVectorStore simpleVectorStore,
                                                              RagConfigData ragConfigData) {
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(simpleVectorStore)
                        .topK(ragConfigData.getTopK())
                        .similarityThreshold(ragConfigData.getSimilarityThreshold())
                        .build())
                .build();
    }

    @Bean("openAIAdvancedRAGChatClient")
    ChatClient openAIAdvancedRAGChatClient(OpenAiChatModel openAiChatModel,
                                           RetrievalAugmentationAdvisor retrievalAugmentationAdvisor) {
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(retrievalAugmentationAdvisor)
                .build();
    }

    @Bean
    @Primary
    EmbeddingModel primaryEmbedding(@Qualifier("openAiEmbeddingModel") EmbeddingModel delegateEmbeddingModel) {
        return delegateEmbeddingModel;
    }


}
