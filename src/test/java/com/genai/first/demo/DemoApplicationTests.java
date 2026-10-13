package com.genai.first.demo;

import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class DemoApplicationTests {

	// The real vector store embeds the handbook through Ollama at startup, which the test must not depend on.
	@MockitoBean
	private SimpleVectorStore simpleVectorStore;

	@Test
	void contextLoads() {
	}

}
