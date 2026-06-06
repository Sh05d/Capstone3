package org.example.capstone_3.Service;

import lombok.RequiredArgsConstructor;
import org.example.capstone_3.Api.ApiException;
import org.example.capstone_3.DTO.IN.MockInterviewReportDTOIN;
import org.example.capstone_3.DTO.OUT.MockInterviewReportDTOOUT;
import org.example.capstone_3.DTO.OUT.AiMockInterviewReportDTOOUT;
import org.example.capstone_3.Model.Mentor;
import org.example.capstone_3.Model.MockInterview;
import org.example.capstone_3.Model.MockInterviewReport;
import org.example.capstone_3.Repository.MentorRepository;
import org.example.capstone_3.Repository.MockInterviewReportRepository;
import org.example.capstone_3.Repository.MockInterviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MockInterviewReportService {

    private final MockInterviewReportRepository mockInterviewReportRepository;
    private final MockInterviewRepository mockInterviewRepository;
    private final MentorRepository mentorRepository;
    private final EmailService emailService;

    public void create(Integer mentorId, Integer mockInterviewId, MockInterviewReportDTOIN dto) {

        Mentor mentor = mentorRepository.findMentorById(mentorId);

        if (mentor == null) {
            throw new ApiException("Mentor with id " + mentorId + " not found");
        }

        MockInterview mockInterview = mockInterviewRepository.findMockInterviewById(mockInterviewId);

        if (mockInterview == null) {
            throw new ApiException("Mock interview with id " + mockInterviewId + " not found");
        }

        if (!mockInterview.getInterviewMode().equals("MENTOR")) {
            throw new ApiException("Report can be created only for mentor interviews");
        }

        if (mockInterview.getMentor() == null || !mockInterview.getMentor().getId().equals(mentorId)) {
            throw new ApiException("This mock interview does not belong to this mentor");
        }

        completeInterviewIfTimeEnded(mockInterview);

        if (!mockInterview.getStatus().equals("COMPLETE")) {
            throw new ApiException("Report can be created only after the interview is completed");
        }

        if (mockInterview.getStudent() == null) {
            throw new ApiException("Mock interview is not linked to a student");
        }

        if (mockInterviewReportRepository.findMockInterviewReportByMockInterviewId(mockInterviewId) != null) {
            throw new ApiException("This mock interview already has a report");
        }

        MockInterviewReport report = new MockInterviewReport();

        report.setSummary(dto.getSummary());
        report.setStrengths(dto.getStrengths());
        report.setWeaknesses(dto.getWeaknesses());
        report.setRecommendations(dto.getRecommendations());
        report.setGeneratedAt(LocalDateTime.now());
        report.setStudent(mockInterview.getStudent());
        report.setMockInterview(mockInterview);

        mockInterviewReportRepository.save(report);

        emailService.sendMockInterviewReportEmail(
                mockInterview.getStudent(),
                mentor,
                mockInterview,
                report
        );
    }

    public AiMockInterviewReportDTOOUT getAiReport(Integer studentId, Integer mockInterviewId) {

        MockInterview mockInterview = mockInterviewRepository.findMockInterviewById(mockInterviewId);

        if (mockInterview == null) {
            throw new ApiException("Mock interview with id " + mockInterviewId + " not found");
        }

        if (!"AI".equals(mockInterview.getInterviewMode())) {
            throw new ApiException("This report is only for AI interviews");
        }

        if (mockInterview.getStudent() == null || !mockInterview.getStudent().getId().equals(studentId)) {
            throw new ApiException("This AI interview does not belong to this student");
        }

        MockInterviewReport report =
                mockInterviewReportRepository.findMockInterviewReportByMockInterviewId(mockInterviewId);

        if (report == null) {
            throw new ApiException("AI report has not been generated yet");
        }

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

    public MockInterviewReportDTOOUT getById(Integer id) {

        MockInterviewReport report = mockInterviewReportRepository.findMockInterviewReportById(id);

        if (report == null) {
            throw new ApiException("Mock interview report with id " + id + " not found");
        }

        return toDtoOut(report);
    }

    public List<MockInterviewReportDTOOUT> getAll() {

        List<MockInterviewReport> reports = mockInterviewReportRepository.findAll();

        List<MockInterviewReportDTOOUT> reportDTOOUTS = new ArrayList<>();

        for (MockInterviewReport report : reports) {
            reportDTOOUTS.add(toDtoOut(report));
        }

        return reportDTOOUTS;
    }

    public void update(Integer id, MockInterviewReportDTOIN dto) {

        MockInterviewReport report = mockInterviewReportRepository.findMockInterviewReportById(id);

        if (report == null) {
            throw new ApiException("Mock interview report with id " + id + " not found");
        }

        report.setSummary(dto.getSummary());
        report.setStrengths(dto.getStrengths());
        report.setWeaknesses(dto.getWeaknesses());
        report.setRecommendations(dto.getRecommendations());
        report.setGeneratedAt(LocalDateTime.now());

        mockInterviewReportRepository.save(report);

        if (report.getStudent() != null &&
                report.getMockInterview() != null &&
                report.getMockInterview().getMentor() != null) {

            emailService.sendMockInterviewReportEmail(
                    report.getStudent(),
                    report.getMockInterview().getMentor(),
                    report.getMockInterview(),
                    report
            );
        }
    }

    public void delete(Integer id) {

        MockInterviewReport report = mockInterviewReportRepository.findMockInterviewReportById(id);

        if (report == null) {
            throw new ApiException("Mock interview report with id " + id + " not found");
        }

        mockInterviewReportRepository.delete(report);
    }

    private void completeInterviewIfTimeEnded(MockInterview mockInterview) {

        if (mockInterview.getScheduledAt() == null || mockInterview.getDurationMinutes() == null) {
            throw new ApiException("Interview schedule information is missing");
        }

        if (!mockInterview.getStatus().equals("SCHEDULE") && !mockInterview.getStatus().equals("COMPLETE")) {
            throw new ApiException("Only scheduled or completed interviews can have reports");
        }

        LocalDateTime endTime = mockInterview.getScheduledAt()
                .plusMinutes(mockInterview.getDurationMinutes());

        if (LocalDateTime.now().isBefore(endTime)) {
            throw new ApiException("Interview time has not ended yet. Mentor cannot create report now");
        }

        if (mockInterview.getStatus().equals("SCHEDULE")) {
            mockInterview.setStatus("COMPLETE");
            mockInterviewRepository.save(mockInterview);
        }
    }

    private MockInterviewReportDTOOUT toDtoOut(MockInterviewReport report) {

        Integer studentId = report.getStudent() != null ? report.getStudent().getId() : null;

        Integer mockInterviewId = report.getMockInterview() != null
                ? report.getMockInterview().getId()
                : null;

        return new MockInterviewReportDTOOUT(
                report.getId(),
                report.getSummary(),
                report.getStrengths(),
                report.getWeaknesses(),
                report.getRecommendations(),
                report.getGeneratedAt(),
                studentId,
                mockInterviewId
        );
    }
}