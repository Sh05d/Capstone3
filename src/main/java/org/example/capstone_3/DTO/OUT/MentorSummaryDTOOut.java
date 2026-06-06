package org.example.capstone_3.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MentorSummaryDTOOut {

    private Integer id;

    private String fullName;

    private String jobTitle;

    private String company;

    private String specialization;

    private Double rating;
}