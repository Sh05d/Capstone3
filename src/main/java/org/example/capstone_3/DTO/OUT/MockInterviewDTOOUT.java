package org.example.capstone_3.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockInterviewDTOOUT {

    private Integer id;
    private String interviewMode;
    private String interviewType;
    private String description;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String status;
    private String url;
    private String meetingProvider;
    private LocalDateTime createdAt;
    private Integer studentId;
    private Integer mentorId;
}