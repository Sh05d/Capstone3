package org.example.capstone_3.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapDTOOUT {
    private Integer id;

    private String title;

    private String targetRole;

    private String skillGapSummary;

    private String recommendations;

    private Integer progressPercentage;

    private List<RoadmapStepDTOOUT> roadmapSteps;

    private LocalDateTime createdAt;

}
