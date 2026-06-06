package org.example.capstone_3.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningGroupDTOOUT {
    private Integer id;

    private String name;

    private String focusArea;

    private String description;

    private LocalDateTime createdAt;
}
