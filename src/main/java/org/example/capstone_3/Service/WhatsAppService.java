package org.example.capstone_3.Service;

import jakarta.annotation.PostConstruct;
import org.example.capstone_3.Model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${ultramsg.api-url}")
    private String apiUrl;

    @Value("${ultramsg.token}")
    private String token;

    @PostConstruct
    void validateConfig() {
        if (!isConfigured()) {
            log.error("UltraMsg WhatsApp is not configured (ultramsg.api-url / ultramsg.token). Messages will not be sent.");
            return;
        }

        String envToken = System.getenv("ULTRAMSG_TOKEN");
        if (envToken != null && !envToken.isBlank()) {
            log.warn(
                    "ULTRAMSG_TOKEN is still set in Windows/IDE environment (suffix: {}). "
                            + "Spring uses it instead of application-local.properties. Delete it and restart IntelliJ.",
                    tokenSuffix(envToken)
            );
        }

        log.info(
                "UltraMsg WhatsApp configured for instance {} (active token suffix: {})",
                resolvedApiUrl(),
                tokenSuffix(resolvedToken())
        );
    }

    public void sendWhatsApp(String toPhone, String message) {
        if (!isConfigured()) {
            log.error("Skipping WhatsApp send because UltraMsg is not configured");
            return;
        }

        String to = toInternationalFormat(toPhone);
        if (to == null) {
            log.warn("Skipping WhatsApp send because phone number is missing or invalid (raw value: {})", toPhone);
            return;
        }

        String apiToken = resolvedToken();

        try {
            URI requestUri = UriComponentsBuilder
                    .fromUriString(resolvedApiUrl() + "/messages/chat")
                    .queryParam("token", apiToken)
                    .queryParam("to", to)
                    .queryParam("body", message)
                    .encode()
                    .build()
                    .toUri();

            log.info("Sending WhatsApp via UltraMsg GET to {}", to);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(requestUri)
                    .GET()
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            int status = httpResponse.statusCode();
            String responseBody = httpResponse.body();

            if (status < 200 || status >= 300) {
                log.error("Failed to send WhatsApp to {} (HTTP {}): {}", to, status, responseBody);
                return;
            }

            JsonNode response = JSON.readTree(responseBody);

            if (response.hasNonNull("error")) {
                String error = response.get("error").asString();
                if (error.contains("Wrong token")) {
                    log.error(
                            "UltraMsg rejected WhatsApp to {}: invalid token (active suffix: {}). "
                                    + "Check ultramsg.token in application-local.properties and remove ULTRAMSG_TOKEN from Windows/IDE env.",
                            to,
                            tokenSuffix(apiToken)
                    );
                } else {
                    log.error("UltraMsg rejected WhatsApp to {}: {}", to, error);
                }
                return;
            }

            if (!response.path("sent").asBoolean(false)) {
                log.error("UltraMsg did not confirm WhatsApp delivery to {}: {}", to, responseBody);
                return;
            }

            log.info("UltraMsg WhatsApp sent to {} (message id: {})", to, response.path("id").asString());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("WhatsApp send interrupted for {}: {}", to, ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Failed to send WhatsApp to {}: {}", to, ex.getMessage(), ex);
        }
    }

    public void sendInviteMessage(Student invitedStudent, LearningGroup learningGroup, Student inviter) {
        sendWhatsApp(invitedStudent.getPhoneNumber(),
                WhatsAppTemplates.buildGroupInvite(invitedStudent, learningGroup, inviter));
    }

    public void sendInterviewRequestToMentor(Mentor mentor, Student student, MockInterview mockInterview) {
        log.info("Sending mentor interview request WhatsApp to mentor {} (interview id {})",
                mentor.getId(), mockInterview.getId());

        sendWhatsApp(mentor.getPhoneNumber(),
                WhatsAppTemplates.buildInterviewRequest(mentor, student, mockInterview));
    }

    public void sendMentorInterviewScheduledNotifications(Student student,
                                                          Mentor mentor,
                                                          MockInterview mockInterview) {
        log.info("Sending scheduled interview WhatsApp for interview id {}", mockInterview.getId());

        sendWhatsApp(
                student.getPhoneNumber(),
                EmailHtmlTemplates.buildScheduledInterviewPlainText(
                        student.getFullName(), true, student, mentor, mockInterview)
        );

        sendWhatsApp(
                mentor.getPhoneNumber(),
                EmailHtmlTemplates.buildScheduledInterviewPlainText(
                        mentor.getFullName(), false, student, mentor, mockInterview)
        );
    }

    public void sendInterviewReminderToStudent(Student student, MockInterview mockInterview) {
        sendWhatsApp(student.getPhoneNumber(),
                WhatsAppTemplates.buildStudentReminder(student, mockInterview));
    }

    public void sendInterviewReminderToMentor(Mentor mentor, Student student, MockInterview mockInterview) {
        sendWhatsApp(mentor.getPhoneNumber(),
                WhatsAppTemplates.buildMentorReminder(mentor, student, mockInterview));
    }

    private boolean isConfigured() {
        return !resolvedApiUrl().isBlank() && resolvedToken() != null && !resolvedToken().isBlank();
    }

    private String resolvedToken() {
        return token == null ? null : token.trim();
    }

    private String resolvedApiUrl() {
        if (apiUrl == null || apiUrl.isBlank()) {
            return "";
        }
        String normalized = apiUrl.trim();
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String toInternationalFormat(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }

        String digits = phone.replaceAll("\\D", "");
        if (digits.isBlank()) {
            return null;
        }

        return "+" + digits;
    }

    private String tokenSuffix(String value) {
        if (value == null || value.length() < 4) {
            return "****";
        }
        return "..." + value.substring(value.length() - 4);
    }

    public void sendTaskDeadlineReminderToStudent(Student student, Task task) {

        String message = "Hello " + student.getFullName() + "\n\n"
                + "Reminder: Task deadline is tomorrow!\n\n"
                + "Task Details:\n"
                + "Title: " + task.getTitle() + "\n"
                + "Difficulty: " + task.getDifficulty() + "\n"
                + "Please make sure to submit on time.\n\n"
                + "Kutaa team";

        sendWhatsApp(student.getPhoneNumber(), message);
    }
}
