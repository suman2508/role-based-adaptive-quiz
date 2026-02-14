package com.hackathon.quiz.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.quiz.ai.service.QuizGenerationAIService;
import com.hackathon.quiz.dto.request.QuizStartRequest;
import com.hackathon.quiz.dto.request.QuizSubmitRequest;
import com.hackathon.quiz.dto.response.QuizQuestionResponse;
import com.hackathon.quiz.dto.response.QuizSubmitResponse;
import com.hackathon.quiz.entity.QuizAttempt;
import com.hackathon.quiz.entity.QuizQuestion;
import com.hackathon.quiz.entity.Role;
import com.hackathon.quiz.entity.Skill;
import com.hackathon.quiz.entity.User;
import com.hackathon.quiz.enums.DifficultyLevel;
import com.hackathon.quiz.exception.ResourceNotFoundException;
import com.hackathon.quiz.exception.ValidationException;
import com.hackathon.quiz.mapper.QuizMapper;
import com.hackathon.quiz.repository.QuizAttemptRepository;
import com.hackathon.quiz.repository.QuizQuestionRepository;
import com.hackathon.quiz.repository.RoleRepository;
import com.hackathon.quiz.repository.SkillRepository;
import com.hackathon.quiz.repository.UserRepository;
import com.hackathon.quiz.service.QuizService;
import com.hackathon.quiz.util.DifficultyMapper;
import com.hackathon.quiz.util.ScoreCalculator;
import com.hackathon.quiz.validation.QuizSubmissionValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QuizServiceImpl implements QuizService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SkillRepository skillRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    private final QuizGenerationAIService quizGenerationAIService;
    private final QuizSubmissionValidator quizSubmissionValidator;
    private final QuizMapper quizMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<QuizQuestionResponse> start(QuizStartRequest request) {
        quizSubmissionValidator.validateStart(request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + request.getUserId()));

        String difficultyStr = request.getDifficulty() != null ? request.getDifficulty() : "medium";
        DifficultyLevel requested = DifficultyMapper.fromString(difficultyStr);
        String storageDifficulty = DifficultyMapper.toStorageString(requested);

        int limit = request.getLimit() != null && request.getLimit() > 0 ? request.getLimit() : 5;

        List<Skill> candidateSkills;

        if (request.getSkillId() != null) {
            Skill skill = skillRepository.findById(request.getSkillId())
                    .orElseThrow(() -> new ResourceNotFoundException("Skill not found: id=" + request.getSkillId()));
            candidateSkills = List.of(skill);
        } else {
            if (user.getTargetRole() == null || user.getTargetRole().isBlank()) {
                throw new ValidationException("MISSING_TARGET_ROLE", "User must set targetRole before starting quiz", null);
            }
            Role role = roleRepository.findByRoleName(user.getTargetRole())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found by name: " + user.getTargetRole()));

            candidateSkills = skillRepository.findByRoleOrderByPriorityScoreAsc(role).stream()
                    .sorted(Comparator.comparing(Skill::getPriorityScore, Comparator.nullsFirst(Integer::compareTo)).reversed())
                    .toList();
            if (candidateSkills.isEmpty()) {
                throw new ResourceNotFoundException("No skills found for role: " + role.getRoleName());
            }
        }

        List<QuizQuestionResponse> collected = new ArrayList<>();
        for (Skill skill : candidateSkills) {
            QuizQuestion q = findOrGenerateQuestion(skill, storageDifficulty);
            if (q != null) {
                collected.add(quizMapper.toResponse(q));
            }
            if (collected.size() >= limit) break;
        }
        return collected;
    }

    @Override
    public QuizSubmitResponse submit(QuizSubmitRequest request) {
        quizSubmissionValidator.validateSubmission(
                request.getUserId(), request.getQuestionId(), request.getSelectedAnswer(), null);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + request.getUserId()));
        QuizQuestion question = quizQuestionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: id=" + request.getQuestionId()));

        double score = ScoreCalculator.correctnessIgnoreCase(request.getSelectedAnswer(), question.getCorrectAnswer());

        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .question(question)
                .selectedAnswer(request.getSelectedAnswer())
                .score(score)
                .attemptTimestamp(OffsetDateTime.now())
                .build();
        quizAttemptRepository.save(attempt);

        return QuizSubmitResponse.builder()
                .questionId(question.getId())
                .correct(score >= 0.999) // binary in this phase
                .score(score)
                .correctAnswer(question.getCorrectAnswer())
                .explanation(question.getExplanation())
                .build();
    }

    @Override
    public QuizQuestionResponse nextAdaptive(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ValidationException("VALIDATION_ERROR", "userId is required", null);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + userId));

        if (user.getTargetRole() == null || user.getTargetRole().isBlank()) {
            throw new ValidationException("MISSING_TARGET_ROLE", "User must set targetRole for adaptive quiz", null);
        }

        Role role = roleRepository.findByRoleName(user.getTargetRole())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found by name: " + user.getTargetRole()));

        List<Skill> roleSkills = skillRepository.findByRoleOrderByPriorityScoreAsc(role);

        if (roleSkills.isEmpty()) {
            throw new ResourceNotFoundException("No skills found for role: " + role.getRoleName());
        }

        // Compute recent performance overall
        List<QuizAttempt> recent = quizAttemptRepository.findTop10ByUserOrderByAttemptTimestampDesc(user);
        double recentAvg = recent.isEmpty()
                ? 0.0
                : recent.stream().map(a -> a.getScore() != null ? a.getScore() : 0.0).mapToDouble(Double::doubleValue).average().orElse(0.0);

        // Choose difficulty based on recent performance
        DifficultyLevel nextDiff = DifficultyMapper.fromScore(recentAvg);
        String storageDifficulty = DifficultyMapper.toStorageString(nextDiff);

        // Choose skill: prioritize weakest skill (lowest accuracy), then by higher role priority
        Skill chosen = chooseWeakestSkill(user, roleSkills).orElse(roleSkills.get(0));

        QuizQuestion q = findOrGenerateQuestion(chosen, storageDifficulty);
        if (q == null) {
            throw new ResourceNotFoundException("Unable to generate or fetch a question at the moment");
        }
        return quizMapper.toResponse(q);
    }

    private Optional<Skill> chooseWeakestSkill(User user, List<Skill> skills) {
        // Build accuracy map per skill
        Map<Long, List<QuizAttempt>> bySkill = new HashMap<>();
        for (Skill s : skills) {
            List<QuizAttempt> attempts = quizAttemptRepository.findByUserAndQuestion_SkillOrderByAttemptTimestampDesc(user, s);
            bySkill.put(s.getId(), attempts);
        }

        // Compute accuracy (avg score) per skill; if no attempts, treat as lower accuracy to ensure coverage
        return skills.stream()
                .sorted((a, b) -> {
                    double accA = averageScore(bySkill.get(a.getId()));
                    double accB = averageScore(bySkill.get(b.getId()));
                    if (Double.compare(accA, accB) != 0) {
                        return Double.compare(accA, accB); // ascending => weakest first
                    }
                    // tie-breaker: higher priority score first (i.e., more important)
                    int pa = a.getPriorityScore() != null ? a.getPriorityScore() : 0;
                    int pb = b.getPriorityScore() != null ? b.getPriorityScore() : 0;
                    return Integer.compare(pb, pa);
                })
                .findFirst();
    }

    private double averageScore(List<QuizAttempt> attempts) {
        if (attempts == null || attempts.isEmpty()) return 0.0;
        return attempts.stream().map(a -> a.getScore() != null ? a.getScore() : 0.0)
                .mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private QuizQuestion findOrGenerateQuestion(Skill skill, String storageDifficulty) {
        // Try existing first (guard against legacy column type mismatches etc.)
        try {
            Optional<QuizQuestion> existing = quizQuestionRepository.findFirstBySkillAndDifficultyOrderByIdAsc(skill, storageDifficulty);
            if (existing.isPresent()) {
                return existing.get();
            }
        } catch (Exception ex) {
            log.error("Existing question lookup failed for skill={}, difficulty={}, cause={}",
                    skill != null ? skill.getSkillName() : "N/A", storageDifficulty, ex.toString(), ex);
        }
    
        // Fallback to AI generation
        try {
            String raw = quizGenerationAIService.generateQuestionsJson(skill.getSkillName(), storageDifficulty);
            return persistGeneratedQuestion(skill, storageDifficulty, raw);
        } catch (Exception e) {
            log.warn("AI generation failed for skill={}, difficulty={}", skill.getSkillName(), storageDifficulty, e);
            return null;
        }
    }

    private QuizQuestion persistGeneratedQuestion(Skill skill, String storageDifficulty, String rawJson) throws Exception {
        if (rawJson == null || rawJson.isBlank()) {
            throw new IllegalArgumentException("Empty AI question JSON");
        }
        JsonNode root = objectMapper.readTree(rawJson);

        String questionText = textOr(root, "questionText", "question");
        if (questionText == null || questionText.isBlank()) {
            throw new IllegalArgumentException("AI JSON missing questionText");
        }

        // Accept either "options" as array or "choices"
        JsonNode optsNode = root.hasNonNull("options") ? root.get("options") : root.get("choices");
        List<String> options = new ArrayList<>();
        if (optsNode != null && optsNode.isArray()) {
            for (JsonNode n : optsNode) {
                options.add(n.asText());
            }
        }

        String correctAnswer = textOr(root, "correctAnswer", "answer");
        if (correctAnswer == null) correctAnswer = "";
        if (correctAnswer.length() > 512) {
            correctAnswer = correctAnswer.substring(0, 512);
        }
        String explanation = textOr(root, "explanation", "why");

        // If AI returns difficulty, override only if it's valid
        String aiDiff = textOr(root, "difficulty", null);
        String finalDifficulty = storageDifficulty;
        if (aiDiff != null && !aiDiff.isBlank()) {
            finalDifficulty = DifficultyMapper.toStorageString(DifficultyMapper.fromString(aiDiff));
        }

        QuizQuestion entity = QuizQuestion.builder()
                .skill(skill)
                .difficulty(finalDifficulty)
                .questionText(questionText)
                .options(objectMapper.writeValueAsString(options))
                .correctAnswer(correctAnswer != null ? correctAnswer : "")
                .explanation(explanation)
                .build();

        return quizQuestionRepository.save(entity);
    }

    private String textOr(JsonNode root, String primary, String alt) {
        if (root == null) return null;
        if (primary != null && root.hasNonNull(primary)) {
            return root.get(primary).asText();
        }
        if (alt != null && root.hasNonNull(alt)) {
            return root.get(alt).asText();
        }
        return null;
    }
}
