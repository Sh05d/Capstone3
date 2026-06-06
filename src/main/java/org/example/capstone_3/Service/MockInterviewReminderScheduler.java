package org.example.capstone_3.Service;

import lombok.RequiredArgsConstructor;
import org.example.capstone_3.Model.*;
import org.example.capstone_3.Repository.MockInterviewRepository;
import org.example.capstone_3.Repository.StudentRepository;
import org.example.capstone_3.Repository.TaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MockInterviewReminderScheduler {

    private final TaskRepository taskRepository;
    private final StudentRepository studentRepository;
    private final MockInterviewRepository mockInterviewRepository;
    private final WhatsAppService whatsAppService;


    @Scheduled(fixedRate = 60000)
    public void sendUpcomingInterviewReminders() {

        List<MockInterview> scheduledInterviews =
                mockInterviewRepository.findMockInterviewsByStatus("SCHEDULE");

        LocalDateTime now = LocalDateTime.now();

        for (MockInterview mockInterview : scheduledInterviews) {

            if (!"MENTOR".equals(mockInterview.getInterviewMode())) {
                continue;
            }

            if (Boolean.TRUE.equals(mockInterview.getWhatsappReminderSent())) {
                continue;
            }

            if (mockInterview.getScheduledAt() == null) {
                continue;
            }

            Student student = mockInterview.getStudent();
            Mentor mentor = mockInterview.getMentor();

            if (student == null || mentor == null) {
                continue;
            }

            LocalDateTime reminderStart = mockInterview.getScheduledAt().minusMinutes(3);
            LocalDateTime reminderEnd = mockInterview.getScheduledAt();

            boolean isReminderTime =
                    !now.isBefore(reminderStart) && now.isBefore(reminderEnd);

            if (!isReminderTime) {
                continue;
            }

            whatsAppService.sendInterviewReminderToStudent(student, mockInterview);
            whatsAppService.sendInterviewReminderToMentor(mentor, student, mockInterview);

            mockInterview.setWhatsappReminderSent(true);
            mockInterviewRepository.save(mockInterview);
        }
    }

    @Scheduled(fixedRate = 60000)
    public void sendUpcomingTaskDeadlineReminders() {

        List<Task> tasks = taskRepository.findTasksByWhatsappReminderSentFalse();

        LocalDateTime now = LocalDateTime.now();

        for (Task task : tasks) {

            if (task.getDeadline() == null) {
                continue;
            }

            LearningGroup learningGroup = task.getLearningGroup();
            if (learningGroup == null) {
                continue;
            }

            List<Student> students = studentRepository.findStudentsByGroupId(learningGroup.getId());
            if (students == null || students.isEmpty()) {
                continue;
            }

            LocalDateTime reminderTime = task.getDeadline().minusHours(24);

            boolean isReminderTime = !now.isBefore(reminderTime) && now.isBefore(task.getDeadline());

            if (!isReminderTime) {
                continue;
            }

            for (Student student : students) {
                whatsAppService.sendTaskDeadlineReminderToStudent(student, task);
            }

            task.setWhatsappReminderSent(true);
            taskRepository.save(task);
        }
    }
}