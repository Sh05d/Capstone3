package org.example.capstone_3.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.example.capstone_3.Api.ApiException;
import org.example.capstone_3.Model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class EmailService {

    private static final String SYSTEM_NAME = "Khutaa";

    private final JavaMailSender mailSender;
    private final MockInterviewReportPdfService mockInterviewReportPdfService;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public void sendMentorInterviewScheduledEmail(Student student, Mentor mentor, MockInterview mockInterview) {
        String subject = SYSTEM_NAME + " — Mock Interview Scheduled";

        sendHtmlEmail(
                student.getEmail(),
                subject,
                EmailHtmlTemplates.buildScheduledInterviewHtml(
                        student.getFullName(), true, student, mentor, mockInterview),
                EmailHtmlTemplates.buildScheduledInterviewPlainText(
                        student.getFullName(), true, student, mentor, mockInterview)
        );

        sendHtmlEmail(
                mentor.getEmail(),
                subject,
                EmailHtmlTemplates.buildScheduledInterviewHtml(
                        mentor.getFullName(), false, student, mentor, mockInterview),
                EmailHtmlTemplates.buildScheduledInterviewPlainText(
                        mentor.getFullName(), false, student, mentor, mockInterview)
        );
    }

    public void sendMockInterviewReportEmail(Student student,
                                             Mentor mentor,
                                             MockInterview mockInterview,
                                             MockInterviewReport report) {

        if (student.getEmail() == null || student.getEmail().isBlank()) {
            return;
        }

        byte[] pdfBytes = mockInterviewReportPdfService.generateReportPdf(
                student,
                mentor,
                mockInterview,
                report
        );

        String attachmentName = mockInterviewReportPdfService.buildAttachmentFileName(report);
        String subject = SYSTEM_NAME + " — Your Mock Interview Report";

        sendHtmlEmailWithPdfAttachment(
                student.getEmail(),
                subject,
                EmailHtmlTemplates.buildReportEmailHtml(student, mentor, mockInterview),
                EmailHtmlTemplates.buildReportEmailPlainText(student, mentor, mockInterview),
                attachmentName,
                pdfBytes
        );
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody, String plainBody) {
        if (to == null || to.isBlank()) {
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainBody, htmlBody);

            if (mailFrom != null && !mailFrom.isBlank()) {
                helper.setFrom(mailFrom);
            }

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new ApiException("Failed to send email: " + e.getMessage());
        }
    }

    private void sendHtmlEmailWithPdfAttachment(String to,
                                                String subject,
                                                String htmlBody,
                                                String plainBody,
                                                String attachmentName,
                                                byte[] pdfBytes) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainBody, htmlBody);

            if (mailFrom != null && !mailFrom.isBlank()) {
                helper.setFrom(mailFrom);
            }

            helper.addAttachment(
                    attachmentName,
                    new ByteArrayResource(pdfBytes),
                    "application/pdf"
            );

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new ApiException("Failed to send interview report email: " + e.getMessage());
        }
    }

    public void sendTaskPublishedToStudent(Student student, Task task) {
        String subject = SYSTEM_NAME + " — New Task Published";

        sendHtmlEmail(
                student.getEmail(),
                subject,
                EmailHtmlTemplates.buildTaskPublishedHtml(student, task),
                EmailHtmlTemplates.buildTaskPublishedPlainText(student, task)
        );
    }
}