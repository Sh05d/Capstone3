package org.example.capstone_3.DTO.IN;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AiInterviewAnswerDTOIN {

    @NotBlank(message = "Student answers are required")
    private String studentAnswers;
}