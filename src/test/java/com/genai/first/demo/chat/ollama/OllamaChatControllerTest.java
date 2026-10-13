package com.genai.first.demo.chat.ollama;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OllamaChatControllerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @InjectMocks
    private OllamaChatController ollamaChatController;

    @Test
    void testSummarize() {
        String inputMessage = "Summarize this long text.";
        String expectedSummary = "This is the summary.";

        when(chatClient.prompt().user(inputMessage).call().content()).thenReturn(expectedSummary);

        String result = ollamaChatController.summarize(inputMessage);

        assertEquals(expectedSummary, result);
    }

    @Test
    void testSummarizeStripsThinkBlock() {
        String inputMessage = "Summarize this long text.";

        when(chatClient.prompt().user(inputMessage).call().content())
                .thenReturn("<think>\nreasoning here\n</think>\n\nThis is the summary.");

        String result = ollamaChatController.summarize(inputMessage);

        assertEquals("This is the summary.", result);
    }
}
