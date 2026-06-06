package org.example.capstone_3.DTO.IN;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillCategoryDTOIn {

    @NotBlank(message = "Category is required")
    @Size(min = 2, max = 50, message = "Category must be between 2 and 50 characters")
    @Pattern(
            regexp = "^(Programming|Database|Testing|DevOps|Soft Skill|AI|Security|Other)$",
            message = "Category must be one of: Programming, Database, Testing, DevOps, Soft Skill, AI, Security, Other"
    )
    private String category;
}
