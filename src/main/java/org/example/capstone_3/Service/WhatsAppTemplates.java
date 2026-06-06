package org.example.capstone_3.Service;

import org.example.capstone_3.Model.LearningGroup;
import org.example.capstone_3.Model.Mentor;
import org.example.capstone_3.Model.MockInterview;
import org.example.capstone_3.Model.Student;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

final class WhatsAppTemplates {

    private static final String SYSTEM_NAME = "Khutaa";
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private WhatsAppTemplates() {
    }

    static String buildInterviewRequest(Mentor mentor, Student student, MockInterview mockInterview) {
        return """
                Hello %s,

                You have a new mock interview request on %s.

                Student: %s
                Target Role: %s
                Interview Type: %s
                Date & Time: %s
                Duration: %s

                Please open %s and review the request.

                %s Team
                """.formatted(
                mentor.getFullName(),
                SYSTEM_NAME,
                student.getFullName(),
                nullToDash(student.getTargetRole()),
                nullToDash(mockInterview.getInterviewType()),
                formatDateTime(mockInterview.getScheduledAt()),
                formatDuration(mockInterview.getDurationMinutes()),
                SYSTEM_NAME,
                SYSTEM_NAME
        );
    }

    static String buildStudentReminder(Student student, MockInterview mockInterview) {
        return """
                Hello %s,

                Reminder: Your mock interview starts in about 3 minutes.

                Interview Type: %s
                Date & Time: %s
                Duration: %s
                Meeting Link: %s

                Good luck!

                %s Team
                """.formatted(
                student.getFullName(),
                nullToDash(mockInterview.getInterviewType()),
                formatDateTime(mockInterview.getScheduledAt()),
                formatDuration(mockInterview.getDurationMinutes()),
                nullToDash(mockInterview.getUrl()),
                SYSTEM_NAME
        );
    }

    static String buildMentorReminder(Mentor mentor, Student student, MockInterview mockInterview) {
        return """
                Hello %s,

                Reminder: Your mock interview session starts in about 3 minutes.

                Student: %s
                Target Role: %s
                Interview Type: %s
                Date & Time: %s
                Duration: %s
                Meeting Link: %s

                %s Team
                """.formatted(
                mentor.getFullName(),
                student.getFullName(),
                nullToDash(student.getTargetRole()),
                nullToDash(mockInterview.getInterviewType()),
                formatDateTime(mockInterview.getScheduledAt()),
                formatDuration(mockInterview.getDurationMinutes()),
                nullToDash(mockInterview.getUrl()),
                SYSTEM_NAME
        );
    }

    static String buildGroupInvite(Student invitedStudent, LearningGroup learningGroup, Student inviter) {
        return """
                Hello %s,

                %s has invited you to join a private learning group on %s.

                Group: %s
                Focus Area: %s
                Join Code: %s

                Open %s and use the code above to join.

                %s Team
                """.formatted(
                invitedStudent.getFullName(),
                inviter.getFullName(),
                SYSTEM_NAME,
                nullToDash(learningGroup.getName()),
                nullToDash(learningGroup.getFocusArea()),
                nullToDash(learningGroup.getCode()),
                SYSTEM_NAME,
                SYSTEM_NAME
        );
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "—" : dateTime.format(DATE_TIME_FORMAT);
    }

    private static String formatDuration(Integer minutes) {
        return minutes == null ? "—" : minutes + " minutes";
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
