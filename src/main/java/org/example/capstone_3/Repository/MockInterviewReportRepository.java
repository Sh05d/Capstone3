package org.example.capstone_3.Repository;

import org.example.capstone_3.Model.MockInterviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MockInterviewReportRepository extends JpaRepository<MockInterviewReport, Integer> {

    MockInterviewReport findMockInterviewReportById(Integer id);

    MockInterviewReport findMockInterviewReportByMockInterviewId(Integer mockInterviewId);
}