package com.genai.first.demo.rag.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RagStartupRunner implements ApplicationRunner {

    private final RagIngestionService ingestion;
    private final PdfWatcherService pdfWatcher;

    @Override
    public void run(ApplicationArguments args) {
        ingestion.initializePgVectorStore();
        pdfWatcher.start();
    }
}
