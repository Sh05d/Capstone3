package org.example.capstone_3.DTO.IN;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentGithubDTOIn {

    @Pattern(
            regexp = "^(https?://)?(www\\.)?github\\.com/.*$|^$",
            message = "GitHub URL must be a valid github.com link"
    )
    @Size(max = 255, message = "GitHub URL must not exceed 255 characters")
    private String githubUrl;
}
