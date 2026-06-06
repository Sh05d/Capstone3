package org.example.capstone_3.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiMockInterviewReportDTOOUT {

    private Integer reportId;
    private Integer mockInterviewId;
    private String interviewType;
    private Integer score;
    private String feedback;
    private String summary;
    private String strengths;
    private String weaknesses;
    private String recommendations;
    private LocalDateTime generatedAt;
}