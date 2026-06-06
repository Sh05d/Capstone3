package org.example.capstone_3.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiInterviewQuestionsDTOOUT {

    private Integer mockInterviewId;
    private String interviewType;
    private String description;
    private String questions;
    private String status;
}