package org.example.capstone_3.DTO.IN;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobAnalysisDTOIn {

    @NotBlank(message = "Job description is required")
    @Size(min = 50, max = 10000, message = "Job description must be between 50 and 10000 characters")
    private String jobDescription;
}