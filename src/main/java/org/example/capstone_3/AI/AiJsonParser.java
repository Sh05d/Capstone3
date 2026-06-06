package org.example.capstone_3.AI;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses JSON strings returned by {@link AiService#ask(String)}.
 */
public final class AiJsonParser {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private AiJsonParser() {
    }

    public static JsonNode parseObject(String json) {
        try {
            JsonNode node = JSON.readTree(json);
            if (node == null || !node.isObject()) {
                throw new AiException("AI response is not a JSON object.");
            }
            return node;
        } catch (AiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AiException("AI response is not valid JSON: " + ex.getMessage(), ex);
        }
    }

    public static boolean requireBoolean(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || !value.isBoolean()) {
            throw new AiException("AI response did not contain " + field + ".");
        }
        return value.asBoolean();
    }

    public static int requireInt(JsonNode node, String field, int min, int max) {
        JsonNode value = node.get(field);
        if (value == null || !value.isNumber()) {
            throw new AiException("AI response did not contain " + field + ".");
        }
        int number = value.asInt();
        if (number < min || number > max) {
            throw new AiException("AI " + field + " must be between " + min + " and " + max + ".");
        }
        return number;
    }

    public static String requireText(JsonNode node, String field) {
        return requireTextValue(node.get(field), field);
    }

    public static String optionalText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isArray()) {
            String joined = joinTextArray(value);
            return joined.isBlank() ? null : joined;
        }
        if (value.isTextual()) {
            String text = value.asString().trim();
            return text.isBlank() ? null : text;
        }
        return null;
    }

    public static List<String> optionalStringList(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || !value.isArray()) {
            return List.of();
        }

        List<String> items = new ArrayList<>();
        for (JsonNode item : value) {
            if (item.isTextual()) {
                String text = item.asString().trim();
                if (!text.isBlank()) {
                    items.add(text);
                }
            }
        }
        return items;
    }

    private static String requireTextValue(JsonNode value, String fieldName) {
        if (value == null || value.isNull()) {
            throw new AiException("AI response did not contain " + fieldName + ".");
        }
        if (value.isArray()) {
            String joined = joinTextArray(value);
            if (joined.isBlank()) {
                throw new AiException("AI response did not contain " + fieldName + ".");
            }
            return joined;
        }
        if (value.isTextual()) {
            String text = value.asString().trim();
            if (text.isBlank()) {
                throw new AiException("AI response did not contain " + fieldName + ".");
            }
            return text;
        }
        throw new AiException("AI response field " + fieldName + " must be a string.");
    }

    private static String joinTextArray(JsonNode array) {
        List<String> parts = new ArrayList<>();
        for (JsonNode item : array) {
            if (item.isTextual()) {
                String text = item.asString().trim();
                if (!text.isBlank()) {
                    parts.add(text);
                }
            }
        }
        return String.join("\n", parts);
    }
}
