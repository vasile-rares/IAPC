package com.genai.first.demo.rag.service;

import com.genai.first.demo.rag.config.data.RagConstants;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;

import java.util.List;

/**
 * Wraps {@link ContextualQueryAugmenter} and prefixes every retrieved passage with a
 * citation header like {@code [source:file.pdf, page_number:12]}, so the model can copy it verbatim.
 */
public class CitationQueryAugmenter implements QueryAugmenter {

    static final String PAGE_NUMBER = "page_number";

    private static final PromptTemplate PROMPT_TEMPLATE = new PromptTemplate("""
            CONTEXT (each passage starts with a header [source:..., page_number:...]):

            ---------------------
            {context}
            ---------------------

            Answer the query using ONLY the CONTEXT above.

            Rules:
            1. If the answer is not in the CONTEXT, say "I don't know".
            2. Cite every fact using the exact header of the passage it came from,
               e.g. [source:file.pdf, page_number:12]. Copy the values exactly.

            Query: {query}

            Answer:
            """);

    private final QueryAugmenter delegate = ContextualQueryAugmenter.builder()
            .promptTemplate(PROMPT_TEMPLATE)
            .allowEmptyContext(false)
            .build();

    @Override
    public Query augment(Query query, List<Document> documents) {
        List<Document> withHeaders = documents.stream()
                .map(CitationQueryAugmenter::withCitationHeader)
                .toList();
        return delegate.augment(query, withHeaders);
    }

    private static Document withCitationHeader(Document document) {
        Object source = document.getMetadata().getOrDefault(RagConstants.SOURCE, RagConstants.UNKNOWN);
        Object page = document.getMetadata().getOrDefault(PAGE_NUMBER, RagConstants.UNKNOWN);
        String header = "[source:" + source + ", page_number:" + page + "]";
        return new Document(document.getId(), header + "\n" + document.getText(), document.getMetadata());
    }
}
