package org.example.capstone_3.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JobAnalysisSummaryDTOOut {

    private Integer id;

    private String jobTitle;

    private Integer matchScore;

    private String missingSkillsText;
}