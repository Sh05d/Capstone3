package org.example.capstone_3.Service;

import lombok.RequiredArgsConstructor;
import org.example.capstone_3.AI.AiException;
import org.example.capstone_3.AI.AiService;
import org.example.capstone_3.Api.ApiException;
import org.example.capstone_3.DTO.IN.StudentCvDTOIn;
import org.example.capstone_3.DTO.IN.StudentDTOIn;
import org.example.capstone_3.DTO.IN.StudentGithubDTOIn;
import org.example.capstone_3.DTO.OUT.*;
import org.example.capstone_3.Model.Skill;
import org.example.capstone_3.Model.Student;
import org.example.capstone_3.Repository.SkillRepository;
import org.example.capstone_3.Repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class StudentService {

    private static final Pattern READINESS_SCORE_PATTERN =
            Pattern.compile("\"readinessScore\"\\s*:\\s*(\\d+)");

    private final StudentRepository studentRepository;
    private final SkillRepository skillRepository;
    private final CvExtractionService cvExtractionService;
    private final GithubProfileService githubProfileService;
    private final StudentProfilePromptHelper studentProfilePromptHelper;
    private final AiService aiService;

    /**
     * Add student: validate → AI readiness score → save.
     * Request body validation is done in the controller ({@code @Valid}).
     */
    public void addStudent(StudentDTOIn dto) {
        if (studentRepository.findStudentByEmail(dto.getEmail()) != null) {
            throw new ApiException("Email already exists");
        }
        if (studentRepository.findStudentByPhoneNumber(dto.getPhoneNumber()) != null) {
            throw new ApiException("Phone number already exists");
        }

        Student student = new Student();
        applyDto(student, dto);
        applyCvUrl(student, dto.getCvUrl());
        applyGithubUrl(student, dto.getGithubUrl());
        student.setXp(0);
        student.setCreatedAt(LocalDateTime.now());
        student.setSkills(new HashSet<>());
        student.setReadinessScore(fetchReadinessScoreFromAi(student));

        studentRepository.save(student);
    }

    /**
     * Update student: validate → AI readiness score → save.
     */
    public void updateStudent(Integer id, StudentDTOIn dto) {
        Student student = studentRepository.findStudentById(id);

        if (student == null) {
            throw new ApiException("Student with id " + id + " not found");
        }

        Student emailOwner = studentRepository.findStudentByEmail(dto.getEmail());

        if (emailOwner != null && !emailOwner.getId().equals(id)) {
            throw new ApiException("Email already exists");
        }

        if (studentRepository.findStudentByPhoneNumber(dto.getPhoneNumber()) != null) {
            throw new ApiException("Phone number already exists");
        }

        applyDto(student, dto);
        applyCvUrl(student, dto.getCvUrl());
        applyGithubUrl(student, dto.getGithubUrl());
        student.setReadinessScore(fetchReadinessScoreFromAi(student));

        studentRepository.save(student);
    }

    public void create(StudentDTOIn dto) {
        addStudent(dto);
    }

    public StudentDTOOut getById(Integer id) {

        Student student = studentRepository.findStudentById(id);

        if (student == null) {
            throw new ApiException("Student with id " + id + " not found");
        }

        return toDtoOut(student);
    }

    public List<StudentDTOOut> getAll() {

        List<Student> students = studentRepository.findAll();

        List<StudentDTOOut> studentDTOOuts = new ArrayList<>();

        for (Student student : students) {
            studentDTOOuts.add(toDtoOut(student));
        }

        return studentDTOOuts;
    }

    public List<LeaderboardEntryDTOOut> getLeaderboard() {
        List<Student> students = studentRepository.findAllByOrderByXpDescFullNameAsc();
        List<LeaderboardEntryDTOOut> entries = new ArrayList<>();
        int position = 0;
        int rank = 0;
        Integer previousXp = null;

        for (Student student : students) {
            position++;
            int xp = student.getXp() != null ? student.getXp() : 0;
            if (previousXp == null || xp < previousXp) {
                rank = position;
                previousXp = xp;
            }
            entries.add(new LeaderboardEntryDTOOut(
                    rank,
                    student.getId(),
                    student.getFullName(),
                    xp
            ));
        }
        return entries;
    }

    public LeaderboardRankDTOOut getLeaderboardRank(Integer studentId) {
        Student student = studentRepository.findStudentById(studentId);
        if (student == null) {
            throw new ApiException("Student with id " + studentId + " not found");
        }

        int xp = student.getXp() != null ? student.getXp() : 0;
        int rank = (int) studentRepository.countByXpGreaterThan(xp) + 1;
        long totalStudents = studentRepository.count();

        return new LeaderboardRankDTOOut(
                rank,
                student.getId(),
                student.getFullName(),
                xp,
                totalStudents
        );
    }

    public void update(Integer id, StudentDTOIn dto) {
        updateStudent(id, dto);
    }

    public void updateCv(Integer id, StudentCvDTOIn dto) {
        Student student = studentRepository.findStudentById(id);
        if (student == null) {
            throw new ApiException("Student with id " + id + " not found");
        }

        String normalizedNew = normalizeCvUrl(dto.getCvUrl());
        String normalizedCurrent = normalizeCvUrl(student.getCvUrl());
        if (normalizedNew != null && normalizedNew.equals(normalizedCurrent)) {
            return;
        }

        applyCvUrl(student, dto.getCvUrl());
        student.setReadinessScore(fetchReadinessScoreFromAi(student));
        studentRepository.save(student);
    }

    public void updateGithub(Integer id, StudentGithubDTOIn dto) {
        Student student = studentRepository.findStudentById(id);
        if (student == null) {
            throw new ApiException("Student with id " + id + " not found");
        }

        String keyNew = githubProfileService.canonicalUsernameKey(dto.getGithubUrl());
        String keyCurrent = githubProfileService.canonicalUsernameKey(student.getGithubUrl());
        if (keyNew != null && keyNew.equals(keyCurrent)) {
            return;
        }

        applyGithubUrl(student, dto.getGithubUrl());
        student.setReadinessScore(fetchReadinessScoreFromAi(student));
        studentRepository.save(student);
    }

    public void delete(Integer id) {

        Student student = studentRepository.findStudentById(id);

        if (student == null) {
            throw new ApiException("Student with id " + id + " not found");
        }

        studentRepository.delete(student);
    }

    public void addSkillToStudent(Integer studentId, Integer skillId) {
        Student student = studentRepository.findStudentById(studentId);
        if (student == null) {
            throw new ApiException("Student with id " + studentId + " not found");
        }

        Skill skill = skillRepository.findSkillById(skillId);
        if (skill == null) {
            throw new ApiException("Skill with id " + skillId + " not found");
        }

        if (student.getSkills() == null) {
            student.setSkills(new HashSet<>());
        }

        boolean alreadyHasSkill = student.getSkills().stream()
                .anyMatch(existing -> existing.getId().equals(skillId));
        if (alreadyHasSkill) {
            throw new ApiException("Student already has this skill");
        }

        student.getSkills().add(skill);
        studentRepository.save(student);
    }

    public void removeSkillFromStudent(Integer studentId, Integer skillId) {
        Student student = studentRepository.findStudentById(studentId);
        if (student == null) {
            throw new ApiException("Student with id " + studentId + " not found");
        }

        if (student.getSkills() == null || student.getSkills().isEmpty()) {
            throw new ApiException("Student does not have any skills");
        }

        boolean removed = student.getSkills().removeIf(skill -> skill.getId().equals(skillId));
        if (!removed) {
            throw new ApiException("Student does not have this skill");
        }

        studentRepository.save(student);
    }
/**
 * هنا الطلب من ال AI service
 */

    private int fetchReadinessScoreFromAi(Student student) {
        String prompt = buildReadinessPrompt(student);
        String json = aiService.ask(prompt);
        return parseReadinessScore(json);
    }

    private String buildReadinessPrompt(Student student) {
        return """
                You are a career advisor for university students.
                Estimate job readiness for the target role based on the profile below.
                
                Respond with JSON only using this exact shape:
                {"readinessScore": 0}
                
                readinessScore must be an integer from 0 to 100.
                
                Major: %s
                Target role: %s
                Years of experience: %s
                
                --- CV ---
                %s
                
                --- GitHub ---
                %s
                """.formatted(
                student.getMajor(),
                student.getTargetRole(),
                student.getYearsExperience(),
                studentProfilePromptHelper.formatCvForPrompt(student),
                studentProfilePromptHelper.formatGithubForPrompt(student)
        );
    }

    private void applyCvUrl(Student student, String newCvUrl) {
        String normalizedNew = normalizeCvUrl(newCvUrl);
        String normalizedCurrent = normalizeCvUrl(student.getCvUrl());

        if (normalizedNew != null && normalizedNew.equals(normalizedCurrent)) {
            return;
        }

        student.setCvUrl(normalizedNew);
        if (normalizedNew == null) {
            student.setCvText(null);
            return;
        }

        student.setCvText(cvExtractionService.extractTextFromPdfUrl(normalizedNew));
    }

    private String normalizeCvUrl(String cvUrl) {
        if (cvUrl == null || cvUrl.isBlank()) {
            return null;
        }
        return cvUrl.trim();
    }

    private void applyGithubUrl(Student student, String newGithubUrl) {
        String keyNew = githubProfileService.canonicalUsernameKey(newGithubUrl);
        String keyCurrent = githubProfileService.canonicalUsernameKey(student.getGithubUrl());

        if (keyNew != null && keyNew.equals(keyCurrent)) {
            return;
        }

        String normalizedNew = githubProfileService.normalizeGithubUrl(newGithubUrl);
        student.setGithubUrl(normalizedNew);
        if (normalizedNew == null) {
            student.setGithubText(null);
            return;
        }

        student.setGithubText(githubProfileService.fetchProfileSummary(normalizedNew));
    }

    /**
     * ذا بياخذ الاجابه ويربطها 
     */
    private int parseReadinessScore(String json) {
        Matcher matcher = READINESS_SCORE_PATTERN.matcher(json);
        if (!matcher.find()) {
            throw new AiException("AI response did not contain readinessScore.");
        }
        int score = Integer.parseInt(matcher.group(1));
        if (score < 0 || score > 100) {
            throw new AiException("AI readinessScore must be between 0 and 100.");
        }
        return score;
    }

    private void applyDto(Student student, StudentDTOIn dto) {
        student.setFullName(dto.getFullName());
        student.setPhoneNumber(dto.getPhoneNumber());
        student.setEmail(dto.getEmail());
        student.setPassword(dto.getPassword());
        student.setMajor(dto.getMajor());
        student.setTargetRole(dto.getTargetRole());
        student.setYearsExperience(dto.getYearsExperience());
        student.setLinkedinUrl(dto.getLinkedinUrl());
    }

    private StudentDTOOut toDtoOut(Student student) {
        return new StudentDTOOut(
                student.getId(),
                student.getFullName(),
                student.getPhoneNumber(),
                student.getEmail(),
                student.getMajor(),
                student.getTargetRole(),
                student.getYearsExperience(),
                student.getLinkedinUrl(),
                student.getGithubUrl(),
                student.getCvUrl(),
                student.getXp(),
                student.getReadinessScore(),
                mapSkills(student.getSkills())
        );
    }

    private Set<SkillDTOOut> mapSkills(Set<Skill> skills) {

        Set<SkillDTOOut> skillDTOOuts = new HashSet<>();

        if (skills == null) {
            return skillDTOOuts;
        }

        for (Skill skill : skills) {
            SkillDTOOut skillDTOOut = new SkillDTOOut(
                    skill.getId(),
                    skill.getName(),
                    skill.getCategory()
            );

            skillDTOOuts.add(skillDTOOut);
        }

        return skillDTOOuts;
    }
}
