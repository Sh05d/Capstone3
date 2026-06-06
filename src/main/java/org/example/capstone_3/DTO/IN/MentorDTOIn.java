package org.example.capstone_3.DTO.IN;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MentorDTOIn {

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

    @NotBlank(message = "Job title is required")
    @Size(min = 2, max = 80, message = "Job title must be between 2 and 80 characters")
    private String jobTitle;

    @NotBlank(message = "Company is required")
    @Size(min = 2, max = 80, message = "Company must be between 2 and 80 characters")
    private String company;

    @NotBlank(message = "Specialization is required")
    @Size(min = 2, max = 80, message = "Specialization must be between 2 and 80 characters")
    private String specialization;

    @NotNull(message = "Years experience is required")
    @Min(value = 0, message = "Years experience must be 0 or more")
    @Max(value = 50, message = "Years experience must not exceed 50")
    private Integer yearsExperience;

    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;

    @PositiveOrZero(message = "Session price must be 0 or more")
    private Double sessionPrice;
}