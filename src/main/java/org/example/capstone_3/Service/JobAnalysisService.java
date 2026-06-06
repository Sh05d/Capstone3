package org.example.capstone_3.Service;

import lombok.RequiredArgsConstructor;
import org.example.capstone_3.AI.AiJsonParser;
import org.example.capstone_3.AI.AiService;
import tools.jackson.databind.JsonNode;
import org.example.capstone_3.Api.ApiException;
import org.example.capstone_3.DTO.IN.JobAnalysisDTOIn;
import org.example.capstone_3.DTO.OUT.JobAnalysisDTOOut;
import org.example.capstone_3.DTO.OUT.SkillDTOOut;
import org.example.capstone_3.Model.JobAnalysis;
import org.example.capstone_3.Model.Skill;
import org.example.capstone_3.Model.Student;
import org.example.capstone_3.Repository.JobAnalysisRepository;
import org.example.capstone_3.Repository.SkillRepository;
import org.example.capstone_3.Repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobAnalysisService {

    private final JobAnalysisRepository jobAnalysisRepository;
    private final StudentRepository studentRepository;
    private final SkillRepository skillRepository;
    private final StudentProfilePromptHelper studentProfilePromptHelper;
    private final AiService aiService;

    /**
     * Add job analysis: load student → AI analysis → save JobAnalysis.
     */
    @Transactional
    public JobAnalysisDTOOut addJobAnalysis(Integer studentId, JobAnalysisDTOIn dto) {
        Student student = studentRepository.findStudentById(studentId);
        if (student == null) {
            throw new ApiException("Student with id " + studentId + " not found");
        }

        AiJobAnalysisResult aiResult = fetchJobAnalysisFromAi(student, dto.getJobDescription());

        JobAnalysis jobAnalysis = new JobAnalysis();
        jobAnalysis.setJobDescription(dto.getJobDescription());
        applyAiResult(jobAnalysis, aiResult);
        jobAnalysis.setReadinessScore(student.getReadinessScore());
        jobAnalysis.setStudent(student);
        jobAnalysis.setCreatedAt(LocalDateTime.now());

        jobAnalysis = jobAnalysisRepository.save(jobAnalysis);
        return toDtoOut(jobAnalysis);
    }

    /**
     * Update job analysis: load record + student → AI analysis → save.
     */
    @Transactional
    public JobAnalysisDTOOut updateJobAnalysis(Integer id, JobAnalysisDTOIn dto) {
        JobAnalysis jobAnalysis = jobAnalysisRepository.findJobAnalysisById(id);
        if (jobAnalysis == null) {
            throw new ApiException("Job analysis with id " + id + " not found");
        }

        Student linkedStudent = jobAnalysis.getStudent();
        if (linkedStudent == null) {
            throw new ApiException("Student not found for job analysis " + id);
        }

        Student student = studentRepository.findStudentById(linkedStudent.getId());
        if (student == null) {
            throw new ApiException("Student with id " + linkedStudent.getId() + " not found");
        }

        AiJobAnalysisResult aiResult = fetchJobAnalysisFromAi(student, dto.getJobDescription());

        jobAnalysis.setJobDescription(dto.getJobDescription());
        applyAiResult(jobAnalysis, aiResult);
        jobAnalysis.setReadinessScore(student.getReadinessScore());

        jobAnalysis = jobAnalysisRepository.save(jobAnalysis);
        return toDtoOut(jobAnalysis);
    }

    public JobAnalysisDTOOut create(Integer studentId, JobAnalysisDTOIn dto) {
        return addJobAnalysis(studentId, dto);
    }

    @Transactional(readOnly = true)
    public JobAnalysisDTOOut getById(Integer id) {
        JobAnalysis jobAnalysis = jobAnalysisRepository.findJobAnalysisById(id);
        if (jobAnalysis == null) {
            throw new ApiException("Job analysis with id " + id + " not found");
        }
        return toDtoOut(jobAnalysis);
    }

    @Transactional(readOnly = true)
    public List<JobAnalysisDTOOut> getByStudentId(Integer studentId) {
        Student student = studentRepository.findStudentById(studentId);
        if (student == null) {
            throw new ApiException("Student with id " + studentId + " not found");
        }

        List<JobAnalysisDTOOut> jobAnalysisDTOOuts = new ArrayList<>();
        for (JobAnalysis jobAnalysis : jobAnalysisRepository.findJobAnalysesByStudentId(studentId)) {
            jobAnalysisDTOOuts.add(toDtoOut(jobAnalysis));
        }
        return jobAnalysisDTOOuts;
    }

    public JobAnalysisDTOOut update(Integer id, JobAnalysisDTOIn dto) {
        return updateJobAnalysis(id, dto);
    }

    public void delete(Integer id) {
        JobAnalysis jobAnalysis = jobAnalysisRepository.findJobAnalysisById(id);
        if (jobAnalysis == null) {
            throw new ApiException("Job analysis with id " + id + " not found");
        }
        jobAnalysisRepository.delete(jobAnalysis);
    }

    private AiJobAnalysisResult fetchJobAnalysisFromAi(Student student, String jobDescription) {
        String prompt = buildJobAnalysisPrompt(student, jobDescription);
        String json = aiService.ask(prompt);
        return parseJobAnalysisResult(json);
    }

    private String buildJobAnalysisPrompt(Student student, String jobDescription) {
        String studentSkillNames = formatStudentSkills(student);
        String availableSkillNames = formatAvailableSkills();
        String cv = studentProfilePromptHelper.formatCvForPrompt(student);
        String github = studentProfilePromptHelper.formatGithubForPrompt(student);
        String profileGuidance = buildProfileGuidanceForAi(cv, github);

        return """
                You are a career coach. Compare the student profile to the job description.
                
                %s
                
                Respond with JSON only using this exact shape:
                {
                  "jobTitle": "inferred job title from the posting",
                  "matchScore": 0,
                  "missingSkills": "skills the student lacks for this role",
                  "summary": "2-4 sentence overview of fit",
                  "strengths": "student strengths relative to this job",
                  "weaknesses": "gaps or weaknesses relative to this job",
                  "recommendations": "3-5 actionable steps to improve fit for this job",
                  "skills": ["SkillName1", "SkillName2"]
                }
                matchScore must be an integer from 0 to 100.
                missingSkills, summary, strengths, weaknesses, and recommendations must each be a single JSON string value (not an array).
                The student's readinessScore (below) was already calculated for their target role using CV and profile data.
                Use it as a baseline for overall preparedness, but matchScore must reflect fit for THIS job posting specifically.
                skills must be a JSON array of skill names picked ONLY from the available skills list below.
                Do NOT invent new skill names. If none apply, return an empty array [].
                Include skills the student has that match the job, and/or key job skills that exist in the available list.
                
                --- Available skills (use exact names only) ---
                %s
                
                --- Student ---
                Name: %s
                Major: %s
                Target role: %s
                Years of experience: %s
                Readiness score (0-100): %s
                Student skills: %s
                
                --- CV ---
                %s
                
                --- GitHub ---
                %s
                
                --- Job description ---
                %s
                """.formatted(
                profileGuidance,
                availableSkillNames,
                student.getFullName(),
                student.getMajor(),
                student.getTargetRole(),
                student.getYearsExperience(),
                formatReadinessScore(student),
                studentSkillNames,
                cv,
                github,
                jobDescription
        );
    }

    private String buildProfileGuidanceForAi(String cv, String github) {
        boolean cvMissing = isEmptyProfileSection(cv);
        boolean githubMissing = isEmptyProfileSection(github);

        if (!cvMissing && !githubMissing) {
            return """
                    Profile completeness: ADEQUATE (CV and GitHub text are present).
                    Be rigorous and evidence-based. Do not inflate scores without support from the profile.
                    """;
        }

        StringBuilder guidance = new StringBuilder();
        guidance.append("""
                Profile completeness: WEAK — critical evidence is missing.
                You must treat this as an incomplete profile and be noticeably harsher and more skeptical than usual.
                Do not invent projects, tools, or experience that are not explicitly stated below.
                """);

        if (cvMissing) {
            guidance.append("- CV: NOT PROVIDED. Do not assume resume content, work history, or certifications.\n");
        }
        if (githubMissing) {
            guidance.append("- GitHub: NOT PROVIDED. Do not assume repositories, languages, or open-source work.\n");
        }

        guidance.append("""
                
                Required tone and scoring when profile is weak:
                - In summary, explicitly say the profile is incomplete and the assessment has low confidence.
                - Use direct, critical language in weaknesses (no sugarcoating).
                - matchScore must be conservative: typically 15-40 unless listed student skills alone clearly match the job.
                - If both CV and GitHub are missing, matchScore must not exceed 35 unless the gap is undeniable from skills text only.
                - recommendations must start with providing a CV and/or GitHub, then job-specific steps.
                """);

        return guidance.toString().trim();
    }

    private boolean isEmptyProfileSection(String section) {
        if (section == null || section.isBlank()) {
            return true;
        }
        String normalized = section.trim().toLowerCase();
        return normalized.startsWith("(no ")
                || normalized.contains("not provided");
    }

    private String formatReadinessScore(Student student) {
        if (student.getReadinessScore() == null) {
            return "0 (not yet calculated — treat as unknown baseline)";
        }
        return String.valueOf(student.getReadinessScore());
    }

    private String formatAvailableSkills() {
        List<Skill> skills = skillRepository.findAll();
        if (skills.isEmpty()) {
            return "(none in system — return empty skills array)";
        }
        return skills.stream()
                .map(Skill::getName)
                .collect(Collectors.joining(", "));
    }

    private String formatStudentSkills(Student student) {
        if (student.getSkills() == null || student.getSkills().isEmpty()) {
            return "(none listed)";
        }
        return student.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.joining(", "));
    }

    private AiJobAnalysisResult parseJobAnalysisResult(String json) {
        JsonNode node = AiJsonParser.parseObject(json);
        int matchScore = AiJsonParser.requireInt(node, "matchScore", 0, 100);
        String missingSkills = AiJsonParser.requireText(node, "missingSkills");
        String summary = AiJsonParser.requireText(node, "summary");
        String strengths = AiJsonParser.requireText(node, "strengths");
        String weaknesses = AiJsonParser.requireText(node, "weaknesses");
        String recommendations = AiJsonParser.requireText(node, "recommendations");
        String jobTitle = AiJsonParser.optionalText(node, "jobTitle");
        List<String> skillNames = AiJsonParser.optionalStringList(node, "skills");

        return new AiJobAnalysisResult(
                jobTitle == null ? "Job analysis" : jobTitle,
                matchScore,
                missingSkills,
                summary,
                strengths,
                weaknesses,
                recommendations,
                skillNames
        );
    }

    private Set<Skill> resolveSkillsFromNames(List<String> skillNames) {
        Map<String, Skill> skillsInDatabase = skillRepository.findAll().stream()
                .collect(Collectors.toMap(
                        skill -> skill.getName().trim().toLowerCase(),
                        skill -> skill,
                        (existing, duplicate) -> existing
                ));

        Set<Skill> skills = new HashSet<>();
        for (String name : skillNames) {
            String trimmed = name.trim();
            if (trimmed.isEmpty() || trimmed.length() > 50) {
                continue;
            }

            String key = trimmed.toLowerCase();
            Skill skill = skillsInDatabase.get(key);
            if (skill == null) {
                Skill existing = skillRepository.findSkillByName(trimmed);
                if (existing != null) {
                    skill = existing;
                } else {
                    Skill newSkill = new Skill();
                    newSkill.setName(trimmed);
                    newSkill.setCategory("Other");
                    skill = skillRepository.save(newSkill);
                }
                skillsInDatabase.put(key, skill);
            }
            skills.add(skill);
        }
        return skills;
    }

    private void applyAiResult(JobAnalysis jobAnalysis, AiJobAnalysisResult result) {
        jobAnalysis.setJobTitle(result.jobTitle());
        jobAnalysis.setMatchScore(result.matchScore());
        jobAnalysis.setMissingSkillsText(result.missingSkills());
        jobAnalysis.setStrengths(result.strengths());
        jobAnalysis.setSummary(result.summary());
        jobAnalysis.setWeaknesses(result.weaknesses());
        jobAnalysis.setRecommendations(result.recommendations());
        jobAnalysis.setSkills(resolveSkillsFromNames(result.skillNames()));
    }

    private JobAnalysisDTOOut toDtoOut(JobAnalysis jobAnalysis) {
        Integer studentId = jobAnalysis.getStudent() != null ? jobAnalysis.getStudent().getId() : null;
        return new JobAnalysisDTOOut(
                jobAnalysis.getId(),
                studentId,
                jobAnalysis.getJobTitle(),
                jobAnalysis.getJobDescription(),
                jobAnalysis.getStrengths(),
                jobAnalysis.getMissingSkillsText(),
                jobAnalysis.getMatchScore(),
                jobAnalysis.getReadinessScore(),
                jobAnalysis.getSummary(),
                jobAnalysis.getWeaknesses(),
                jobAnalysis.getRecommendations(),
                jobAnalysis.getCreatedAt(),
                mapSkills(jobAnalysis.getSkills())
        );
    }

    private Set<SkillDTOOut> mapSkills(Set<Skill> skills) {
        Set<SkillDTOOut> skillDTOOuts = new HashSet<>();
        if (skills == null) {
            return skillDTOOuts;
        }
        for (Skill skill : skills) {
            skillDTOOuts.add(new SkillDTOOut(skill.getId(), skill.getName(), skill.getCategory()));
        }
        return skillDTOOuts;
    }

    private record AiJobAnalysisResult(
            String jobTitle,
            int matchScore,
            String missingSkills,
            String summary,
            String strengths,
            String weaknesses,
            String recommendations,
            List<String> skillNames
    ) {
    }
}
