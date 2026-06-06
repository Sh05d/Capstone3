package org.example.capstone_3.DTO.IN;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MockInterviewReportDTOIN {

    @NotBlank(message = "Report summary is required")
    private String summary;

    @NotBlank(message = "Strengths are required")
    private String strengths;

    @NotBlank(message = "Weaknesses are required")
    private String weaknesses;

    @NotBlank(message = "Recommendations are required")
    private String recommendations;
}