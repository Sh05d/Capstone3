package org.example.capstone_3.DTO.OUT;
 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.time.LocalDateTime;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentInterviewHistoryDTOOUT {
 
    private Integer id;
    private String interviewMode;   // MENTOR | AI
    private String interviewType;
    private String status;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private Integer score;          // null للـ MENTOR interviews
    private String mentorName;      // null للـ AI interviews
    private LocalDateTime createdAt;
}