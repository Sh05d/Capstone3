package org.example.capstone_3.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTOOUT {

    private Integer id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private Integer studentId;
    private Integer mentorId;
    private Integer mockInterviewId;
}