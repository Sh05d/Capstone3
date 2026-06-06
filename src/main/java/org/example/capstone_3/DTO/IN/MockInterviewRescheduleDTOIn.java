package org.example.capstone_3.DTO.IN;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
public class MockInterviewRescheduleDTOIn {

    @NotNull(message = "Scheduled date and time are required")
//    @Future(message = "Scheduled date must be in the future")
    private LocalDateTime scheduledAt;

    @NotNull(message = "Duration is required")
    private Integer durationMinutes;

}
