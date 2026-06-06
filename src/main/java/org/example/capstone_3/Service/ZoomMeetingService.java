package org.example.capstone_3.Service;

import lombok.RequiredArgsConstructor;
import org.example.capstone_3.Api.ApiException;
import org.example.capstone_3.Model.Mentor;
import org.example.capstone_3.Model.MockInterview;
import org.example.capstone_3.Model.Student;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ZoomMeetingService implements MeetingService {

    @Value("${zoom.account-id}")
    private String accountId;

    @Value("${zoom.client-id}")
    private String clientId;

    @Value("${zoom.client-secret}")
    private String clientSecret;

    @Value("${zoom.base-url}")
    private String zoomBaseUrl;

    @Value("${zoom.token-url}")
    private String zoomTokenUrl;

    @Override
    public MeetingResponse createMeeting(MockInterview mockInterview, Student student, Mentor mentor) {

        String accessToken = getAccessToken();

        Map<String, Object> body = new HashMap<>();

        body.put("topic", "Khutaa Mock Interview - " + student.getFullName());
        body.put("type", 2);
        body.put("start_time", mockInterview.getScheduledAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        body.put("duration", mockInterview.getDurationMinutes());
        body.put("timezone", "Asia/Riyadh");
        body.put("agenda", mockInterview.getDescription());

        Map<String, Object> settings = new HashMap<>();
        settings.put("join_before_host", true);
        settings.put("waiting_room", false);
        settings.put("approval_type", 0);
        settings.put("audio", "both");
        settings.put("auto_recording", "none");

        body.put("settings", settings);

        try {
            RestClient client = RestClient.builder()
                    .baseUrl(zoomBaseUrl)
                    .defaultHeader("Authorization", "Bearer " + accessToken)
                    .build();

            Map response = client.post()
                    .uri("/users/me/meetings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null || response.get("join_url") == null || response.get("id") == null) {
                throw new ApiException("Zoom did not return meeting details");
            }

            String meetingId = String.valueOf(response.get("id"));
            String joinUrl = String.valueOf(response.get("join_url"));

            return new MeetingResponse(
                    meetingId,
                    joinUrl,
                    "ZOOM"
            );

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to create Zoom meeting: " + e.getMessage());
        }
    }

    private String getAccessToken() {

        String credentials = clientId + ":" + clientSecret;

        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        try {
            RestClient client = RestClient.builder()
                    .baseUrl(zoomTokenUrl)
                    .defaultHeader("Authorization", "Basic " + encodedCredentials)
                    .build();

            ZoomTokenResponse response = client.post()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("grant_type", "account_credentials")
                            .queryParam("account_id", accountId)
                            .build())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .retrieve()
                    .body(ZoomTokenResponse.class);

            if (response == null || response.getAccessToken() == null || response.getAccessToken().isBlank()) {
                throw new ApiException("Zoom did not return access token");
            }

            return response.getAccessToken();

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get Zoom access token: " + e.getMessage());
        }
    }
}