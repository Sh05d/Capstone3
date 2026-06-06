package org.example.capstone_3.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
public class JobAnalysisDTOOut {

    private Integer id;

    private Integer studentId;

    private String jobTitle;

    private String jobDescription;

    private String strengths;

    private String missingSkillsText;

    private Integer matchScore;

    private Integer readinessScore;

    private String summary;

    private String weaknesses;

    private String recommendations;

    private LocalDateTime createdAt;

    private Set<SkillDTOOut> skills;
}
