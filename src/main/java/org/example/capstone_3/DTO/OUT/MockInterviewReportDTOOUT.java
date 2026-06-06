package org.example.capstone_3.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockInterviewReportDTOOUT {

    private Integer id;
    private String summary;
    private String strengths;
    private String weaknesses;
    private String recommendations;
    private LocalDateTime generatedAt;
    private Integer studentId;
    private Integer mockInterviewId;
}
