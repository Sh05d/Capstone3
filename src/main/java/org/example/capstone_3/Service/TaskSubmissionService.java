package org.example.capstone_3.Service;

import lombok.RequiredArgsConstructor;
import org.example.capstone_3.AI.AiJsonParser;
import org.example.capstone_3.AI.AiService;
import org.example.capstone_3.Api.ApiException;
import org.example.capstone_3.DTO.IN.TaskSubmissionDTOIN;
import org.example.capstone_3.DTO.OUT.TaskSubmissionDTOOUT;
import org.example.capstone_3.Model.Student;
import org.example.capstone_3.Model.Task;
import org.example.capstone_3.Model.TaskSubmission;
import org.example.capstone_3.Repository.StudentRepository;
import org.example.capstone_3.Repository.TaskRepository;
import org.example.capstone_3.Repository.TaskSubmissionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskSubmissionService {

    private final TaskSubmissionRepository taskSubmissionRepository;
    private final TaskRepository taskRepository;
    private final StudentRepository studentRepository;
    private final AiService aiService;


    public List<TaskSubmissionDTOOUT> getAllTaskSubmissions() {

        List<TaskSubmissionDTOOUT> submissionDTO = new ArrayList<>();

        for (TaskSubmission submission : taskSubmissionRepository.findAll()) {
            submissionDTO.add(convertToDTO(submission));
        }

        return submissionDTO;
    }

    public TaskSubmissionDTOOUT getTaskSubmissionById(Integer id) {

        TaskSubmission submission = taskSubmissionRepository.findTaskSubmissionById(id);

        if (submission == null) {
            throw new ApiException("Task Submission not found");
        }

        return convertToDTO(submission);
    }

    public void addTaskSubmission(Integer taskId, Integer studentId, TaskSubmissionDTOIN dto) {
        Task task = findTask(taskId);
        Student student = findStudent(studentId);

        boolean alreadyEarned = taskSubmissionRepository.findPassingSubmission(taskId, studentId, 75) != null;

        String[] result = fetchEvaluationFromAi(dto.getAnswerText(), task);
        int score = Integer.parseInt(result[0]);

        TaskSubmission submission = new TaskSubmission();
        submission.setAnswerText(dto.getAnswerText());
        submission.setScore(score);
        submission.setAiFeedback(result[1]);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setTask(task);
        submission.setStudent(student);

        if (!alreadyEarned && score >= 75 && LocalDateTime.now().isBefore(task.getDeadline())) {
            student.setXp(student.getXp() + task.getPoints());
            studentRepository.save(student);
        }

        taskSubmissionRepository.save(submission);
    }

    public void updateTaskSubmission(Integer id, TaskSubmissionDTOIN dto) {
        TaskSubmission submission = findTaskSubmission(id);

        Task task = submission.getTask();
        Student student = submission.getStudent();

        boolean alreadyEarned = taskSubmissionRepository
                .findPassingSubmission(task.getId(), student.getId(), 75) != null;

        String[] result = fetchEvaluationFromAi(dto.getAnswerText(), task);
        int newScore = Integer.parseInt(result[0]);

        submission.setAnswerText(dto.getAnswerText());
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setScore(newScore);
        submission.setAiFeedback(result[1]);

        if (!alreadyEarned && newScore >= 75 && LocalDateTime.now().isBefore(task.getDeadline())) {
            student.setXp(student.getXp() + task.getPoints());
        }

        studentRepository.save(student);
        taskSubmissionRepository.save(submission);
    }

    public void deleteTaskSubmission(Integer id) {
        TaskSubmission submission = findTaskSubmission(id);
        taskSubmissionRepository.delete(submission);
    }

    public List<TaskSubmissionDTOOUT> studentTaskSubmissions(Integer studentId, Integer taskId) {
        findStudent(studentId);
        findTask(taskId);

        List<TaskSubmissionDTOOUT> submissions = new ArrayList<>();
        for (TaskSubmission submission : taskSubmissionRepository.findByTaskIdAndStudentId(taskId, studentId)) {
            submissions.add(convertToDTO(submission));
        }
        return submissions;
    }

    private Task findTask(Integer taskId) {
        Task task = taskRepository.findTaskById(taskId);
        if (task == null) {
            throw new ApiException("Task not found");
        }
        return task;
    }

    private Student findStudent(Integer studentId) {
        Student student = studentRepository.findStudentById(studentId);
        if (student == null) {
            throw new ApiException("Student not found");
        }
        return student;
    }

    private TaskSubmission findTaskSubmission(Integer id) {
        TaskSubmission submission = taskSubmissionRepository.findTaskSubmissionById(id);
        if (submission == null) {
            throw new ApiException("Task Submission not found");
        }
        return submission;
    }

    public TaskSubmissionDTOOUT convertToDTO(TaskSubmission submission) {

        String taskTitle = null;

        if (submission.getTask() != null) {
            taskTitle = submission.getTask().getTitle();
        }

        return new TaskSubmissionDTOOUT(
                submission.getId(),
                taskTitle,
                submission.getAnswerText(),
                submission.getAiFeedback(),
                submission.getScore(),
                submission.getSubmittedAt()
        );
    }

    //AI

    private String[] fetchEvaluationFromAi(String answerText, Task task) {
        String prompt = buildEvaluationPrompt(answerText, task);
        String json = aiService.ask(prompt);
        return parseEvaluationJson(json);
    }

    private String buildEvaluationPrompt(String answerText, Task task) {
        return """
        You are a strict professional task evaluator for a collaborative learning platform.
        Your ONLY job is to evaluate a student's submitted answer for the task below.

        TASK CONTEXT:
        - Title: "%s"
        - Description: "%s"
        - Difficulty: "%s"

        EVALUATION RULES:
        - This platform accepts plain-text answers only — do NOT expect code, syntax, file uploads, links, repos, or screenshots
        - Evaluate how well the student's written explanation addresses the task description
        - Score based on conceptual understanding, clarity, completeness, and relevance — not working code or implementation
        - Do NOT deduct points for missing code, missing syntax, or lack of executable examples
        - Do NOT ask the student to provide code in feedback — they can only submit text explanations
        - Provide constructive, specific feedback explaining the score
        - Feedback must mention what was done well and what was missing conceptually
        - Do not penalize brevity when all required points are covered in prose
        - For EASY tasks, a concise correct explanation should score at least 75

        STUDENT ANSWER:
        %s

        Respond with JSON only:
        {
          "score": <integer>,
          "feedback": "..."
        }

        FIELD RULES:
        - score:
          * integer between 0 and 100
          * EASY task: full marks for a correct basic explanation; concise prose covering key points should score at least 75
          * MEDIUM task: full marks for a complete, well-reasoned text explanation
          * HARD task: full marks for solid conceptual coverage — do NOT require code or perfection;
            reward good understanding and effort even if not expert-level
          * For HARD tasks, a good-faith explanation with correct core concepts should score at least 60
          * 0 = completely wrong or irrelevant
          * 100 = perfect, complete, professional text explanation

        - feedback:
          * max 80 words
          * must be specific to the student's answer
          * must explain why the score was given
          * must mention what was good and what needs improvement
          * must be constructive and professional

        OUTPUT MUST BE STRICT JSON ONLY
        """.formatted(
                task.getTitle(),
                task.getDescription(),
                task.getDifficulty(),
                answerText
        );
    }

    private String[] parseEvaluationJson(String json) {
        var node = AiJsonParser.parseObject(json);
        int score = AiJsonParser.requireInt(node, "score", 0, 100);
        String feedback = AiJsonParser.requireText(node, "feedback");
        return new String[]{String.valueOf(score), feedback};
    }
}
