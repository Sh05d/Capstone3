package org.example.capstone_3.DTO.OUT;
 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.time.LocalDateTime;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MentorScheduleDTOOUT {
 
    private Integer id;
    private String interviewType;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String studentName;
    private String studentTargetRole;
    private String url;
    private String meetingProvider;
}
 