package org.example.capstone_3.Service;

import org.example.capstone_3.Model.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentProfilePromptHelper {

    public String formatCvForPrompt(Student student) {
        if (student.getCvText() == null || student.getCvText().isBlank()) {
            return "(no CV provided)";
        }
        return student.getCvText();
    }

    public String formatGithubForPrompt(Student student) {
        if (student.getGithubText() == null || student.getGithubText().isBlank()) {
            return "(no GitHub profile provided)";
        }
        return student.getGithubText();
    }
}
