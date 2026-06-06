package org.example.capstone_3.Service;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;
import org.example.capstone_3.Api.ApiException;
import org.example.capstone_3.Model.Mentor;
import org.example.capstone_3.Model.MockInterview;
import org.example.capstone_3.Model.MockInterviewReport;
import org.example.capstone_3.Model.Student;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class MockInterviewReportPdfService {

    private static final String SYSTEM_NAME = "Khutaa";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    public byte[] generateReportPdf(Student student,
                                    Mentor mentor,
                                    MockInterview mockInterview,
                                    MockInterviewReport report) {
        try {
            String html = buildReportHtml(student, mentor, mockInterview, report);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(html, outputStream, new ConverterProperties());
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new ApiException("Failed to generate interview report PDF: " + e.getMessage());
        }
    }

    public String buildAttachmentFileName(MockInterviewReport report) {
        return SYSTEM_NAME + "-Interview-Report-" + report.getId() + ".pdf";
    }

    private String buildReportHtml(Student student,
                                   Mentor mentor,
                                   MockInterview mockInterview,
                                   MockInterviewReport report) {
        String studentName = escapeHtml(student.getFullName());
        String mentorName = escapeHtml(mentor.getFullName());
        String interviewType = escapeHtml(mockInterview.getInterviewType());
        String scheduledAt = mockInterview.getScheduledAt() != null
                ? escapeHtml(mockInterview.getScheduledAt().format(DATE_FORMAT))
                : "N/A";
        String generatedAt = report.getGeneratedAt() != null
                ? escapeHtml(report.getGeneratedAt().format(DATE_FORMAT))
                : "N/A";
        String mentorTitle = escapeHtml(mentor.getJobTitle());
        String mentorCompany = escapeHtml(mentor.getCompany());
        String scoreBlock = mockInterview.getScore() != null
                ? """
                <div class="score-card">
                    <div class="score-label">Interview Score</div>
                    <div class="score-value">%d<span>/100</span></div>
                </div>
                """.formatted(mockInterview.getScore())
                : "";

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        @page { size: A4; margin: 28px; }
                        body {
                            font-family: Helvetica, Arial, sans-serif;
                            color: #1f2937;
                            font-size: 11pt;
                            line-height: 1.55;
                            margin: 0;
                        }
                        .header {
                            background: #0f766e;
                            color: #ffffff;
                            padding: 28px 24px;
                            border-radius: 12px;
                            margin-bottom: 22px;
                        }
                        .brand {
                            font-size: 28pt;
                            font-weight: bold;
                            letter-spacing: 1px;
                            margin: 0 0 6px 0;
                        }
                        .subtitle {
                            font-size: 11pt;
                            opacity: 0.95;
                            margin: 0;
                        }
                        .meta-grid {
                            width: 100%%;
                            border-collapse: collapse;
                            margin-bottom: 22px;
                        }
                        .meta-grid td {
                            width: 50%%;
                            vertical-align: top;
                            padding: 0 10px 0 0;
                        }
                        .meta-card {
                            background: #f8fafc;
                            border: 1px solid #e2e8f0;
                            border-radius: 10px;
                            padding: 14px 16px;
                        }
                        .meta-label {
                            font-size: 9pt;
                            text-transform: uppercase;
                            letter-spacing: 0.6px;
                            color: #64748b;
                            margin-bottom: 4px;
                        }
                        .meta-value {
                            font-size: 11pt;
                            font-weight: bold;
                            color: #0f172a;
                        }
                        .score-card {
                            background: #ecfeff;
                            border: 1px solid #99f6e4;
                            border-radius: 10px;
                            padding: 16px 18px;
                            text-align: center;
                            margin-bottom: 22px;
                        }
                        .score-label {
                            font-size: 10pt;
                            color: #0f766e;
                            text-transform: uppercase;
                            letter-spacing: 0.5px;
                        }
                        .score-value {
                            font-size: 30pt;
                            font-weight: bold;
                            color: #115e59;
                            margin-top: 4px;
                        }
                        .score-value span {
                            font-size: 14pt;
                            color: #64748b;
                        }
                        .section {
                            margin-bottom: 18px;
                            page-break-inside: avoid;
                        }
                        .section-title {
                            font-size: 12pt;
                            font-weight: bold;
                            color: #0f766e;
                            border-bottom: 2px solid #99f6e4;
                            padding-bottom: 6px;
                            margin-bottom: 10px;
                        }
                        .section-body {
                            background: #ffffff;
                            border: 1px solid #e2e8f0;
                            border-radius: 10px;
                            padding: 14px 16px;
                            color: #334155;
                        }
                        .footer {
                            margin-top: 26px;
                            padding-top: 14px;
                            border-top: 1px solid #e2e8f0;
                            font-size: 9pt;
                            color: #64748b;
                            text-align: center;
                        }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <p class="brand">%s</p>
                        <p class="subtitle">Mock Interview Performance Report</p>
                    </div>

                    <table class="meta-grid">
                        <tr>
                            <td>
                                <div class="meta-card">
                                    <div class="meta-label">Student</div>
                                    <div class="meta-value">%s</div>
                                </div>
                            </td>
                            <td>
                                <div class="meta-card">
                                    <div class="meta-label">Mentor</div>
                                    <div class="meta-value">%s</div>
                                    <div style="font-size:10pt;color:#64748b;margin-top:4px;">%s · %s</div>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td style="padding-top:12px;">
                                <div class="meta-card">
                                    <div class="meta-label">Interview Type</div>
                                    <div class="meta-value">%s</div>
                                </div>
                            </td>
                            <td style="padding-top:12px;">
                                <div class="meta-card">
                                    <div class="meta-label">Interview Date</div>
                                    <div class="meta-value">%s</div>
                                </div>
                            </td>
                        </tr>
                    </table>

                    %s

                    <div class="section">
                        <div class="section-title">Summary</div>
                        <div class="section-body">%s</div>
                    </div>

                    <div class="section">
                        <div class="section-title">Strengths</div>
                        <div class="section-body">%s</div>
                    </div>

                    <div class="section">
                        <div class="section-title">Areas for Improvement</div>
                        <div class="section-body">%s</div>
                    </div>

                    <div class="section">
                        <div class="section-title">Recommendations</div>
                        <div class="section-body">%s</div>
                    </div>

                    <div class="footer">
                        Generated by %s on %s · Confidential student report
                    </div>
                </body>
                </html>
                """.formatted(
                SYSTEM_NAME,
                studentName,
                mentorName,
                mentorTitle,
                mentorCompany,
                interviewType,
                scheduledAt,
                scoreBlock,
                formatMultiline(report.getSummary()),
                formatMultiline(report.getStrengths()),
                formatMultiline(report.getWeaknesses()),
                formatMultiline(report.getRecommendations()),
                SYSTEM_NAME,
                generatedAt
        );
    }

    private String formatMultiline(String value) {
        if (value == null || value.isBlank()) {
            return "<em>No information provided.</em>";
        }
        return escapeHtml(value).replace("\n", "<br/>");
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
