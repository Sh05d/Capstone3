package org.example.capstone_3.AI;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiServiceTest {

    @Test
    void extractAssistantContent_parsesOpenAiStyleJson() {
        String json = """
                {
                  "choices": [{
                    "message": {
                      "content": "Hello\\nworld"
                    }
                  }]
                }
                """;
        assertEquals("Hello\nworld", AiService.extractAssistantContent(json));
    }

    @Test
    void extractAssistantContent_throwsWhenEmpty() {
        assertThrows(AiException.class, () -> AiService.extractAssistantContent(""));
    }

    @Test
    void normalizeJsonContent_stripsMarkdownFences() {
        String raw = "```json\n{\"ok\":true}\n```";
        assertEquals("{\"ok\":true}", AiService.normalizeJsonContent(raw));
    }

    @Test
    void validateJsonShape_rejectsPlainText() {
        assertThrows(AiException.class, () -> AiService.validateJsonShape("not json"));
    }
}
