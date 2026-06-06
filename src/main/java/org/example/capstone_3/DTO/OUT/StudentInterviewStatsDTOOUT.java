package org.example.capstone_3.DTO.OUT;
 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentInterviewStatsDTOOUT {
 
    private Integer totalInterviews;
    private Integer mentorInterviews;
    private Integer aiInterviews;
    private Integer completedInterviews;
    private Double averageAiScore;      // null إذا ما أكمل أي AI interview
}