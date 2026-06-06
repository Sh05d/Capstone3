package org.example.capstone_3.AI;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AiServiceIntegrationTest {

    @Autowired
    private AiService aiService;

    @Test
    void ask_returnsJsonObject() {
        String result = aiService.ask("""
                Return JSON with exactly this shape:
                {"status":"ok"}
                """);
        assertNotNull(result);
        assertFalse(result.isBlank(), "AI response should not be blank");
        assertTrue(result.trim().startsWith("{"), "AI response should be a JSON object");
        assertTrue(result.contains("status"), "AI response should contain expected field");
        System.out.println("AiService test passed. Response length: " + result.length());
        System.out.println("Response preview: " + result.substring(0, Math.min(80, result.length())));
    }
}
