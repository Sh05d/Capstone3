package org.example.capstone_3.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeDTOOUT {

    private Integer id;
    private String title;
    private String question;
    private Integer points;
    private String difficulty;
}
