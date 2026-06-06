package org.example.capstone_3.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaderboardRankDTOOut {

    private Integer rank;

    private Integer studentId;

    private String fullName;

    private Integer xp;

    private Long totalStudents;
}
