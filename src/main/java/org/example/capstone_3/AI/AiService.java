package org.example.capstone_3.AI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Single entry point for all AI API calls in the project.
 * <p>
 * {@link #ask(String)} always requests a JSON object from the API ({@code response_format: json_object}).
 * Put the exact JSON shape you need inside {@code prompt}.
 * <p>
 * Example prompt:
 * <pre>
 *   Respond with JSON only using this shape:
 *   {"jobTitle": "...", "matchScore": 0}
 * </pre>
 */
@Service
public class AiService {

    private static final String JSON_SYSTEM_MESSAGE =
            "You must respond with a single valid JSON object only. No markdown, no code fences, no extra text.";

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${ai.model:gpt-4o-mini}")
    private String model;

    @Value("${ai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    /**
     * Sends a prompt to the chat API and returns a JSON string.
     * <p>
     * The prompt must describe the JSON fields you expect (OpenAI requires this when using json_object mode).
     *
     * @param prompt instructions plus the JSON schema you want
     * @return JSON object as text, starting with {@code {}
     */
    public String ask(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiException(
                    "AI is not configured. Add openai.api.key to application-local.properties (copy from application-local.properties.example).");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content", JSON_SYSTEM_MESSAGE),
                Map.of("role", "user", "content", prompt)
        ));
        body.put("response_format", Map.of("type", "json_object"));
        body.put("temperature", 0);

        try {
            RestClient client = RestClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .build();

            String responseBody = client.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            String content = normalizeJsonContent(extractAssistantContent(responseBody));
            validateJsonShape(content);
            return content;
        } catch (AiException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            throw new AiException("AI API error: " + ex.getStatusCode() + " — " + ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            throw new AiException("AI request failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Strips optional ```json fences if the model adds them anyway.
     */
    static String normalizeJsonContent(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > 0) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            int closingFence = trimmed.lastIndexOf("```");
            if (closingFence >= 0) {
                trimmed = trimmed.substring(0, closingFence);
            }
            trimmed = trimmed.trim();
        }
        return trimmed;
    }

    static void validateJsonShape(String content) {
        if (content.isEmpty()) {
            throw new AiException("AI returned an empty response.");
        }
        char first = content.charAt(0);
        if (first != '{' && first != '[') {
            throw new AiException("AI response is not JSON. Starts with: " + content.substring(0, Math.min(40, content.length())));
        }
    }

    /**
     * Reads {@code choices[0].message.content} from the OpenAI-style JSON response without extra libraries.
     */
    static String extractAssistantContent(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new AiException("AI returned an empty response.");
        }
        String marker = "\"content\"";
        int markerIndex = responseBody.indexOf(marker);
        if (markerIndex < 0) {
            throw new AiException("AI response did not contain message content.");
        }
        int valueStart = responseBody.indexOf('"', markerIndex + marker.length());
        if (valueStart < 0) {
            throw new AiException("AI response did not contain message content.");
        }
        valueStart++;

        StringBuilder content = new StringBuilder();
        for (int i = valueStart; i < responseBody.length(); i++) {
            char c = responseBody.charAt(i);
            if (c == '\\' && i + 1 < responseBody.length()) {
                char next = responseBody.charAt(++i);
                switch (next) {
                    case 'n' -> content.append('\n');
                    case 'r' -> content.append('\r');
                    case 't' -> content.append('\t');
                    case '"' -> content.append('"');
                    case '\\' -> content.append('\\');
                    default -> content.append(next);
                }
            } else if (c == '"') {
                break;
            } else {
                content.append(c);
            }
        }

        String text = content.toString().trim();
        if (text.isEmpty()) {
            throw new AiException("AI returned an empty response.");
        }
        return text;
    }
}
