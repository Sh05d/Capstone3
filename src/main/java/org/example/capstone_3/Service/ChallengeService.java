package org.example.capstone_3.Service;

import lombok.RequiredArgsConstructor;
import org.example.capstone_3.AI.AiException;
import org.example.capstone_3.AI.AiService;
import org.example.capstone_3.Api.ApiException;
import org.example.capstone_3.DTO.IN.ChallengeDTOIN;
import org.example.capstone_3.DTO.OUT.ChallengeDTOOUT;
import org.example.capstone_3.Model.Admin;
import org.example.capstone_3.Model.Challenge;
import org.example.capstone_3.Model.Skill;
import org.example.capstone_3.Repository.AdminRepository;
import org.example.capstone_3.Repository.ChallengeRepository;
import org.example.capstone_3.Repository.SkillRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private static final Pattern TITLE_PATTERN =
            Pattern.compile("\"title\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern QUESTION_PATTERN =
            Pattern.compile("\"question\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern CORRECT_ANSWER_PATTERN =
            Pattern.compile("\"correctAnswer\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern POINTS_PATTERN =
            Pattern.compile("\"points\"\\s*:\\s*(\\d+)");
    private static final Pattern DIFFICULTY_PATTERN =
            Pattern.compile("\"difficulty\"\\s*:\\s*\"(EASY|MEDIUM|HARD)\"");

    private final ChallengeRepository challengeRepository;
    private final SkillRepository skillRepository;
    private final AiService aiService;

    public void create(ChallengeDTOIN dto) {
        Skill skill = findSkillByName(dto.getSkillName());

        Challenge challenge = fetchChallengeFromAi(dto.getSkillName());

        challenge.setSkill(skill);
        applyDto(challenge, skill.getId());

        challengeRepository.save(challenge);
    }

    public ChallengeDTOOUT getById(Integer id) {
        Challenge challenge = challengeRepository.findChallengeById(id);
        if (challenge == null) {
            throw new ApiException("Challenge with id " + id + " not found");
        }
        return toDtoOut(challenge);
    }

    public List<ChallengeDTOOUT> getAll() {
        return challengeRepository.findAll().stream().map(this::toDtoOut).toList();
    }

    public void update(Integer id, ChallengeDTOIN dto) {
        Challenge challenge = challengeRepository.findChallengeById(id);
        if (challenge == null) {
            throw new ApiException("Challenge with id " + id + " not found");
        }
        Skill skill = findSkillByName(dto.getSkillName());
        Challenge updatedChallenge = fetchChallengeFromAi(dto.getSkillName());

        updatedChallenge.setId(challenge.getId());
        updatedChallenge.setSkill(skill);

        challengeRepository.save(updatedChallenge);
    }

    public void delete(Integer id) {
        Challenge challenge = challengeRepository.findChallengeById(id);
        if (challenge == null) {
            throw new ApiException("Challenge with id " + id + " not found");
        }
        challengeRepository.deleteById(id);
    }

    public List<ChallengeDTOOUT> challengesBySkill(Integer skillId){
        List<ChallengeDTOOUT> challengesDTOOUTS = new ArrayList<>();
        for(Challenge challenge: challengeRepository.availableChallengesBySkillId(skillId)){
            challengesDTOOUTS.add(toDtoOut(challenge));
        }
        return challengesDTOOUTS;
    }

    public List<ChallengeDTOOUT> challengesBySkillAndDifficulty(Integer skillId, String difficulty){
        List<ChallengeDTOOUT> challengesDTOOUTS = new ArrayList<>();
        for(Challenge challenge: challengeRepository.availableChallengesBySkillIdAndDifficulty(skillId,difficulty)){
            challengesDTOOUTS.add(toDtoOut(challenge));
        }
        return challengesDTOOUTS;
    }

    private void applyDto(Challenge challenge, Integer skillId) {
        challenge.setSkill(findSkill(skillId));
    }

    private Skill findSkill(Integer skillId) {
        if (skillId == null) {
            return null;
        }
        Skill skill = skillRepository.findSkillById(skillId);
        if (skill == null) {
            throw new ApiException("Skill with id " + skillId + " not found");
        }
        return skill;
    }

    private Skill findSkillByName(String skillName){
        if (skillName == null) {
            return null;
        }
        Skill skill = skillRepository.findSkillByName(skillName);
        if (skill == null) {
            throw new ApiException("Skill with name " + skillName + " not found");
        }
        return skill;
    }

    private ChallengeDTOOUT toDtoOut(Challenge challenge) {
        return new ChallengeDTOOUT(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getQuestion(),
                challenge.getPoints(),
                challenge.getDifficulty()
        );
    }

    // AI service

    private String classifySkillWithAi(String skillName) {
        String prompt = """
            Is "%s" a programming/coding skill (like a language, framework, or library)?
            Respond with JSON only:
            {"isCoding": true}
            """.formatted(skillName);

        String json = aiService.ask(prompt);
        return json.contains("true") ? "CODING" : "WORKPLACE";
    }

    private Challenge fetchChallengeFromAi(String skillName) {
        String skillType = classifySkillWithAi(skillName);
        List<Challenge> existingChallenges = challengeRepository.findChallengesBySkillName(skillName);
        String prompt = buildChallengePrompt(skillName, skillType, existingChallenges);
        String json = aiService.ask(prompt);
        Challenge challenge = parseChallengeJson(json);
        challenge.setPoints(mapPoints(challenge.getDifficulty()));
        return challenge;
    }

    private String buildChallengePrompt(String skillName, String skillType, List<Challenge> existingChallenges) {

        StringBuilder existingQuestions = new StringBuilder();
        if (!existingChallenges.isEmpty()) {
            existingQuestions.append("ALREADY GENERATED QUESTIONS — DO NOT REUSE OR PARAPHRASE:\n");
            for (int i = 0; i < existingChallenges.size(); i++) {
                existingQuestions.append(i + 1)
                        .append(". ")
                        .append(existingChallenges.get(i).getQuestion())
                        .append("\n");
            }
        }

        String difficulty = randomDifficulty();

        String questionGuidance;
        if (skillType.equals("CODING")) {
            questionGuidance = """
                QUESTION TYPE: CODING CHALLENGE
                - The question must require writing, fixing, or analyzing actual code
                - Include a realistic code snippet or coding scenario
                - The correct answer must contain a code solution or explanation of code behavior
                - Focus on real bugs, design decisions, or implementation problems
                """;
        } else {
            questionGuidance = """
                QUESTION TYPE: WORKPLACE SCENARIO
                - The question must reflect a real workplace or professional situation
                - Focus on decision-making, best practices, or problem-solving in a work context
                - The correct answer must be the most professional and effective response
                - No code required — focus on soft skills, processes, or business decisions
                """;
        }

        return """
            You are a strict professional challenge generator for a career development platform.

            Your ONLY job is to generate a single challenge exclusively about the skill: "%s".

            CRITICAL RULES:
            - The challenge must be 100%% about "%s"
            - Do NOT generate generic or tutorial-style questions
            - The question must be a real-world scenario
            - The challenge must reflect how "%s" is used in real work or business context
            - The question must be simple, clear, and easy to understand
            - The student must know exactly what is expected from them — no ambiguity in what to do
            - The answer must be precise, unambiguous, and objectively correct
            - The challenge must NOT be reusable across other skills
            - NEVER repeat or paraphrase previous questions

            %s

            %s

            UNIQUENESS RULES:
            - Generate a completely new challenge
            - Do NOT reuse any previous scenario, business context, problem type, or solution pattern
            - Similar meaning counts as duplication even if wording is different
            - If your generated question is similar to any previous question, discard it and generate a different one
            - Prefer unexplored use cases of the skill
            - The generated challenge must be clearly distinguishable from all previous challenges

            Today's date is: %s
            
            Respond with JSON only:
                {
                  "title": "write actual title here",
                  "question": "write actual question here",
                  "correctAnswer": "write actual answer here",
                  "difficulty": "%s"
                }

            FIELD RULES:

            - title:
              * max 10 words
              * must include "%s"

            - question:
              * max 60 words
              * must require direct "%s" expertise
              * must be a real-world scenario, not a tutorial
              * must test skill-specific knowledge
              * must clearly reflect the selected difficulty level

            - correctAnswer:
              * max 40 words
              * must be precise and deterministic
              * must directly solve the scenario
              * avoid generic answers
              * for technical skills, provide the technically correct solution or recommendation
              * for soft skills, provide the most professional and effective response

            - difficulty:
              * MUST be exactly: %s
              * Do NOT change or override it
              * EASY: beginner level, basic concepts, only fundamental required
              * MEDIUM: intermediate level, practical application, moderate experience required
              * HARD: expert level, advanced scenarios, significant experience required
              * The generated question MUST strictly match the selected difficulty level

            VALIDATION RULES:
            - Must NOT be generic or reusable
            - Must require skill "%s"
            - Scenario complexity must match difficulty level
            - OUTPUT MUST BE STRICT JSON ONLY
            """.formatted(
                skillName,
                skillName,
                skillName,
                existingQuestions.toString(),
                questionGuidance,
                LocalDateTime.now(),
                difficulty,
                skillName,
                skillName,
                difficulty,
                skillName
        );
    }

    private Challenge parseChallengeJson(String json) {

        Matcher titleMatcher = TITLE_PATTERN.matcher(json);
        Matcher questionMatcher = QUESTION_PATTERN.matcher(json);
        Matcher correctAnswerMatcher = CORRECT_ANSWER_PATTERN.matcher(json);
        Matcher difficultyMatcher = DIFFICULTY_PATTERN.matcher(json);

        if (!titleMatcher.find()) throw new AiException("AI response did not contain title.");
        if (!questionMatcher.find()) throw new AiException("AI response did not contain question.");
        if (!correctAnswerMatcher.find()) throw new AiException("AI response did not contain correctAnswer.");
        if (!difficultyMatcher.find()) throw new AiException("AI response did not contain difficulty.");

        String difficulty = difficultyMatcher.group(1);

        Challenge challenge = new Challenge();
        challenge.setTitle(titleMatcher.group(1));
        challenge.setQuestion(questionMatcher.group(1));
        challenge.setCorrectAnswer(correctAnswerMatcher.group(1));
        challenge.setDifficulty(difficulty);
        return challenge;
    }

    private int mapPoints(String difficulty) {
        return switch (difficulty) {
            case "EASY" -> 10;
            case "MEDIUM" -> 20;
            case "HARD" -> 30;
            default -> throw new AiException("Invalid difficulty: " + difficulty);
        };
    }

    private String randomDifficulty() {
        String[] levels = {"EASY", "MEDIUM", "HARD"};
        return levels[new Random().nextInt(levels.length)];
    }
}
