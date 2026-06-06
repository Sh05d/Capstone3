package org.example.capstone_3.Service;

import lombok.RequiredArgsConstructor;
import org.example.capstone_3.AI.AiException;
import org.example.capstone_3.AI.AiService;
import org.example.capstone_3.Api.ApiException;
import org.example.capstone_3.DTO.OUT.RoadmapDTOOUT;
import org.example.capstone_3.DTO.OUT.RoadmapStepDTOOUT;
import org.example.capstone_3.Model.Roadmap;
import org.example.capstone_3.Model.RoadmapStep;
import org.example.capstone_3.Model.Skill;
import org.example.capstone_3.Model.Student;
import org.example.capstone_3.Repository.RoadmapRepository;
import org.example.capstone_3.Repository.RoadmapStepRepository;
import org.example.capstone_3.Repository.SkillRepository;
import org.example.capstone_3.Repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoadmapService {

    private static final Pattern STEP_OBJECT_PATTERN = Pattern.compile(
            "\\{\\s*\"title\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"\\s*,\\s*"
                    + "\"description\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"\\s*,\\s*"
                    + "\"orderNumber\"\\s*:\\s*(\\d+)\\s*,\\s*"
                    + "\"skillName\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"\\s*\\}",
            Pattern.DOTALL);

    private final RoadmapRepository roadmapRepository;
    private final RoadmapStepRepository roadmapStepRepository;
    private final StudentRepository studentRepository;
    private final SkillRepository skillRepository;
    private final RoadmapStepService roadmapStepService;
    private final StudentProfilePromptHelper studentProfilePromptHelper;
    private final AiService aiService;

    @Transactional
    public RoadmapDTOOUT createRoadmap(Integer studentId) {
        Student student = findStudent(studentId);
        String targetRole = student.getTargetRole();
        String studentSkills = formatStudentSkills(student);
        String availableSkills = formatAvailableSkills();

        AiRoadmapOverview overview = fetchRoadmapOverviewFromAi(student, targetRole, studentSkills, availableSkills);
        List<AiRoadmapStep> steps = fetchRoadmapStepsFromAi(
                student, targetRole, studentSkills, availableSkills, overview);

        Roadmap roadmap = new Roadmap();
        roadmap.setTitle(overview.title());
        roadmap.setTargetRole(targetRole);
        roadmap.setSkillGapSummary(overview.skillGapSummary());
        roadmap.setProgressPercentage(0);
        roadmap.setCreatedAt(LocalDateTime.now());
        roadmap.setStudent(student);
        roadmap = roadmapRepository.save(roadmap);

        saveGeneratedSteps(roadmap, steps);

        return getRoadmapById(roadmap.getId());
    }

    @Transactional
    public RoadmapDTOOUT updateRoadmap(Integer id) {
        Roadmap roadmap = roadmapRepository.findRoadmapById(id);
        if (roadmap == null) {
            throw new ApiException("Roadmap not found");
        }

        Student student = roadmap.getStudent();
        if (student == null) {
            throw new ApiException("Student not found for roadmap " + id);
        }

        String targetRole = student.getTargetRole();
        String studentSkills = formatStudentSkills(student);
        String availableSkills = formatAvailableSkills();

        AiRoadmapOverview overview = fetchRoadmapOverviewFromAi(student, targetRole, studentSkills, availableSkills);
        List<AiRoadmapStep> steps = fetchRoadmapStepsFromAi(
                student, targetRole, studentSkills, availableSkills, overview);

        deleteExistingSteps(roadmap);

        roadmap.setTitle(overview.title());
        roadmap.setTargetRole(targetRole);
        roadmap.setSkillGapSummary(overview.skillGapSummary());
        roadmap.setProgressPercentage(0);
        roadmap = roadmapRepository.save(roadmap);

        saveGeneratedSteps(roadmap, steps);

        return getRoadmapById(roadmap.getId());
    }

    public List<RoadmapDTOOUT> getAllRoadmaps() {
        List<RoadmapDTOOUT> roadmapDTO = new ArrayList<>();
        for (Roadmap roadmap : roadmapRepository.findAll()) {
            roadmapDTO.add(convertToDTO(roadmap));
        }
        return roadmapDTO;
    }

    public RoadmapDTOOUT getRoadmapById(Integer id) {
        Roadmap roadmap = roadmapRepository.findRoadmapById(id);
        if (roadmap == null) {
            throw new ApiException("Roadmap not found");
        }
        return convertToDTO(roadmap);
    }

    @Transactional(readOnly = true)
    public List<RoadmapDTOOUT> getRoadmapsByStudentId(Integer studentId) {
        findStudent(studentId);
        List<RoadmapDTOOUT> roadmaps = new ArrayList<>();
        for (Roadmap roadmap : roadmapRepository.findRoadmapsByStudentId(studentId)) {
            roadmaps.add(convertToDTO(roadmap));
        }
        return roadmaps;
    }

    public void deleteRoadmap(Integer id) {
        Roadmap roadmap = roadmapRepository.findRoadmapById(id);
        if (roadmap == null) {
            throw new ApiException("Roadmap not found");
        }
        roadmapRepository.delete(roadmap);
    }

    private Student findStudent(Integer studentId) {
        Student student = studentRepository.findStudentById(studentId);
        if (student == null) {
            throw new ApiException("Student with id " + studentId + " not found");
        }
        return student;
    }

    private AiRoadmapOverview fetchRoadmapOverviewFromAi(
            Student student,
            String targetRole,
            String studentSkills,
            String availableSkills) {
        String prompt = """
                You are a learning path designer for tech students.
                Analyze the gap between the student's current skills and their target role.
                
                Respond with JSON only using this exact shape:
                {
                  "title": "short roadmap title",
                  "skillGapSummary": "paragraph describing main skill gaps"
                }
                
                Target role: %s
                Student major: %s
                Years of experience: %s
                Student skills: %s
                Available skills in system: %s
                
                --- CV ---
                %s
                
                --- GitHub ---
                %s
                """.formatted(
                targetRole,
                student.getMajor(),
                student.getYearsExperience(),
                studentSkills,
                availableSkills,
                studentProfilePromptHelper.formatCvForPrompt(student),
                studentProfilePromptHelper.formatGithubForPrompt(student)
        );

        String json = aiService.ask(prompt);
        String title = extractJsonString(json, "title");
        String skillGapSummary = extractJsonString(json, "skillGapSummary");

        if (title == null || skillGapSummary == null) {
            throw new AiException("AI response is missing roadmap title or skillGapSummary.");
        }

        return new AiRoadmapOverview(title, skillGapSummary);
    }

    private List<AiRoadmapStep> fetchRoadmapStepsFromAi(
            Student student,
            String targetRole,
            String studentSkills,
            String availableSkills,
            AiRoadmapOverview overview) {
        String prompt = """
                You are a learning path designer. Create practical roadmap steps for the student.
                
                Respond with JSON only using this exact shape:
                {
                  "steps": [
                    {
                      "title": "step title",
                      "description": "what the student should do",
                      "orderNumber": 1,
                      "skillName": "ExactSkillNameFromList"
                    }
                  ]
                }
                
                Include 4 to 8 steps. orderNumber starts at 1.
                skillName must use exact names from the available skills list only. Do NOT invent skill names.
                
                Roadmap title: %s
                Skill gap summary: %s
                Target role: %s
                Student skills: %s
                Available skills: %s
                """.formatted(
                overview.title(),
                overview.skillGapSummary(),
                targetRole,
                studentSkills,
                availableSkills
        );

        String json = aiService.ask(prompt);
        List<AiRoadmapStep> steps = parseRoadmapSteps(json);
        if (steps.isEmpty()) {
            throw new AiException("AI did not return any roadmap steps.");
        }
        return steps;
    }

    private List<AiRoadmapStep> parseRoadmapSteps(String json) {
        List<AiRoadmapStep> steps = new ArrayList<>();
        Matcher matcher = STEP_OBJECT_PATTERN.matcher(json);
        while (matcher.find()) {
            steps.add(new AiRoadmapStep(
                    unescapeJsonString(matcher.group(1)),
                    unescapeJsonString(matcher.group(2)),
                    Integer.parseInt(matcher.group(3)),
                    unescapeJsonString(matcher.group(4))
            ));
        }
        return steps;
    }

    private void saveGeneratedSteps(Roadmap roadmap, List<AiRoadmapStep> steps) {
        Map<String, Skill> skillsInDatabase = skillRepository.findAll().stream()
                .collect(Collectors.toMap(
                        skill -> skill.getName().trim().toLowerCase(),
                        skill -> skill,
                        (existing, duplicate) -> existing
                ));

        for (AiRoadmapStep step : steps) {
            Skill skill = skillsInDatabase.get(step.skillName().trim().toLowerCase());
            if (skill == null) {
                continue;
            }
            roadmapStepService.saveGeneratedStep(
                    roadmap,
                    step.title(),
                    step.description(),
                    step.orderNumber(),
                    skill
            );
        }
    }

    private void deleteExistingSteps(Roadmap roadmap) {
        if (roadmap.getRoadmapSteps() == null || roadmap.getRoadmapSteps().isEmpty()) {
            return;
        }
        List<RoadmapStep> existingSteps = new ArrayList<>(roadmap.getRoadmapSteps());
        roadmapStepRepository.deleteAll(existingSteps);
        roadmap.getRoadmapSteps().clear();
    }

    private String formatStudentSkills(Student student) {
        if (student.getSkills() == null || student.getSkills().isEmpty()) {
            return "(none listed)";
        }
        return student.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.joining(", "));
    }

    private String formatAvailableSkills() {
        List<Skill> skills = skillRepository.findAll();
        if (skills.isEmpty()) {
            return "(none in system)";
        }
        return skills.stream()
                .map(Skill::getName)
                .collect(Collectors.joining(", "));
    }

    private String extractJsonString(String json, String fieldName) {
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return null;
        }
        return unescapeJsonString(matcher.group(1));
    }

    private String unescapeJsonString(String value) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                char next = value.charAt(++i);
                switch (next) {
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    case '"' -> result.append('"');
                    case '\\' -> result.append('\\');
                    default -> result.append(next);
                }
            } else {
                result.append(c);
            }
        }
        return result.toString().trim();
    }

    public RoadmapDTOOUT convertToDTO(Roadmap roadmap) {
        List<RoadmapStepDTOOUT> steps = new ArrayList<>();
        if (roadmap.getRoadmapSteps() != null) {
            for (RoadmapStep step : roadmap.getRoadmapSteps()) {
                steps.add(roadmapStepService.convertToDTO(step));
            }
        }
        return new RoadmapDTOOUT(
                roadmap.getId(),
                roadmap.getTitle(),
                roadmap.getTargetRole(),
                roadmap.getSkillGapSummary(),
                null,
                roadmap.getProgressPercentage(),
                steps,
                roadmap.getCreatedAt()
        );
    }

    private record AiRoadmapOverview(String title, String skillGapSummary) {
    }

    private record AiRoadmapStep(String title, String description, int orderNumber, String skillName) {
    }
}
