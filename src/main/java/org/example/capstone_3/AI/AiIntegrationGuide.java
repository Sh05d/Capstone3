package org.example.capstone_3.AI;

/**
 * Team guide — read this class first.
 * <p>
 * <b>Folder layout</b>
 * <pre>
 *   AI/
 *     AiService.java              → call aiService.ask(prompt) from any Service
 *     AiException.java
 *     AiIntegrationGuide.java     → this file
 * </pre>
 * <p>
 * <b>Steps to wire AI into a domain service (example: JobAnalysisService)</b>
 * <ol>
 *   <li>Add {@code private final AiService aiService;} to the service (constructor injection).</li>
 *   <li>Build a prompt that includes the exact JSON shape you need (see example in {@link AiService} javadoc).</li>
 *   <li>Call: {@code String json = aiService.ask(prompt);} — response is always JSON ({@code response_format: json_object}).</li>
 *   <li>Parse JSON into entity fields and save with the repository.</li>
 * </ol>
 * <p>
 * <b>Config</b> — put secrets in {@code application-local.properties}:
 * <pre>
 *   openai.api.key=sk-...
 * </pre>
 * Optional in {@code application.properties}: {@code ai.model}, {@code ai.base-url}
 * <p>
 * <b>Not done yet (by design)</b>: no new REST endpoints; domain services still use placeholder data until each owner wires AI.
 */
public final class AiIntegrationGuide {

    private AiIntegrationGuide() {
    }
}
