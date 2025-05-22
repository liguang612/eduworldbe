package com.example.eduworldbe.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.eduworldbe.exception.ResourceNotFoundException;
import com.example.eduworldbe.model.AttemptChoice;
import com.example.eduworldbe.model.AttemptMatchingColumn;
import com.example.eduworldbe.model.AttemptMatchingPair;
import com.example.eduworldbe.model.AttemptQuestion;
import com.example.eduworldbe.model.Choice;
import com.example.eduworldbe.model.Exam;
import com.example.eduworldbe.model.ExamAttempt;
import com.example.eduworldbe.model.MatchingColumn;
import com.example.eduworldbe.model.MatchingPair;
import com.example.eduworldbe.model.Question;
import com.example.eduworldbe.repository.AttemptChoiceRepository;
import com.example.eduworldbe.repository.AttemptMatchingColumnRepository;
import com.example.eduworldbe.repository.AttemptMatchingPairRepository;
import com.example.eduworldbe.repository.AttemptQuestionRepository;
import com.example.eduworldbe.repository.ChoiceRepository;
import com.example.eduworldbe.repository.ExamRepository;
import com.example.eduworldbe.repository.ExamAttemptRepository;
import com.example.eduworldbe.repository.MatchingColumnRepository;
import com.example.eduworldbe.repository.MatchingPairRepository;
import com.example.eduworldbe.repository.QuestionRepository;
import com.example.eduworldbe.dto.QuestionDetailResponse;

import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
@Transactional
@Slf4j
public class ExamAttemptService {
  private final ExamAttemptRepository examAttemptRepository;
  private final AttemptQuestionRepository attemptQuestionRepository;
  private final AttemptChoiceRepository attemptChoiceRepository;
  private final AttemptMatchingColumnRepository attemptMatchingColumnRepository;
  private final AttemptMatchingPairRepository attemptMatchingPairRepository;
  private final ExamRepository examRepository;
  private final QuestionRepository questionRepository;
  private final ChoiceRepository choiceRepository;
  private final MatchingColumnRepository matchingColumnRepository;
  private final MatchingPairRepository matchingPairRepository;
  @Autowired
  private QuestionService questionService;
  @Autowired
  private ObjectMapper objectMapper;

  public ExamAttemptService(
      ExamAttemptRepository examAttemptRepository,
      AttemptQuestionRepository attemptQuestionRepository,
      AttemptChoiceRepository attemptChoiceRepository,
      AttemptMatchingColumnRepository attemptMatchingColumnRepository,
      AttemptMatchingPairRepository attemptMatchingPairRepository,
      ExamRepository examRepository,
      QuestionRepository questionRepository,
      ChoiceRepository choiceRepository,
      MatchingColumnRepository matchingColumnRepository,
      MatchingPairRepository matchingPairRepository) {
    this.examAttemptRepository = examAttemptRepository;
    this.attemptQuestionRepository = attemptQuestionRepository;
    this.attemptChoiceRepository = attemptChoiceRepository;
    this.attemptMatchingColumnRepository = attemptMatchingColumnRepository;
    this.attemptMatchingPairRepository = attemptMatchingPairRepository;
    this.examRepository = examRepository;
    this.questionRepository = questionRepository;
    this.choiceRepository = choiceRepository;
    this.matchingColumnRepository = matchingColumnRepository;
    this.matchingPairRepository = matchingPairRepository;
  }

  public ExamAttempt startAttempt(String userId, String examId) {
    // Kiểm tra xem user đã có attempt đang in_progress chưa
    Optional<ExamAttempt> existingAttempt = examAttemptRepository.findByUserIdAndExamIdAndStatus(userId, examId,
        "in_progress");
    if (existingAttempt.isPresent()) {
      return existingAttempt.get();
    }

    // 1. Lấy thông tin đề thi
    Exam exam = examRepository.findById(examId)
        .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

    // 2. Tạo attempt mới
    ExamAttempt attempt = new ExamAttempt();
    attempt.setExamId(examId);
    attempt.setClassId(exam.getClassId());
    attempt.setDuration(exam.getDurationMinutes());
    attempt.setMaxScore(exam.getMaxScore().doubleValue());
    attempt.setTitle(exam.getTitle());
    attempt.setEasyScore(exam.getEasyScore());
    attempt.setMediumScore(exam.getMediumScore());
    attempt.setHardScore(exam.getHardScore());
    attempt.setVeryHardScore(exam.getVeryHardScore());
    attempt.setUserId(userId);
    attempt.setStartTime(new Date());
    attempt.setStatus("in_progress");
    attempt = examAttemptRepository.save(attempt);

    // 3. Copy questions
    List<Question> questions = questionRepository.findAllById(exam.getQuestionIds());
    for (Question question : questions) {
      AttemptQuestion attemptQuestion = new AttemptQuestion();
      attemptQuestion.setAttemptId(attempt.getId());
      attemptQuestion.setQuestionId(question.getId());
      attemptQuestion.setLevel(String.valueOf(question.getLevel()));
      attemptQuestion.setType(question.getType());
      attemptQuestion.setTitle(question.getTitle());
      attemptQuestion.setSharedMedia(question.getSharedMedia());
      attemptQuestion.setAnswer(""); // Khởi tạo câu trả lời rỗng
      attemptQuestion = attemptQuestionRepository.save(attemptQuestion);

      // 4. Copy choices hoặc matching data tùy theo loại câu hỏi
      if ("itemConnector".equals(question.getType())) {
        // Copy matching columns
        List<MatchingColumn> matchingColumns = matchingColumnRepository.findByQuestionId(question.getId());
        for (MatchingColumn column : matchingColumns) {
          AttemptMatchingColumn attemptColumn = new AttemptMatchingColumn();
          attemptColumn.setQuestionId(attemptQuestion.getId());
          attemptColumn.setMatchingColumnId(column.getId());
          attemptColumn.setSide(column.getSide());
          attemptColumn.setLabel(column.getLabel());
          attemptMatchingColumnRepository.save(attemptColumn);
        }

        // Copy matching pairs
        List<MatchingPair> matchingPairs = matchingPairRepository.findByQuestionId(question.getId());
        for (MatchingPair pair : matchingPairs) {
          AttemptMatchingPair attemptPair = new AttemptMatchingPair();
          attemptPair.setQuestionId(attemptQuestion.getId());
          attemptPair.setMatchingPairId(pair.getId());
          attemptPair.setSource(pair.getFrom());
          attemptPair.setTarget(pair.getTo());
          attemptMatchingPairRepository.save(attemptPair);
        }
      } else {
        // Copy choices
        List<Choice> choices = choiceRepository.findByQuestionId(question.getId());
        for (Choice choice : choices) {
          AttemptChoice attemptChoice = new AttemptChoice();
          attemptChoice.setQuestionId(attemptQuestion.getId());
          attemptChoice.setChoiceId(choice.getId());
          attemptChoice.setIsCorrect(choice.getIsCorrect());
          attemptChoice.setOrderIndex(choice.getOrderIndex());
          attemptChoice.setText(choice.getText());
          attemptChoice.setValue(choice.getValue());
          attemptChoiceRepository.save(attemptChoice);
        }
      }
    }

    // Attempt mới thì status là started (Phải copyWith vì nếu như setStatus ở
    // instance dùng để save thì DB biến đổi theo :/)
    ExamAttempt newAttempt = attempt.copyWith();
    newAttempt.setStatus("started");
    return newAttempt;
  }

  public void saveAnswer(String attemptId, String questionId, String answer) {
    AttemptQuestion attemptQuestion = attemptQuestionRepository
        .findByAttemptIdAndQuestionId(attemptId, questionId)
        .orElseThrow(() -> new ResourceNotFoundException("Question not found in attempt"));
    attemptQuestion.setAnswer(answer);
    attemptQuestionRepository.save(attemptQuestion);
  }

  public void submitAttempt(String attemptId) {
    ExamAttempt attempt = examAttemptRepository.findById(attemptId)
        .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

    // Tính điểm và cập nhật attempt
    Double score = calculateScore(attemptId);
    attempt.setStatus("submitted");
    attempt.setEndTime(new Date());
    attempt.setScore(score);
    examAttemptRepository.save(attempt);
  }

  private Double calculateScore(String attemptId) {
    // 1. Lấy thông tin attempt
    ExamAttempt attempt = examAttemptRepository.findById(attemptId)
        .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

    // 2. Lấy danh sách câu hỏi của attempt
    List<AttemptQuestion> attemptQuestions = attemptQuestionRepository.findByAttemptId(attemptId);
    double totalScore = 0.0;

    // 3. Chấm điểm từng câu
    for (AttemptQuestion attemptQuestion : attemptQuestions) {
      // Lấy thông tin câu hỏi gốc
      Question question = questionRepository.findById(attemptQuestion.getQuestionId())
          .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

      // Lấy câu trả lời của user
      String userAnswer = attemptQuestion.getAnswer();

      // Kiểm tra đáp án và tính điểm dựa trên level của câu hỏi
      boolean isCorrect = checkAnswer(question, userAnswer);
      if (isCorrect) {
        switch (question.getLevel()) {
          case 1: // Easy
            totalScore += attempt.getEasyScore();
            break;
          case 2: // Medium
            totalScore += attempt.getMediumScore();
            break;
          case 3: // Hard
            totalScore += attempt.getHardScore();
            break;
          case 4: // Very Hard
            totalScore += attempt.getVeryHardScore();
            break;
        }
      }
    }

    return totalScore;
  }

  private boolean checkAnswer(Question question, String userAnswer) {
    if (userAnswer == null || userAnswer.isEmpty()) {
      return false;
    }

    try {
      // Chuyển đổi Question thành QuestionDetailResponse
      QuestionDetailResponse questionDetail = new QuestionDetailResponse(question);

      // Parse userAnswer thành Object tương ứng với loại câu hỏi
      Object parsedAnswer;
      switch (question.getType()) {
        case "radio":
        case "shortAnswer":
          parsedAnswer = userAnswer;
          break;
        case "checkbox":
        case "ranking":
          parsedAnswer = objectMapper.readValue(userAnswer, new TypeReference<List<String>>() {
          });
          break;
        case "itemConnector":
          parsedAnswer = objectMapper.readValue(userAnswer, new TypeReference<List<Map<String, String>>>() {
          });
          break;
        default:
          return false;
      }

      // Sử dụng QuestionService để kiểm tra đáp án
      return questionService.checkAnswer(questionDetail, parsedAnswer);
    } catch (Exception e) {
      log.error("Error checking answer", e);
      return false;
    }
  }

  @Transactional
  public void deleteAttempt(String attemptId) {
    List<AttemptQuestion> attemptQuestions = attemptQuestionRepository.findByAttemptId(attemptId);
    for (AttemptQuestion attemptQuestion : attemptQuestions) {
      attemptChoiceRepository.deleteByQuestionId(attemptQuestion.getId());
      attemptMatchingColumnRepository.deleteByQuestionId(attemptQuestion.getId());
      attemptMatchingPairRepository.deleteByQuestionId(attemptQuestion.getId());

      attemptQuestionRepository.delete(attemptQuestion);
    }

    examAttemptRepository.deleteById(attemptId);
  }

  public Map<String, String> getSavedAnswers(String attemptId) {
    List<AttemptQuestion> attemptQuestions = attemptQuestionRepository.findByAttemptId(attemptId);
    return attemptQuestions.stream()
        .collect(Collectors.toMap(
            AttemptQuestion::getQuestionId,
            AttemptQuestion::getAnswer));
  }
}