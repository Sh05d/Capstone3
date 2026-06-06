package org.example.capstone_3.AI;

/**
 * Thrown when the AI provider is not configured or the API call fails.
 * Handled by {@link org.example.capstone_3.ControllerAdvise.ControllerAdvisor}.
 */
public class AiException extends RuntimeException {

    public AiException(String message) {
        super(message);
    }

    public AiException(String message, Throwable cause) {
        super(message, cause);
    }
}
