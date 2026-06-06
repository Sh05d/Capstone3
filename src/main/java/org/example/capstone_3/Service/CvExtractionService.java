package org.example.capstone_3.Service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.example.capstone_3.Api.ApiException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CvExtractionService {

    private static final Pattern GOOGLE_DRIVE_FILE_ID_PATTERN =
            Pattern.compile("drive\\.google\\.com/file/d/([a-zA-Z0-9_-]+)");
    private static final Pattern GOOGLE_DRIVE_OPEN_ID_PATTERN =
            Pattern.compile("drive\\.google\\.com/(?:open|uc)\\?[^#]*[?&]id=([a-zA-Z0-9_-]+)");
    private static final Pattern GOOGLE_DRIVE_CONFIRM_TOKEN_PATTERN =
            Pattern.compile("confirm=([0-9A-Za-z_]+)");

    private static final int MAX_PDF_BYTES = 10 * 1024 * 1024;
    private static final int MAX_CV_TEXT_LENGTH = 50_000;

    private final RestClient restClient = RestClient.builder()
            .requestFactory(new JdkClientHttpRequestFactory(
                    HttpClient.newBuilder()
                            .followRedirects(HttpClient.Redirect.ALWAYS)
                            .build()))
            .defaultHeader(HttpHeaders.USER_AGENT,
                    "Mozilla/5.0 (compatible; Capstone3-CvExtractor/1.0)")
            .build();

    public String extractTextFromPdfUrl(String cvUrl) {
        byte[] pdfBytes = downloadPdf(cvUrl);
        String text = parsePdfText(pdfBytes);
        if (text.isBlank()) {
            throw new ApiException("No readable text found in the CV PDF");
        }
        return truncate(text);
    }

    private byte[] downloadPdf(String cvUrl) {
        String googleFileId = extractGoogleDriveFileId(cvUrl);
        if (googleFileId != null) {
            return downloadGoogleDrivePdf(googleFileId);
        }
        return fetchBytes(resolveDownloadUrl(cvUrl));
    }

    private byte[] downloadGoogleDrivePdf(String fileId) {
        String baseUrl = "https://drive.google.com/uc?export=download&id=" + fileId;
        byte[] bytes = fetchBytes(baseUrl + "&confirm=t");
        if (isPdf(bytes)) {
            return bytes;
        }
        String confirmToken = extractConfirmTokenFromHtml(bytes);
        if (confirmToken != null) {
            bytes = fetchBytes(baseUrl + "&confirm=" + confirmToken);
        }
        validatePdfBytes(bytes);
        return bytes;
    }

    private byte[] fetchBytes(String url) {
        URI uri = toUri(url);
        try {
            byte[] bytes = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(byte[].class);
            if (bytes == null || bytes.length == 0) {
                throw new ApiException("CV PDF download returned empty content");
            }
            if (bytes.length > MAX_PDF_BYTES) {
                throw new ApiException("CV PDF exceeds maximum allowed size (10 MB)");
            }
            return bytes;
        } catch (RestClientResponseException ex) {
            throw new ApiException("Could not download CV PDF (HTTP " + ex.getStatusCode().value() + ")");
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException("Could not download CV PDF: " + ex.getMessage());
        }
    }

    private String extractConfirmTokenFromHtml(byte[] bytes) {
        if (bytes == null || bytes.length == 0 || isPdf(bytes)) {
            return null;
        }
        String html = new String(bytes, StandardCharsets.UTF_8);
        Matcher matcher = GOOGLE_DRIVE_CONFIRM_TOKEN_PATTERN.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private boolean isPdf(byte[] bytes) {
        return bytes != null
                && bytes.length >= 4
                && bytes[0] == '%'
                && bytes[1] == 'P'
                && bytes[2] == 'D'
                && bytes[3] == 'F';
    }

    private String extractGoogleDriveFileId(String cvUrl) {
        String trimmed = cvUrl.trim();
        Matcher fileMatcher = GOOGLE_DRIVE_FILE_ID_PATTERN.matcher(trimmed);
        if (fileMatcher.find()) {
            return fileMatcher.group(1);
        }
        Matcher openMatcher = GOOGLE_DRIVE_OPEN_ID_PATTERN.matcher(trimmed);
        if (openMatcher.find()) {
            return openMatcher.group(1);
        }
        if (trimmed.contains("drive.google.com") && trimmed.contains("id=")) {
            int idIndex = trimmed.indexOf("id=");
            String idPart = trimmed.substring(idIndex + 3);
            int amp = idPart.indexOf('&');
            String fileId = amp >= 0 ? idPart.substring(0, amp) : idPart;
            if (!fileId.isBlank()) {
                return fileId;
            }
        }
        return null;
    }

    private String parsePdfText(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document).trim();
        } catch (IOException ex) {
            throw new ApiException("Could not read CV PDF: " + ex.getMessage());
        }
    }

    /**
     * Normalizes non-Drive URLs (Drive uses {@link #downloadGoogleDrivePdf}).
     */
    private String resolveDownloadUrl(String cvUrl) {
        return cvUrl.trim();
    }

    private void validatePdfBytes(byte[] bytes) {
        if (!isPdf(bytes)) {
            throw new ApiException(
                    "CV URL must point to a PDF file. For Google Drive: share as 'Anyone with the link'. "
                            + "If it still fails, upload the PDF to GitHub and use a Raw link "
                            + "(https://raw.githubusercontent.com/.../resume.pdf).");
        }
    }

    private URI toUri(String cvUrl) {
        try {
            URI uri = new URI(cvUrl.trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new ApiException("CV URL must use http or https");
            }
            return uri;
        } catch (URISyntaxException ex) {
            throw new ApiException("CV URL is not valid");
        }
    }

    private String truncate(String text) {
        if (text.length() <= MAX_CV_TEXT_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_CV_TEXT_LENGTH);
    }
}
