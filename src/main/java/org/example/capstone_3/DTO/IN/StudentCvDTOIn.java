package org.example.capstone_3.DTO.IN;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentCvDTOIn {

    @Pattern(
            regexp = "^(https?://).+$|^$",
            message = "CV URL must be a valid http(s) link to a PDF"
    )
    @Size(max = 2048, message = "CV URL must not exceed 2048 characters")
    private String cvUrl;
}
