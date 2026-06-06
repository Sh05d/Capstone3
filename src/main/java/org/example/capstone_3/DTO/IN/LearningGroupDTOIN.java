package org.example.capstone_3.DTO.IN;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningGroupDTOIN {
    @NotEmpty(message = "Group name is required")
    private String name;

    @NotEmpty(message = "Focus area is required")
    private String focusArea;

    @NotEmpty(message = "Description is required")
    private String description;

    @NotEmpty(message = "Group type is required")
    @Pattern(regexp = "(?i)^(Public|Private)", message ="Group type is either Public or Private" )
    private String groupType;
}
