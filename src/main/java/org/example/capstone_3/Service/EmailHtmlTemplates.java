package org.example.capstone_3.Service;

import org.example.capstone_3.Model.Mentor;
import org.example.capstone_3.Model.MockInterview;
import org.example.capstone_3.Model.Student;
import org.example.capstone_3.Model.Task;

final class EmailHtmlTemplates {

    private static final String SYSTEM_NAME = "Khutaa";
    private static final String BRAND_COLOR = "#0f766e";
    private static final String BRAND_COLOR_DARK = "#115e59";

    private EmailHtmlTemplates() {
    }

    static String buildScheduledInterviewHtml(String recipientName,
                                              boolean forStudent,
                                              Student student,
                                              Mentor mentor,
                                              MockInterview mockInterview) {
        String greeting = forStudent
                ? "Your mock interview has been scheduled successfully."
                : "You have a new mock interview session scheduled with a student.";

        String meetingUrl = mockInterview.getUrl();
        String meetingLinkHtml = meetingUrl != null && !meetingUrl.isBlank()
                ? """
                <p style="margin:0 0 8px;">
                    <a href="%s" style="color:%s;font-weight:bold;text-decoration:none;">Join meeting</a>
                </p>
                <p style="margin:0;font-size:12px;color:#64748b;word-break:break-all;">%s</p>
                """.formatted(escapeHtml(meetingUrl), BRAND_COLOR, escapeHtml(meetingUrl))
                : "<p style=\"margin:0;color:#64748b;\">Meeting link will be shared separately.</p>";

        String content = """
                <p style="margin:0 0 16px;font-size:16px;color:#0f172a;">Hello <strong>%s</strong>,</p>
                <p style="margin:0 0 20px;color:#334155;">%s</p>
                %s
                %s
                <p style="margin:20px 0 0;color:#334155;">Please join the meeting at the scheduled time. Good luck!</p>
                """.formatted(
                escapeHtml(recipientName),
                greeting,
                detailCard("Interview details", buildScheduledDetailsRows(student, mentor, mockInterview)),
                sectionBox("Meeting", meetingLinkHtml)
        );

        return wrapLayout("Mock Interview Scheduled", content);
    }

    static String buildScheduledInterviewPlainText(String recipientName,
                                                   boolean forStudent,
                                                   Student student,
                                                   Mentor mentor,
                                                   MockInterview mockInterview) {
        String greeting = forStudent
                ? "Your mock interview has been scheduled successfully."
                : "You have a new mock interview session scheduled with a student.";

        return """
                Hello %s,

                %s

                Interview Details:
                Student: %s
                Mentor: %s
                Interview Type: %s
                Date & Time: %s
                Duration: %s minutes
                Meeting Provider: %s
                Meeting Link: %s

                Please join the meeting at the scheduled time.

                %s Team
                """.formatted(
                recipientName,
                greeting,
                student.getFullName(),
                mentor.getFullName(),
                mockInterview.getInterviewType(),
                mockInterview.getScheduledAt(),
                mockInterview.getDurationMinutes(),
                mockInterview.getMeetingProvider(),
                nullToDash(mockInterview.getUrl()),
                SYSTEM_NAME
        );
    }

    static String buildReportEmailHtml(Student student, Mentor mentor, MockInterview mockInterview) {
        String content = """
                <p style="margin:0 0 16px;font-size:16px;color:#0f172a;">Hello <strong>%s</strong>,</p>
                <p style="margin:0 0 20px;color:#334155;">
                    Here is your PDF mock interview report from <strong>%s</strong>.
                </p>
                %s
                <p style="margin:20px 0 0;color:#334155;">
                    Your mentor <strong>%s</strong> has submitted feedback for your
                    <strong>%s</strong> interview session.
                    The full report is attached to this email as a PDF.
                </p>
                <p style="margin:16px 0 0;color:#334155;">Keep learning and growing.</p>
                """.formatted(
                escapeHtml(student.getFullName()),
                SYSTEM_NAME,
                detailCard("Session summary", buildReportDetailsRows(mentor, mockInterview)),
                escapeHtml(mentor.getFullName()),
                escapeHtml(mockInterview.getInterviewType())
        );

        return wrapLayout("Your Mock Interview Report", content);
    }

    static String buildReportEmailPlainText(Student student, Mentor mentor, MockInterview mockInterview) {
        return """
                Hello %s,

                Here is your PDF mock interview report from %s.

                Mentor: %s
                Interview Type: %s
                Interview Date & Time: %s

                Your mentor has submitted feedback for your session.
                Please find the full report attached to this email.

                Keep learning and growing,
                %s Team
                """.formatted(
                student.getFullName(),
                SYSTEM_NAME,
                mentor.getFullName(),
                mockInterview.getInterviewType(),
                mockInterview.getScheduledAt(),
                SYSTEM_NAME
        );
    }

    private static String buildScheduledDetailsRows(Student student, Mentor mentor, MockInterview mockInterview) {
        return detailRow("Student", student.getFullName())
                + detailRow("Mentor", mentor.getFullName())
                + detailRow("Interview type", mockInterview.getInterviewType())
                + detailRow("Date & time", String.valueOf(mockInterview.getScheduledAt()))
                + detailRow("Duration", mockInterview.getDurationMinutes() + " minutes")
                + detailRow("Meeting provider", nullToDash(mockInterview.getMeetingProvider()));
    }

    private static String buildReportDetailsRows(Mentor mentor, MockInterview mockInterview) {
        return detailRow("Mentor", mentor.getFullName())
                + detailRow("Interview type", mockInterview.getInterviewType())
                + detailRow("Interview date", String.valueOf(mockInterview.getScheduledAt()))
                + detailRow("Attachment", "Khutaa interview report (PDF)");
    }

    private static String wrapLayout(String title, String contentHtml) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8"/>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                    <title>%s</title>
                </head>
                <body style="margin:0;padding:0;background:#f1f5f9;font-family:Arial,Helvetica,sans-serif;color:#1f2937;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#f1f5f9;padding:24px 12px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:620px;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 10px 30px rgba(15,118,110,0.12);">
                                    <tr>
                                        <td style="background:%s;padding:28px 32px;">
                                            <p style="margin:0;font-size:28px;font-weight:bold;color:#ffffff;letter-spacing:0.5px;">%s</p>
                                            <p style="margin:8px 0 0;font-size:14px;color:#ccfbf1;">%s</p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:32px;">
                                            %s
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:0 32px 28px;">
                                            <p style="margin:0;font-size:12px;color:#94a3b8;text-align:center;">
                                                Sent by %s · Your career development platform
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                escapeHtml(title),
                BRAND_COLOR,
                SYSTEM_NAME,
                escapeHtml(title),
                contentHtml,
                SYSTEM_NAME
        );
    }

    private static String detailCard(String title, String rowsHtml) {
        return """
                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="margin:0 0 20px;background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;">
                    <tr>
                        <td style="padding:18px 20px;">
                            <p style="margin:0 0 12px;font-size:13px;font-weight:bold;color:%s;text-transform:uppercase;letter-spacing:0.6px;">%s</p>
                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
                                %s
                            </table>
                        </td>
                    </tr>
                </table>
                """.formatted(BRAND_COLOR, escapeHtml(title), rowsHtml);
    }

    private static String sectionBox(String title, String innerHtml) {
        return """
                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="margin:0 0 4px;background:#ecfeff;border:1px solid #99f6e4;border-radius:12px;">
                    <tr>
                        <td style="padding:18px 20px;">
                            <p style="margin:0 0 10px;font-size:13px;font-weight:bold;color:%s;text-transform:uppercase;letter-spacing:0.6px;">%s</p>
                            %s
                        </td>
                    </tr>
                </table>
                """.formatted(BRAND_COLOR_DARK, escapeHtml(title), innerHtml);
    }

    private static String detailRow(String label, String value) {
        return """
                <tr>
                    <td style="padding:6px 0;font-size:12px;color:#64748b;width:38%%;vertical-align:top;">%s</td>
                    <td style="padding:6px 0;font-size:14px;color:#0f172a;font-weight:bold;vertical-align:top;">%s</td>
                </tr>
                """.formatted(escapeHtml(label), escapeHtml(nullToDash(value)));
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    public static String buildTaskPublishedHtml(Student student, Task task) {
        String content = """
            <p style="margin:0 0 16px;font-size:16px;color:#0f172a;">Hello <strong>%s</strong>,</p>
            <p style="margin:0 0 20px;color:#334155;">A new task has been published in <strong>%s</strong> group.</p>
            %s
            <p style="margin:20px 0 0;color:#334155;">Make sure to submit before the deadline. Good luck!</p>
            """.formatted(
                escapeHtml(student.getFullName()),
                escapeHtml(task.getLearningGroup().getName()),
                detailCard("Task Details", buildTaskDetailsRows(task))
        );

        return wrapLayout("New Task Published", content);
    }

    public static String buildTaskPublishedPlainText(Student student, Task task) {
        return """
            Hello %s,

            A new task has been published in %s group.

            Task Details:
            Title: %s
            Difficulty: %s
            Points: %s
            Deadline: %s

            Make sure to submit before the deadline.

            %s Team
            """.formatted(
                student.getFullName(),
                task.getLearningGroup().getName(),
                task.getTitle(),
                task.getDifficulty(),
                task.getPoints(),
                task.getDeadline(),
                SYSTEM_NAME
        );
    }

    private static String buildTaskDetailsRows(Task task) {
        return detailRow("Title", task.getTitle())
                + detailRow("Difficulty", task.getDifficulty())
                + detailRow("Points", String.valueOf(task.getPoints()))
                + detailRow("Deadline", String.valueOf(task.getDeadline()));
    }
}
