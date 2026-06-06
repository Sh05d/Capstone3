package org.example.capstone_3.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeAttemptDTOOUT {

    private Integer id;
    private String submittedAnswer;
    private Boolean correct;
    private LocalDateTime submittedAt;
}
