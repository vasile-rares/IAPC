package com.genai.first.demo.rag.service;

import com.genai.first.demo.rag.config.data.RagConfigData;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Builds a {@link RetrievalAugmentationAdvisor} over the PgVector RAG store.
 * Built per request because in Spring AI 1.0.0-M6 the metadata filter is fixed on the retriever.
 */
@Component
public class AdvancedRagAdvisorFactory {

    private final VectorStore ragVectorStore;
    private final RagConfigData ragConfigData;

    public AdvancedRagAdvisorFactory(@Qualifier("ragVectorStore") VectorStore ragVectorStore,
                                     RagConfigData ragConfigData) {
        this.ragVectorStore = ragVectorStore;
        this.ragConfigData = ragConfigData;
    }

    /**
     * @param filter optional metadata filter, e.g. {@code source == 'scope_roles.pdf'}
     * @throws IllegalArgumentException if the filter expression cannot be parsed
     */
    public RetrievalAugmentationAdvisor create(@Nullable String filter) {
        var retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(ragVectorStore)
                .topK(ragConfigData.getTopK())
                .similarityThreshold(ragConfigData.getSimilarityThreshold());

        if (filter != null && !filter.isBlank()) {
            try {
                retriever.filterExpression(new FilterExpressionTextParser().parse(filter));
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Invalid filter expression: " + filter, e);
            }
        }

        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever.build())
                .queryAugmenter(new CitationQueryAugmenter())
                .build();
    }
}
