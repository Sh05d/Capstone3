package org.example.capstone_3.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapStepDTOOUT {
    private Integer id;

    private String title;

    private String description;

    private Integer orderNumber;

    private Boolean completed;

    private String skillName;

    private LocalDateTime completedAt;
}
