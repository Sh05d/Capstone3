package org.example.capstone_3.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SkillCategoryGenerationDTOOut {

    private String category;

    private List<SkillDTOOut> savedSkills;

    private List<SkillDTOOut> existingSkills;
}
