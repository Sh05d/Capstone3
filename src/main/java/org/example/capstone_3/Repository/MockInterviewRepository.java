package org.example.capstone_3.Repository;

import org.example.capstone_3.Model.MockInterview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockInterviewRepository extends JpaRepository<MockInterview, Integer> {

    MockInterview findMockInterviewById(Integer id);

    List<MockInterview> findMockInterviewsByMentorIdAndStatus(Integer mentorId, String status);

    List<MockInterview> findMockInterviewsByStudentId(Integer studentId);

    List<MockInterview> findMockInterviewsByStudentIdAndInterviewMode(Integer studentId, String interviewMode);

    List<MockInterview> findMockInterviewsByStudentIdAndInterviewModeAndStatus(
            Integer studentId, String interviewMode, String status);

    List<MockInterview> findMockInterviewsByStatus(String status);

    // ── الجديدة ──────────────────────────────────────────────────────────────

    List<MockInterview> findMockInterviewsByStudentIdOrderByCreatedAtDesc(Integer studentId);

    List<MockInterview> findMockInterviewsByMentorIdAndStatusOrderByScheduledAtAsc(
            Integer mentorId, String status);

    @Query("SELECT m.interviewMode, COUNT(m) FROM MockInterview m " +
            "WHERE m.student.id = :studentId GROUP BY m.interviewMode")
    List<Object[]> countInterviewsByModeForStudent(@Param("studentId") Integer studentId);

    @Query("SELECT AVG(m.score) FROM MockInterview m " +
            "WHERE m.student.id = :studentId AND m.interviewMode = 'AI' AND m.score IS NOT NULL")
    Double avgScoreForStudent(@Param("studentId") Integer studentId);

    List<MockInterview> findMockInterviewsByMentorIdOrderByCreatedAtDesc(Integer mentorId);

    @Query("SELECT m FROM MockInterview m " +
            "WHERE m.student.id = :studentId AND m.status = 'COMPLETE' " +
            "ORDER BY m.scheduledAt ASC")
    List<MockInterview> findCompletedInterviewsByStudentId(@Param("studentId") Integer studentId);
}