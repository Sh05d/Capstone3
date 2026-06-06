package org.example.capstone_3.DTO.IN;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDTOIn {

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 50, message = "Full name must be between 3 and 50 characters")
    private String fullName;

    @NotEmpty(message = "Phone number is required")
    @Pattern(regexp = "^9665\\d{8}$", message = "Number should be valid Saudi number start with 966")
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Password must contain at least one letter and one number"
    )
    private String password;

    @NotBlank(message = "Major is required")
    @Size(min = 2, max = 60, message = "Major must be between 2 and 60 characters")
    private String major;

    @NotBlank(message = "Target role is required")
    @Size(min = 2, max = 80, message = "Target role must be between 2 and 80 characters")
    private String targetRole;

    @NotNull(message = "Years experience is required")
    @Min(value = 0, message = "Years experience must be 0 or more")
    @Max(value = 40, message = "Years experience must not exceed 40")
    private Integer yearsExperience;

    @Pattern(
            regexp = "^(https?://)?(www\\.)?linkedin\\.com/.*$|^$",
            message = "LinkedIn URL must be valid"
    )
    private String linkedinUrl;

    @Pattern(
            regexp = "^(https?://)?(www\\.)?github\\.com/.*$|^$",
            message = "GitHub URL must be valid"
    )
    private String githubUrl;

    @Pattern(
            regexp = "^(https?://).+$|^$",
            message = "CV URL must be a valid http(s) link to a PDF"
    )
    @Size(max = 2048, message = "CV URL must not exceed 2048 characters")
    private String cvUrl;
}