package org.example.capstone_3.Service;

import lombok.RequiredArgsConstructor;
import org.example.capstone_3.AI.AiJsonParser;
import org.example.capstone_3.AI.AiService;
import org.example.capstone_3.Api.ApiException;
import org.example.capstone_3.DTO.IN.ChallengeAttemptDTOIN;
import org.example.capstone_3.DTO.OUT.ChallengeAttemptDTOOUT;
import org.example.capstone_3.Model.Challenge;
import org.example.capstone_3.Model.ChallengeAttempt;
import org.example.capstone_3.Model.Student;
import org.example.capstone_3.Repository.ChallengeAttemptRepository;
import org.example.capstone_3.Repository.ChallengeRepository;
import org.example.capstone_3.Repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChallengeAttemptService {

    private final ChallengeAttemptRepository challengeAttemptRepository;
    private final ChallengeRepository challengeRepository;
    private final StudentRepository studentRepository;
    private final AiService aiService;

    public void create(Integer student_id, Integer challenge_id, ChallengeAttemptDTOIN dto) {
        Student student = findStudent(student_id);
        Challenge challenge = findChallenge(challenge_id);

        ChallengeAttempt correctAttempt = challengeAttemptRepository.correctAttemptForChallenge(student_id, challenge_id);
        if(correctAttempt != null){
            throw new ApiException("Already solved this challenge");
        }

        boolean isCorrect = fetchIsCorrectFromAi(dto.getSubmittedAnswer(), challenge.getCorrectAnswer());

        ChallengeAttempt challengeAttempt = new ChallengeAttempt();
        applyDto(challengeAttempt, dto);
        challengeAttempt.setStudent(student);
        challengeAttempt.setChallenge(challenge);
        challengeAttempt.setCorrect(isCorrect);
        challengeAttempt.setSubmittedAt(LocalDateTime.now());

        if (isCorrect) {
            student.setXp(student.getXp() + challenge.getPoints());
            studentRepository.save(student);
        }

        challengeAttemptRepository.save(challengeAttempt);
    }

    public ChallengeAttemptDTOOUT getById(Integer id) {
        ChallengeAttempt challengeAttempt = challengeAttemptRepository.findChallengeAttemptById(id);
        if (challengeAttempt == null) {
            throw new ApiException("Challenge attempt with id " + id + " not found");
        }
        return toDtoOut(challengeAttempt);
    }

    public List<ChallengeAttemptDTOOUT> getAll() {
        return challengeAttemptRepository.findAll().stream().map(this::toDtoOut).toList();
    }

    public void update(Integer id, ChallengeAttemptDTOIN dto) {
        ChallengeAttempt challengeAttempt = challengeAttemptRepository.findChallengeAttemptById(id);
        if (challengeAttempt == null) {
            throw new ApiException("Challenge attempt with id " + id + " not found");
        }

        Challenge challenge = challengeAttempt.getChallenge();
        Student student = challengeAttempt.getStudent();

        boolean isCorrect = fetchIsCorrectFromAi(dto.getSubmittedAnswer(), challenge.getCorrectAnswer());

        // reverse old points if previous attempt was correct
        if (challengeAttempt.getCorrect()) {
            student.setXp(student.getXp() - challenge.getPoints());
        }

        challengeAttempt.setSubmittedAnswer(dto.getSubmittedAnswer());
        challengeAttempt.setSubmittedAt(LocalDateTime.now());
        challengeAttempt.setCorrect(isCorrect);

        if (isCorrect) {
            student.setXp(student.getXp() + challenge.getPoints());
        }

        studentRepository.save(student);
        challengeAttemptRepository.save(challengeAttempt);
    }

    public void delete(Integer id) {
        ChallengeAttempt challengeAttempt = challengeAttemptRepository.findChallengeAttemptById(id);
        if (challengeAttempt == null) {
            throw new ApiException("Challenge attempt with id " + id + " not found");
        }
        challengeAttemptRepository.deleteById(id);
    }

    public List<ChallengeAttemptDTOOUT> studentAttemptsForChallenge(Integer studentId, Integer challengeId) {
        findStudent(studentId);
        findChallenge(challengeId);

        List<ChallengeAttemptDTOOUT> attempts = new ArrayList<>();
        for (ChallengeAttempt attempt : challengeAttemptRepository.studentAttemptsForChallenge(studentId, challengeId)) {
            attempts.add(toDtoOut(attempt));
        }
        return attempts;
    }

    private void applyDto(ChallengeAttempt challengeAttempt, ChallengeAttemptDTOIN dto) {
        challengeAttempt.setSubmittedAnswer(dto.getSubmittedAnswer());
    }

    private Student findStudent(Integer studentId) {
        if (studentId == null) {
            return null;
        }
        Student student = studentRepository.findStudentById(studentId);
        if (student == null) {
            throw new ApiException("Student with id " + studentId + " not found");
        }
        return student;
    }

    private Challenge findChallenge(Integer challengeId) {
        if (challengeId == null) {
            return null;
        }
        Challenge challenge = challengeRepository.findChallengeById(challengeId);
        if (challenge == null) {
            throw new ApiException("Challenge with id " + challengeId + " not found");
        }
        return challenge;
    }

    private ChallengeAttemptDTOOUT toDtoOut(ChallengeAttempt challengeAttempt) {
        return new ChallengeAttemptDTOOUT(
                challengeAttempt.getId(),
                challengeAttempt.getSubmittedAnswer(),
                challengeAttempt.getCorrect(),
                challengeAttempt.getSubmittedAt()
        );
    }

    // AI service

    private boolean fetchIsCorrectFromAi(String submittedAnswer, String correctAnswer) {
        String prompt = buildIsCorrectPrompt(submittedAnswer, correctAnswer);
        String json = aiService.ask(prompt);
        return parseIsCorrect(json);
    }

    private String buildIsCorrectPrompt(String submittedAnswer, String correctAnswer) {
        return """
        You are a strict but fair answer evaluator for a career development platform
        covering various domains including technical, business, soft skills, and more.

        Compare the student's answer to the correct answer based on CONCEPTS, not exact wording.
        Consider the answer CORRECT if the student demonstrates understanding of the core idea,
        even if they use different phrasing, additional detail, or a different order.
        If the student gives a valid example of the concept (e.g. O(n) for Big-O), mark correct
        even when the full formal definition is not stated.

        Consider the answer INCORRECT only if:
        - A key concept is missing
        - The student states something factually wrong
        - The answer is completely off-topic

        Correct Answer: %s
        Student Answer: %s

        Respond with JSON only, no explanation, no markdown:
        {"isCorrect": true}
        or
        {"isCorrect": false}
        """.formatted(correctAnswer, submittedAnswer);
    }

    private boolean parseIsCorrect(String json) {
        return AiJsonParser.requireBoolean(AiJsonParser.parseObject(json), "isCorrect");
    }
}
