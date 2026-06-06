package org.example.capstone_3.Service;

import lombok.RequiredArgsConstructor;
import org.example.capstone_3.AI.AiException;
import org.example.capstone_3.AI.AiJsonParser;
import org.example.capstone_3.AI.AiService;
import org.example.capstone_3.Api.ApiException;
import org.example.capstone_3.DTO.IN.AiInterviewAnswerDTOIN;
import org.example.capstone_3.DTO.IN.AiMockInterviewDTOIN;
import org.example.capstone_3.DTO.IN.MockInterviewDTOIN;
import org.example.capstone_3.DTO.IN.MockInterviewRescheduleDTOIn;
import org.example.capstone_3.DTO.OUT.*;
import org.example.capstone_3.Model.Mentor;
import org.example.capstone_3.Model.MockInterview;
import org.example.capstone_3.Model.MockInterviewReport;
import org.example.capstone_3.Model.Student;
import org.example.capstone_3.Repository.MentorRepository;
import org.example.capstone_3.Repository.MockInterviewReportRepository;
import org.example.capstone_3.Repository.MockInterviewRepository;
import org.example.capstone_3.Repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MockInterviewService {

    private final MockInterviewRepository mockInterviewRepository;
    private final MockInterviewReportRepository mockInterviewReportRepository;
    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;
    private final AiService aiService;
    private final StudentProfilePromptHelper studentProfilePromptHelper;
    private final MeetingService meetingService;
    private final EmailService emailService;
    private final WhatsAppService whatsAppService;


    public List<StudentInterviewHistoryDTOOUT> getStudentHistory(Integer studentId) {

        Student student = studentRepository.findStudentById(studentId);
        if (student == null) {
            throw new ApiException("Student with id " + studentId + " not found");
        }

        List<MockInterview> interviews =
                mockInterviewRepository.findMockInterviewsByStudentIdOrderByCreatedAtDesc(studentId);

        List<StudentInterviewHistoryDTOOUT> result = new ArrayList<>();

        for (MockInterview interview : interviews) {
            String mentorName = (interview.getMentor() != null)
                    ? interview.getMentor().getFullName()
                    : null;

            result.add(new StudentInterviewHistoryDTOOUT(
                    interview.getId(),
                    interview.getInterviewMode(),
                    interview.getInterviewType(),
                    interview.getStatus(),
                    interview.getScheduledAt(),
                    interview.getDurationMinutes(),
                    interview.getScore(),
                    mentorName,
                    interview.getCreatedAt()
            ));
        }

        return result;
    }

    public StudentInterviewStatsDTOOUT getStudentInterviewStats(Integer studentId) {

        Student student = studentRepository.findStudentById(studentId);
        if (student == null) {
            throw new ApiException("Student with id " + studentId + " not found");
        }

        List<MockInterview> all =
                mockInterviewRepository.findMockInterviewsByStudentId(studentId);

        int total = all.size();
        int mentorCount = 0;
        int aiCount = 0;
        int completedCount = 0;

        for (MockInterview m : all) {
            if ("MENTOR".equals(m.getInterviewMode())) mentorCount++;
            if ("AI".equals(m.getInterviewMode()))     aiCount++;
            if ("COMPLETE".equals(m.getStatus()))      completedCount++;
        }

        Double avgScore = mockInterviewRepository.avgScoreForStudent(studentId);

        return new StudentInterviewStatsDTOOUT(
                total,
                mentorCount,
                aiCount,
                completedCount,
                avgScore
        );
    }

    public List<MentorScheduleDTOOUT> getMentorSchedule(Integer mentorId) {

        Mentor mentor = mentorRepository.findMentorById(mentorId);
        if (mentor == null) {
            throw new ApiException("Mentor with id " + mentorId + " not found");
        }

        List<MockInterview> scheduled =
                mockInterviewRepository
                        .findMockInterviewsByMentorIdAndStatusOrderByScheduledAtAsc(mentorId, "SCHEDULE");

        List<MentorScheduleDTOOUT> result = new ArrayList<>();

        for (MockInterview interview : scheduled) {
            String studentName = (interview.getStudent() != null)
                    ? interview.getStudent().getFullName()
                    : null;
            String studentTargetRole = (interview.getStudent() != null)
                    ? interview.getStudent().getTargetRole()
                    : null;

            result.add(new MentorScheduleDTOOUT(
                    interview.getId(),
                    interview.getInterviewType(),
                    interview.getScheduledAt(),
                    interview.getDurationMinutes(),
                    studentName,
                    studentTargetRole,
                    interview.getUrl(),
                    interview.getMeetingProvider()
            ));
        }

        return result;
    }


    public void createMentorInterview(Integer studentId, Integer mentorId, MockInterviewDTOIN dto) {

        Student student = findStudent(studentId);
        Mentor mentor = findMentor(mentorId);

        if (!Boolean.TRUE.equals(mentor.getAcceptedByAdmin())) {
            throw new ApiException("Mentor is not accepted by admin yet");
        }

        MockInterview mockInterview = new MockInterview();

        mockInterview.setInterviewMode("MENTOR");
        mockInterview.setInterviewType(dto.getInterviewType());
        mockInterview.setDescription(dto.getDescription());
        mockInterview.setScheduledAt(dto.getScheduledAt());
        mockInterview.setDurationMinutes(dto.getDurationMinutes());
        mockInterview.setStatus("PENDING");
        mockInterview.setCreatedAt(LocalDateTime.now());
        mockInterview.setStudent(student);
        mockInterview.setMentor(mentor);

        mockInterview.setQuestions(generateMentorQuestions(student, mentor, dto));
        mockInterview.setStudentAnswers(null);
        mockInterview.setFeedback(null);
        mockInterview.setScore(null);
        mockInterview.setUrl(null);
        mockInterview.setMeetingProvider(null);
        mockInterview.setExternalMeetingId(null);
        mockInterview.setWhatsappReminderSent(false);

        mockInterviewRepository.save(mockInterview);

        whatsAppService.sendInterviewRequestToMentor(
                mentor,
                student,
                mockInterview
        );
    }

    public void acceptMentorInterview(Integer mentorId, Integer mockInterviewId) {

        Mentor mentor = findMentor(mentorId);
        MockInterview mockInterview = findMockInterview(mockInterviewId);

        if (!"MENTOR".equals(mockInterview.getInterviewMode())) {
            throw new ApiException("Only mentor interviews can be accepted by mentor");
        }

        if (mockInterview.getMentor() == null || !mockInterview.getMentor().getId().equals(mentorId)) {
            throw new ApiException("This mock interview does not belong to this mentor");
        }

        if (!"PENDING".equals(mockInterview.getStatus())) {
            throw new ApiException("Only pending interviews can be accepted");
        }

        MeetingResponse meeting = meetingService.createMeeting(
                mockInterview,
                mockInterview.getStudent(),
                mentor
        );

        mockInterview.setUrl(meeting.getJoinUrl());
        mockInterview.setExternalMeetingId(meeting.getMeetingId());
        mockInterview.setMeetingProvider(meeting.getProvider());
        mockInterview.setStatus("SCHEDULE");

        mockInterviewRepository.save(mockInterview);

        emailService.sendMentorInterviewScheduledEmail(
                mockInterview.getStudent(),
                mentor,
                mockInterview
        );

        whatsAppService.sendMentorInterviewScheduledNotifications(
                mockInterview.getStudent(),
                mentor,
                mockInterview
        );
    }

    public void rejectMentorInterview(Integer mentorId, Integer mockInterviewId) {

        Mentor mentor = mentorRepository.findMentorById(mentorId);
        if (mentor == null) {
            throw new ApiException("Mentor with id " + mentorId + " not found");
        }

        MockInterview mockInterview =
                mockInterviewRepository.findMockInterviewById(mockInterviewId);
        if (mockInterview == null) {
            throw new ApiException("Mock interview with id " + mockInterviewId + " not found");
        }

        if (mockInterview.getMentor() == null ||
                !mockInterview.getMentor().getId().equals(mentorId)) {
            throw new ApiException("This mock interview does not belong to this mentor");
        }

        if (!mockInterview.getStatus().equals("PENDING")) {
            throw new ApiException("Only PENDING interviews can be rejected");
        }

        mockInterview.setStatus("REJECT");
        mockInterviewRepository.save(mockInterview);

        if (mockInterview.getStudent() != null) {
            Student student = mockInterview.getStudent();
            String message = "Hello " + student.getFullName() + ",\n\n"
                    + "Unfortunately, your mock interview request has been declined by "
                    + mentor.getFullName() + ".\n\n"
                    + "Please browse other available mentors and request a new session.\n\n"
                    + "Khutaa Team";
            whatsAppService.sendWhatsApp(student.getPhoneNumber(), message);
        }
    }


    public List<MentorMockInterviewDTOOUT> getPendingMentorInterviews(Integer mentorId) {

        findMentor(mentorId);

        List<MockInterview> mockInterviews =
                mockInterviewRepository.findMockInterviewsByMentorIdAndStatus(mentorId, "PENDING");

        List<MentorMockInterviewDTOOUT> dtoOuts = new ArrayList<>();

        for (MockInterview mockInterview : mockInterviews) {
            if ("MENTOR".equals(mockInterview.getInterviewMode())) {
                dtoOuts.add(toMentorDtoOut(mockInterview));
            }
        }

        return dtoOuts;
    }

    public MentorMockInterviewDTOOUT getMentorInterviewDetails(Integer mentorId, Integer mockInterviewId) {

        findMentor(mentorId);

        MockInterview mockInterview = findMockInterview(mockInterviewId);

        if (!"MENTOR".equals(mockInterview.getInterviewMode())) {
            throw new ApiException("This endpoint is only for mentor interviews");
        }

        if (mockInterview.getMentor() == null || !mockInterview.getMentor().getId().equals(mentorId)) {
            throw new ApiException("This mock interview does not belong to this mentor");
        }

        return toMentorDtoOut(mockInterview);
    }

    public void rescheduleMentorInterview(Integer mentorId, Integer mockInterviewId, MockInterviewRescheduleDTOIn dto) {

        Mentor mentor = mentorRepository.findMentorById(mentorId);
        if (mentor == null) {
            throw new ApiException("Mentor with id " + mentorId + " not found");
        }

        MockInterview mockInterview =
                mockInterviewRepository.findMockInterviewById(mockInterviewId);
        if (mockInterview == null) {
            throw new ApiException("Mock interview with id " + mockInterviewId + " not found");
        }

        if (mockInterview.getMentor() == null ||
                !mockInterview.getMentor().getId().equals(mentorId)) {
            throw new ApiException("This mock interview does not belong to this mentor");
        }

        if (!mockInterview.getStatus().equals("PENDING") &&
                !mockInterview.getStatus().equals("SCHEDULE")) {
            throw new ApiException("Only PENDING or SCHEDULE interviews can be rescheduled");
        }

        mockInterview.setScheduledAt(dto.getScheduledAt());
        mockInterview.setDurationMinutes(dto.getDurationMinutes());
        mockInterviewRepository.save(mockInterview);

        if (mockInterview.getStudent() != null) {
            Student student = mockInterview.getStudent();
            String message = "Hello " + student.getFullName() + ",\n\n"
                    + "Your mentor " + mentor.getFullName()
                    + " has proposed a new time for your mock interview.\n\n"
                    + "New Date & Time: " + dto.getScheduledAt() + "\n"
                    + "Duration: " + dto.getDurationMinutes() + " minutes\n\n"
                    + "Please review the new time on Khutaa.\n\n"
                    + "Khutaa Team";
            whatsAppService.sendWhatsApp(student.getPhoneNumber(), message);
        }
    }


    public void markStudentNoShow(Integer mentorId, Integer mockInterviewId) {

        Mentor mentor = mentorRepository.findMentorById(mentorId);
        if (mentor == null) {
            throw new ApiException("Mentor with id " + mentorId + " not found");
        }

        MockInterview mockInterview =
                mockInterviewRepository.findMockInterviewById(mockInterviewId);
        if (mockInterview == null) {
            throw new ApiException("Mock interview with id " + mockInterviewId + " not found");
        }

        if (mockInterview.getMentor() == null ||
                !mockInterview.getMentor().getId().equals(mentorId)) {
            throw new ApiException("This mock interview does not belong to this mentor");
        }

        if (!mockInterview.getStatus().equals("SCHEDULE")) {
            throw new ApiException("No-show can only be recorded for SCHEDULE interviews");
        }

        mockInterview.setStatus("CANCEL");
        mockInterviewRepository.save(mockInterview);

        if (mockInterview.getStudent() != null) {
            Student student = mockInterview.getStudent();
            String message = "Hello " + student.getFullName() + ",\n\n"
                    + "You were marked as a no-show for your mock interview with "
                    + mentor.getFullName() + " scheduled at "
                    + mockInterview.getScheduledAt() + ".\n\n"
                    + "Please schedule a new session at your earliest convenience.\n\n"
                    + "Khutaa Team";
            whatsAppService.sendWhatsApp(student.getPhoneNumber(), message);
        }
    }

    public AiInterviewQuestionsDTOOUT createAiInterview(Integer studentId, AiMockInterviewDTOIN dto) {

        Student student = findStudent(studentId);

        MockInterview mockInterview = new MockInterview();

        mockInterview.setInterviewMode("AI");
        mockInterview.setInterviewType(dto.getInterviewType());
        mockInterview.setDescription(dto.getDescription());
        mockInterview.setScheduledAt(LocalDateTime.now());
        mockInterview.setDurationMinutes(0);
        mockInterview.setStatus("SCHEDULE");
        mockInterview.setCreatedAt(LocalDateTime.now());
        mockInterview.setStudent(student);
        mockInterview.setMentor(null);

        mockInterview.setQuestions(generateAiInterviewQuestions(student, dto));
        mockInterview.setStudentAnswers(null);
        mockInterview.setFeedback(null);
        mockInterview.setScore(null);
        mockInterview.setUrl(null);
        mockInterview.setMeetingProvider(null);
        mockInterview.setExternalMeetingId(null);
        mockInterview.setWhatsappReminderSent(false);

        mockInterviewRepository.save(mockInterview);

        return new AiInterviewQuestionsDTOOUT(
                mockInterview.getId(),
                mockInterview.getInterviewType(),
                mockInterview.getDescription(),
                mockInterview.getQuestions(),
                mockInterview.getStatus()
        );
    }

    public AiMockInterviewReportDTOOUT submitAiInterviewAnswers(Integer studentId,
                                                                Integer mockInterviewId,
                                                                AiInterviewAnswerDTOIN dto) {

        Student student = findStudent(studentId);
        MockInterview mockInterview = findMockInterview(mockInterviewId);

        if (!"AI".equals(mockInterview.getInterviewMode())) {
            throw new ApiException("This endpoint is only for AI interviews");
        }

        if (mockInterview.getStudent() == null || !mockInterview.getStudent().getId().equals(studentId)) {
            throw new ApiException("This AI interview does not belong to this student");
        }

        if ("COMPLETE".equals(mockInterview.getStatus())) {
            throw new ApiException("This AI interview is already completed");
        }

        if (mockInterviewReportRepository.findMockInterviewReportByMockInterviewId(mockInterviewId) != null) {
            throw new ApiException("This AI interview already has a report");
        }

        mockInterview.setStudentAnswers(dto.getStudentAnswers());

        String json = evaluateAiInterviewAnswers(student, mockInterview, dto.getStudentAnswers());

        mockInterview.setFeedback(parseTextField(json, "feedback"));
        mockInterview.setScore(parseScore(json));
        mockInterview.setStatus("COMPLETE");

        mockInterviewRepository.save(mockInterview);

        MockInterviewReport report = new MockInterviewReport();

        report.setSummary(parseTextField(json, "summary"));
        report.setStrengths(parseTextField(json, "strengths"));
        report.setWeaknesses(parseTextField(json, "weaknesses"));
        report.setRecommendations(parseTextField(json, "recommendations"));
        report.setGeneratedAt(LocalDateTime.now());
        report.setStudent(student);
        report.setMockInterview(mockInterview);

        mockInterviewReportRepository.save(report);

        return toAiReportDtoOut(report, mockInterview);
    }

    public AiInterviewQuestionsDTOOUT getAiInterviewQuestions(Integer studentId, Integer mockInterviewId) {

        findStudent(studentId);

        MockInterview mockInterview = findMockInterview(mockInterviewId);

        if (!"AI".equals(mockInterview.getInterviewMode())) {
            throw new ApiException("This is not an AI interview");
        }

        if (mockInterview.getStudent() == null || !mockInterview.getStudent().getId().equals(studentId)) {
            throw new ApiException("This AI interview does not belong to this student");
        }

        return new AiInterviewQuestionsDTOOUT(
                mockInterview.getId(),
                mockInterview.getInterviewType(),
                mockInterview.getDescription(),
                mockInterview.getQuestions(),
                mockInterview.getStatus()
        );
    }

    public List<MockInterviewDTOOUT> getStudentAiInterviews(Integer studentId) {

        findStudent(studentId);

        List<MockInterview> mockInterviews =
                mockInterviewRepository.findMockInterviewsByStudentIdAndInterviewMode(studentId, "AI");

        List<MockInterviewDTOOUT> dtoOuts = new ArrayList<>();

        for (MockInterview mockInterview : mockInterviews) {
            dtoOuts.add(toDtoOut(mockInterview));
        }

        return dtoOuts;
    }

    public MockInterviewDTOOUT getById(Integer id) {

        MockInterview mockInterview = findMockInterview(id);

        return toDtoOut(mockInterview);
    }

    public List<MockInterviewDTOOUT> getAll() {

        List<MockInterview> mockInterviews = mockInterviewRepository.findAll();

        List<MockInterviewDTOOUT> dtoOuts = new ArrayList<>();

        for (MockInterview mockInterview : mockInterviews) {
            dtoOuts.add(toDtoOut(mockInterview));
        }

        return dtoOuts;
    }

    public void delete(Integer id) {

        MockInterview mockInterview = findMockInterview(id);

        mockInterviewRepository.delete(mockInterview);
    }

    private String generateMentorQuestions(Student student, Mentor mentor, MockInterviewDTOIN dto) {

        String cv = studentProfilePromptHelper.formatCvForPrompt(student);
        String github = studentProfilePromptHelper.formatGithubForPrompt(student);

        String prompt = """
                You are helping a mentor prepare for a mock interview.

                Respond with JSON only using this exact shape:
                {"questions": "Question 1...\\nQuestion 2...\\nQuestion 3...\\nQuestion 4...\\nQuestion 5..."}

                Generate 10 suggested questions. These questions are only suggestions for the mentor.

                Interview type: %s
                Interview description: %s

                Student name: %s
                Student major: %s
                Student target role: %s
                Student years of experience: %s
                Student readiness score: %s

                Mentor job title: %s
                Mentor specialization: %s
                Mentor years of experience: %s

                Student CV:
                %s

                Student GitHub:
                %s
                """.formatted(
                dto.getInterviewType(),
                dto.getDescription(),
                student.getFullName(),
                student.getMajor(),
                student.getTargetRole(),
                student.getYearsExperience(),
                student.getReadinessScore(),
                mentor.getJobTitle(),
                mentor.getSpecialization(),
                mentor.getYearsExperience(),
                cv,
                github
        );

        String json = aiService.ask(prompt);

        return parseQuestions(json);
    }

    private String generateAiInterviewQuestions(Student student, AiMockInterviewDTOIN dto) {

        String cv = studentProfilePromptHelper.formatCvForPrompt(student);
        String github = studentProfilePromptHelper.formatGithubForPrompt(student);

        String prompt = """
                You are an AI mock interviewer.

                Respond with JSON only using this exact shape:
                {"questions": "Question 1...\\nQuestion 2...\\nQuestion 3...\\nQuestion 4...\\nQuestion 5..."}

                Generate 10 interview questions for the student.
                The questions must match the interview type, target role, and student profile.

                Interview type: %s
                Interview description: %s

                Student name: %s
                Student major: %s
                Student target role: %s
                Student years of experience: %s
                Student readiness score: %s

                Student CV:
                %s

                Student GitHub:
                %s
                """.formatted(
                dto.getInterviewType(),
                dto.getDescription(),
                student.getFullName(),
                student.getMajor(),
                student.getTargetRole(),
                student.getYearsExperience(),
                student.getReadinessScore(),
                cv,
                github
        );

        String json = aiService.ask(prompt);

        return parseQuestions(json);
    }

    private String evaluateAiInterviewAnswers(Student student, MockInterview mockInterview, String studentAnswers) {

        String prompt = """
                You are an AI interview evaluator.

                Respond with JSON only using this exact shape:
                {
                  "feedback": "...",
                  "score": 0,
                  "summary": "...",
                  "strengths": "...",
                  "weaknesses": "...",
                  "recommendations": "..."
                }

                score must be an integer from 0 to 100.

                Evaluate the student's answers based on:
                - correctness
                - clarity
                - depth
                - relevance to the interview questions
                - readiness for the target role

                If core concepts are correct but brief, score 70-85 rather than below 60.
                Do not penalize concise answers when the main idea is right.

                Student name: %s
                Student major: %s
                Student target role: %s
                Student readiness score: %s

                Interview type: %s
                Interview description: %s

                Questions:
                %s

                Student answers:
                %s
                """.formatted(
                student.getFullName(),
                student.getMajor(),
                student.getTargetRole(),
                student.getReadinessScore(),
                mockInterview.getInterviewType(),
                mockInterview.getDescription(),
                mockInterview.getQuestions(),
                studentAnswers
        );

        return aiService.ask(prompt);
    }

    private void validateInterviewEndTimePassed(MockInterview mockInterview) {

        if (mockInterview.getScheduledAt() == null || mockInterview.getDurationMinutes() == null) {
            throw new ApiException("Mock interview schedule is incomplete");
        }

        LocalDateTime interviewEndTime =
                mockInterview.getScheduledAt().plusMinutes(mockInterview.getDurationMinutes());

        if (LocalDateTime.now().isBefore(interviewEndTime)) {
            throw new ApiException("Mock interview can be completed only after interview end time");
        }
    }

    private String parseQuestions(String json) {
        return AiJsonParser.requireText(AiJsonParser.parseObject(json), "questions");
    }

    private String parseTextField(String json, String fieldName) {
        return AiJsonParser.requireText(AiJsonParser.parseObject(json), fieldName);
    }

    private Integer parseScore(String json) {
        return AiJsonParser.requireInt(AiJsonParser.parseObject(json), "score", 0, 100);
    }

    private Student findStudent(Integer studentId) {

        Student student = studentRepository.findStudentById(studentId);

        if (student == null) {
            throw new ApiException("Student with id " + studentId + " not found");
        }

        return student;
    }

    private Mentor findMentor(Integer mentorId) {

        Mentor mentor = mentorRepository.findMentorById(mentorId);

        if (mentor == null) {
            throw new ApiException("Mentor with id " + mentorId + " not found");
        }

        return mentor;
    }

    private MockInterview findMockInterview(Integer mockInterviewId) {

        MockInterview mockInterview = mockInterviewRepository.findMockInterviewById(mockInterviewId);

        if (mockInterview == null) {
            throw new ApiException("Mock interview with id " + mockInterviewId + " not found");
        }

        return mockInterview;
    }

    private MockInterviewDTOOUT toDtoOut(MockInterview mockInterview) {

        Integer studentId = mockInterview.getStudent() != null ? mockInterview.getStudent().getId() : null;
        Integer mentorId = mockInterview.getMentor() != null ? mockInterview.getMentor().getId() : null;

        return new MockInterviewDTOOUT(
                mockInterview.getId(),
                mockInterview.getInterviewMode(),
                mockInterview.getInterviewType(),
                mockInterview.getDescription(),
                mockInterview.getScheduledAt(),
                mockInterview.getDurationMinutes(),
                mockInterview.getStatus(),
                mockInterview.getUrl(),
                mockInterview.getMeetingProvider(),
                mockInterview.getCreatedAt(),
                studentId,
                mentorId
        );
    }

    private MentorMockInterviewDTOOUT toMentorDtoOut(MockInterview mockInterview) {

        Student student = mockInterview.getStudent();
        Mentor mentor = mockInterview.getMentor();

        return new MentorMockInterviewDTOOUT(
                mockInterview.getId(),
                mockInterview.getInterviewMode(),
                mockInterview.getInterviewType(),
                mockInterview.getDescription(),
                mockInterview.getScheduledAt(),
                mockInterview.getDurationMinutes(),
                mockInterview.getStatus(),
                mockInterview.getQuestions(),
                mockInterview.getUrl(),
                mockInterview.getMeetingProvider(),
                mockInterview.getCreatedAt(),
                student != null ? student.getId() : null,
                student != null ? student.getFullName() : null,
                student != null ? student.getTargetRole() : null,
                mentor != null ? mentor.getId() : null,
                mentor != null ? mentor.getFullName() : null
        );
    }

    private AiMockInterviewReportDTOOUT toAiReportDtoOut(MockInterviewReport report, MockInterview mockInterview) {

        return new AiMockInterviewReportDTOOUT(
                report.getId(),
                mockInterview.getId(),
                mockInterview.getInterviewType(),
                mockInterview.getScore(),
                mockInterview.getFeedback(),
                report.getSummary(),
                report.getStrengths(),
                report.getWeaknesses(),
                report.getRecommendations(),
                report.getGeneratedAt()
        );
    }
}