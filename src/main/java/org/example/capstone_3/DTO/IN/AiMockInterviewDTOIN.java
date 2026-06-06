package org.example.capstone_3.DTO.IN;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AiMockInterviewDTOIN {

    @NotBlank(message = "Interview type is required")
    private String interviewType;

    @NotBlank(message = "Description is required")
    private String description;
}