package org.example.capstone_3.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentRoadmapStepDTOOut {
    private Integer roadmapId;
    private String roadmapTitle;
    private String targetRole;
    private Integer progressPercentage;
    private RoadmapStepDTOOUT currentStep;
    private Boolean allStepsCompleted;
    private Integer totalSteps;
    private Integer completedSteps;
}
