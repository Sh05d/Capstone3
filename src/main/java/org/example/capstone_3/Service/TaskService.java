package org.example.capstone_3.Service;

import lombok.RequiredArgsConstructor;
import org.example.capstone_3.AI.AiException;
import org.example.capstone_3.AI.AiService;
import org.example.capstone_3.Api.ApiException;
import org.example.capstone_3.DTO.IN.TaskDTOIN;
import org.example.capstone_3.DTO.OUT.TaskDTOOUT;
import org.example.capstone_3.Model.LearningGroup;
import org.example.capstone_3.Model.Student;
import org.example.capstone_3.Model.Task;
import org.example.capstone_3.Repository.LearningGroupRepository;
import org.example.capstone_3.Repository.StudentRepository;
import org.example.capstone_3.Repository.TaskRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class TaskService {
    // Add these patterns at the top of the class
    private static final Pattern TITLE_PATTERN =
            Pattern.compile("\"title\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern DESCRIPTION_PATTERN =
            Pattern.compile("\"description\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern POINTS_PATTERN =
            Pattern.compile("\"points\"\\s*:\\s*(\\d+)");
    private static final Pattern DIFFICULTY_PATTERN =
            Pattern.compile("\"difficulty\"\\s*:\\s*\"(EASY|MEDIUM|HARD)\"");

    private final TaskRepository taskRepository;
    private final LearningGroupRepository learningGroupRepository;
    private final StudentRepository studentRepository;
    private final EmailService emailService;
    private final AiService aiService;

    public List<TaskDTOOUT> getAllTasks() {

        List<TaskDTOOUT> taskDTO = new ArrayList<>();

        for (Task task : taskRepository.findAll()) {
            taskDTO.add(convertToDTO(task));
        }

        return taskDTO;
    }

    public TaskDTOOUT getTaskById(Integer id) {

        Task task = taskRepository.findTaskById(id);

        if (task == null) {
            throw new ApiException("Task not found");
        }

        return convertToDTO(task);
    }

    public void addTask(Integer learningGroupId) {
        LearningGroup learningGroup = learningGroupRepository.findLearningGroupById(learningGroupId);
        if (learningGroup == null) {
            throw new ApiException("Learning group not found");
        }

        Task task = fetchTaskFromAi(learningGroup);
        task.setCreatedAt(LocalDateTime.now());
        task.setLearningGroup(learningGroup);
        taskRepository.save(task);

        List<Student> members = studentRepository.findStudentsByGroupId(learningGroupId);
        for(Student student: members){
            emailService.sendTaskPublishedToStudent(student,task);
        }
    }

    @Scheduled(cron = "0 0 10 * * SUN")
    public void aiPublish(){
        List<LearningGroup> learningGroups = learningGroupRepository.findAll();
        for(LearningGroup group: learningGroups){
            addTask(group.getId());
        }
    }

    public void updateTask(Integer id, TaskDTOIN dto) {

        Task task = taskRepository.findTaskById(id);

        if (task == null) {
            throw new ApiException("Task not found");
        }

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDeadline(dto.getDeadline());

        taskRepository.save(task);
    }

    public void deleteTask(Integer id) {

        Task task = taskRepository.findTaskById(id);

        if (task == null) {
            throw new ApiException("Task not found");
        }

        taskRepository.delete(task);
    }

    public List<TaskDTOOUT> unsubmittedTasksForStudent(Integer learningGroupId, Integer studentId) {
        LearningGroup learningGroup = findLearningGroup(learningGroupId);
        Student student = findStudent(studentId);

        if (!student.getLearningGroups().contains(learningGroup)) {
            throw new ApiException("Student is not a member of this group");
        }

        List<TaskDTOOUT> unsubmittedTasks = new ArrayList<>();
        for (Task task : taskRepository.findUnsubmittedTasksByGroupAndStudent(learningGroupId, studentId)) {
            unsubmittedTasks.add(convertToDTO(task));
        }
        return unsubmittedTasks;
    }

    public List<TaskDTOOUT> groupOldTasks(Integer learningGroupId){
        findLearningGroup(learningGroupId);

        List<TaskDTOOUT> oldTasks = new ArrayList<>();
        for(Task task: taskRepository.groupOldTasks(learningGroupId)){
            oldTasks.add(convertToDTO(task));
        }
        return oldTasks;
    }

    public List<TaskDTOOUT> groupAvailableTasks(Integer learningGroupId) {
        findLearningGroup(learningGroupId);

        List<TaskDTOOUT> availableTasks = new ArrayList<>();
        for (Task task : taskRepository.groupAvailableTasks(learningGroupId)) {
            availableTasks.add(convertToDTO(task));
        }
        return availableTasks;
    }

    private Student findStudent(Integer student_id) {
        Student student = studentRepository.findStudentById(student_id);
        if (student == null) {
            throw new ApiException("Student not found");
        }
        return student;
    }

    private LearningGroup findLearningGroup(Integer group_id) {
        LearningGroup learningGroup = learningGroupRepository.findLearningGroupById(group_id);
        if (learningGroup == null) {
            throw new ApiException("Learning group not found");
        }
        return learningGroup;
    }

    public TaskDTOOUT convertToDTO(Task task) {

        return new TaskDTOOUT(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDifficulty(),
                task.getDeadline()
        );
    }

    // AI

    private Task fetchTaskFromAi(LearningGroup learningGroup) {
        List<Task> existingTasks = taskRepository.findTasksByLearningGroupId(learningGroup.getId());
        String prompt = buildTaskPrompt(learningGroup, existingTasks);
        String json = aiService.ask(prompt);
        Task task = parseTaskJson(json);
        task.setPoints(mapPoints(task.getDifficulty()));
        return task;
    }

    private String buildTaskPrompt(LearningGroup learningGroup, List<Task> existingTasks) {
        String difficulty = randomDifficulty();
        int deadlineDays = mapDeadlineDays(difficulty);

        StringBuilder existingTasksList = new StringBuilder();
        if (!existingTasks.isEmpty()) {
            existingTasksList.append("ALREADY GENERATED TASKS — DO NOT REUSE OR PARAPHRASE:\n");
            for (int i = 0; i < existingTasks.size(); i++) {
                existingTasksList.append(i + 1)
                        .append(". ").append(existingTasks.get(i).getTitle())
                        .append(": ").append(existingTasks.get(i).getDescription())
                        .append("\n");
            }
        }

        return """
    You are a strict professional task generator for a collaborative learning platform.
    Your ONLY job is to generate a single realistic, time-appropriate task for the learning group below.

    LEARNING GROUP CONTEXT:
    - Focus Area: "%s"
    - Description: "%s"

    CRITICAL RULES:
    - The task must be 100%% relevant to the focus area "%s"
    - The task must align with the group description and purpose
    - Do NOT generate generic or tutorial-style tasks
    - The task must be a real-world, practical assignment
    - The task scope and complexity MUST be realistically achievable within the deadline
    - NEVER repeat or paraphrase previous tasks

    %s

    UNIQUENESS RULES:
    - Generate a completely new task
    - Do NOT reuse any previous scenario or problem type
    - Similar meaning counts as duplication even if wording is different
    - Prefer unexplored aspects of the focus area

    SUBMISSION RULES (MANDATORY):
    - The task deliverable must be a plain TEXT answer only — written explanation in the submission text box
    - Students type their answer directly; there is no file upload, link, repo, screenshot, or image submission
    - Do NOT ask students to: implement, build, deploy, code, paste code snippets, share GitHub, upload files, or attach screenshots
    - Do NOT generate tasks whose title starts with or implies "Implement", "Build", "Code", or "Create a project"
    - GOOD task types: explain a concept, compare two approaches, outline steps in prose, describe how something works, analyze a scenario, list pros/cons, summarize best practices
    - GOOD deliverable examples: "Explain how JWT authentication works in a REST API", "Describe the steps to secure an endpoint using Spring Security", "Compare session-based vs token-based auth"
    - BAD deliverable examples: "Implement JWT authentication", "Write the code for...", "Submit your GitHub repo", "Paste your code snippet", "Upload a screenshot", "Build a REST API"

    Today's date is: %s
    Deadline: %d days from today

    Respond with JSON only:
    {
      "title": "...",
      "description": "...",
      "difficulty": "%s"
    }

    FIELD RULES:

    - title:
      * max 10 words
      * must reflect the focus area "%s"
      * must describe a concept to explain — NOT an implementation project (avoid "Implement", "Build", "Code")
      * must give a clear idea of what the student will write about in text

    - description:
      * max 80 words
      * must be a concrete, actionable assignment answerable entirely in plain text
      * must ask the student to EXPLAIN, DESCRIBE, OUTLINE, COMPARE, or ANALYZE — never to implement or code
      * must clearly state what the student needs to write in their text submission (no code, files, links, or screenshots)
      * must reflect the difficulty level and time available
      * avoid vague instructions like "research and discuss" — be specific about what concepts to cover
      * do NOT mention code snippets, syntax, file uploads, links, repos, screenshots, or external submissions
      * may explore a deeper or related aspect of previous tasks without repeating them

    - difficulty: must be exactly: %s
      * EASY:
        - Simple, well-defined task
        - Requires basic knowledge of the focus area
        - Minimal coordination needed
        - Example scope: read, summarize, or implement one small thing
      * MEDIUM:
        - Requires applying knowledge to a realistic problem
        - Involves collaboration and decision-making
        - Example scope: design, build, or analyze something with multiple steps
      * HARD:
        - Challenging but achievable task
        - Requires solid understanding of the focus area
        - Example scope: implement, design, or solve a multi-step real-world problem

    REALISM VALIDATION:
    - Ask yourself: can a group realistically complete this task in %d days?
    - If NO → reduce scope

    OUTPUT MUST BE STRICT JSON ONLY
    """.formatted(
                learningGroup.getFocusArea(),
                learningGroup.getDescription(),
                learningGroup.getFocusArea(),
                existingTasksList.toString(),
                LocalDateTime.now(),
                deadlineDays,
                difficulty,
                learningGroup.getFocusArea(),
                difficulty,
                deadlineDays
        );
    }

    private Task parseTaskJson(String json) {
        Matcher titleMatcher = TITLE_PATTERN.matcher(json);
        Matcher descriptionMatcher = DESCRIPTION_PATTERN.matcher(json);
        Matcher difficultyMatcher = DIFFICULTY_PATTERN.matcher(json);

        if (!titleMatcher.find()) throw new AiException("AI response did not contain title.");
        if (!descriptionMatcher.find()) throw new AiException("AI response did not contain description.");
        if (!difficultyMatcher.find()) throw new AiException("AI response did not contain difficulty.");

        String difficulty = difficultyMatcher.group(1);

        Task task = new Task();
        task.setTitle(titleMatcher.group(1));
        task.setDescription(descriptionMatcher.group(1));
        task.setDifficulty(difficulty);
        task.setDeadline(LocalDateTime.now().plusDays(mapDeadlineDays(difficulty)));
        return task;
    }

    private int mapPoints(String difficulty) {
        return switch (difficulty) {
            case "EASY" -> 10;
            case "MEDIUM" -> 20;
            case "HARD" -> 30;
            default -> throw new AiException("Invalid difficulty: " + difficulty);
        };
    }

    private int mapDeadlineDays(String difficulty) {
        return switch (difficulty) {
            case "EASY" -> 2;
            case "MEDIUM" -> 5;
            case "HARD" -> 7;
            default -> throw new AiException("Invalid difficulty: " + difficulty);
        };
    }

    private String randomDifficulty() {
        String[] levels = {"EASY", "MEDIUM", "HARD"};
        return levels[new Random().nextInt(levels.length)];
    }
}
