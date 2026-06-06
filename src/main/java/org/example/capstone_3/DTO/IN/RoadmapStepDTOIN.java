package org.example.capstone_3.DTO.IN;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapStepDTOIN {
    @NotEmpty(message = "Title is required")
    private String title;

    @NotEmpty(message = "Description is required")
    private String description;

    @NotNull(message = "Order number is required")
    @Min(value = 1, message = "Order number must be 1 or more")
    private Integer orderNumber;

    @NotNull(message = "Skill id is required")
    @Min(value = 1, message = "Skill id must be a positive number")
    private Integer skillId;
}
