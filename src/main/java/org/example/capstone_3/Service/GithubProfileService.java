package org.example.capstone_3.Service;

import tools.jackson.databind.JsonNode;
import org.example.capstone_3.Api.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class GithubProfileService {

    private static final int MAX_REPOS = 8;
    private static final int MAX_GITHUB_TEXT_LENGTH = 30_000;
    private static final Pattern GITHUB_HOST_PATTERN = Pattern.compile("^(?:https?://)?(?:www\\.)?github\\.com/",
            Pattern.CASE_INSENSITIVE);

    private static final Set<String> RESERVED_USER_SEGMENTS = Set.of(
            "settings", "organizations", "marketplace", "explore", "topics", "collections",
            "events", "sponsors", "login", "join", "features", "enterprise", "team", "customer-stories");

    private final RestClient restClient;
    private final String apiToken;

    public GithubProfileService(
            @Value("${github.api.base-url:https://api.github.com}") String baseUrl,
            @Value("${github.api.token:}") String apiToken) {
        this.apiToken = apiToken;
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28");
        if (apiToken != null && !apiToken.isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken.trim());
        }
        this.restClient = builder.build();
    }

    public String fetchProfileSummary(String githubUrl) {
        String username = parseUsername(githubUrl);
        JsonNode user = fetchJson("/users/" + username);
        JsonNode repos = fetchJson("/users/" + username + "/repos?sort=updated&per_page=" + MAX_REPOS + "&type=owner");
        String summary = buildSummary(user, repos);
        if (summary.isBlank()) {
            throw new ApiException("No GitHub profile content found for user " + username);
        }
        return truncate(summary);
    }

    public String normalizeGithubUrl(String githubUrl) {
        if (githubUrl == null || githubUrl.isBlank()) {
            return null;
        }
        String trimmed = githubUrl.trim();
        if (!GITHUB_HOST_PATTERN.matcher(trimmed).find() && !trimmed.toLowerCase(Locale.ROOT).contains("github.com/")) {
            throw new ApiException("GitHub URL must be a valid github.com profile link");
        }
        if (!trimmed.toLowerCase(Locale.ROOT).startsWith("http")) {
            trimmed = "https://" + trimmed;
        }
        return trimmed;
    }

    public String canonicalUsernameKey(String githubUrl) {
        if (githubUrl == null || githubUrl.isBlank()) {
            return null;
        }
        return parseUsername(githubUrl).toLowerCase(Locale.ROOT);
    }

    private JsonNode fetchJson(String path) {
        try {
            return restClient.get()
                    .uri(path)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ApiException("GitHub user not found");
            }
            if (ex.getStatusCode().value() == 403) {
                throw new ApiException(
                        "GitHub API rate limit reached. Configure github.api.token in application-local.properties");
            }
            throw new ApiException("Could not fetch GitHub profile (HTTP " + ex.getStatusCode().value() + ")");
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException("Could not fetch GitHub profile: " + ex.getMessage());
        }
    }

    private String buildSummary(JsonNode user, JsonNode repos) {
        StringBuilder summary = new StringBuilder();
        appendLine(summary, "GitHub username: ", textOrDash(user, "login"));
        appendLine(summary, "Name: ", textOrDash(user, "name"));
        appendLine(summary, "Bio: ", textOrDash(user, "bio"));
        appendLine(summary, "Company: ", textOrDash(user, "company"));
        appendLine(summary, "Location: ", textOrDash(user, "location"));
        appendLine(summary, "Public repositories: ", String.valueOf(user.path("public_repos").asInt(0)));

        List<String> repoLines = new ArrayList<>();
        if (repos != null && repos.isArray()) {
            for (JsonNode repo : repos) {
                if (repo.path("fork").asBoolean(false)) {
                    continue;
                }
                String name = repo.path("name").asText("");
                if (name.isBlank()) {
                    continue;
                }
                String language = repo.path("language").asText("unknown");
                String description = repo.path("description").asText("").trim();
                int stars = repo.path("stargazers_count").asInt(0);
                String line = "- " + name + " (" + language + ", stars: " + stars + ")";
                if (!description.isBlank()) {
                    line += ": " + description;
                }
                repoLines.add(line);
                if (repoLines.size() >= MAX_REPOS) {
                    break;
                }
            }
        }

        summary.append("\nRepositories (recent, non-fork):\n");
        if (repoLines.isEmpty()) {
            summary.append("(none listed)\n");
        } else {
            for (String line : repoLines) {
                summary.append(line).append('\n');
            }
        }
        return summary.toString().trim();
    }

    private void appendLine(StringBuilder builder, String label, String value) {
        builder.append(label).append(value == null || value.isBlank() ? "-" : value.trim()).append('\n');
    }

    private String textOrDash(JsonNode node, String field) {
        String value = node.path(field).asText("");
        return value.isBlank() ? "-" : value;
    }

    private String parseUsername(String githubUrl) {
        String normalized = normalizeGithubUrl(githubUrl);
        try {
            URI uri = new URI(normalized);
            String path = uri.getPath();
            if (path == null || path.isBlank() || path.equals("/")) {
                throw new ApiException("GitHub URL must include a username");
            }
            String[] segments = path.split("/");
            String username = null;
            for (String segment : segments) {
                if (segment != null && !segment.isBlank()) {
                    username = segment;
                    break;
                }
            }
            if (username == null || username.isBlank()) {
                throw new ApiException("GitHub URL must include a username");
            }
            if (RESERVED_USER_SEGMENTS.contains(username.toLowerCase(Locale.ROOT))) {
                throw new ApiException("GitHub URL must point to a user profile, not a site page");
            }
            return username;
        } catch (URISyntaxException ex) {
            throw new ApiException("GitHub URL is not valid");
        }
    }

    private String truncate(String text) {
        if (text.length() <= MAX_GITHUB_TEXT_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_GITHUB_TEXT_LENGTH);
    }
}
