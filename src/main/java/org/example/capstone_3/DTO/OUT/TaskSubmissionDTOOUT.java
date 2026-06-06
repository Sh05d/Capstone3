package org.example.capstone_3.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSubmissionDTOOUT {
    private Integer id;

    private String taskTitle;

    private String answerText;

    private String aiFeedback;

    private Integer score;

    private LocalDateTime submittedAt;
}
