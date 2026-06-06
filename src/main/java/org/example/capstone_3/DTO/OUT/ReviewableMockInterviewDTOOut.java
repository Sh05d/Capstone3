package org.example.capstone_3.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewableMockInterviewDTOOut {
    private Integer mockInterviewId;
    private Integer mentorId;
    private String mentorName;
    private String interviewType;
    private LocalDateTime scheduledAt;
    private Integer score;
    private Boolean alreadyReviewed;
}
