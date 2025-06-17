package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Attempt;
import com.example.eduworldbe.model.Question;
import com.example.eduworldbe.repository.AttemptRepository;
import com.example.eduworldbe.repository.ExamRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AttemptService {
  @Autowired
  private AttemptRepository attemptRepository;

  @Autowired
  private ExamRepository examRepository;

  @Autowired
  private ExamService examService;

  public Attempt create(Attempt attempt) {
    // If this is a new attempt, generate questions based on exam settings
    if (attempt.getQuestionIds() == null || attempt.getQuestionIds().isEmpty()) {
      examRepository.findById(attempt.getExamId())
          .orElseThrow(() -> new RuntimeException("Exam not found"));

      List<Question> generatedQuestions = examService.generateExamQuestions(attempt.getExamId());

      List<String> questionIds = generatedQuestions.stream()
          .map(Question::getId)
          .toList();

      attempt.setQuestionIds(questionIds);
    }

    return attemptRepository.save(attempt);
  }

  public Optional<Attempt> getById(String id) {
    return attemptRepository.findById(id);
  }

  public List<Attempt> getByUserId(String userId) {
    return attemptRepository.findByUserId(userId);
  }

  public List<Attempt> getByExamId(String examId) {
    return attemptRepository.findByExamId(examId);
  }

  public List<Attempt> getByUserIdAndExamId(String userId, String examId) {
    return attemptRepository.findByUserIdAndExamId(userId, examId);
  }

  public Optional<Attempt> getActiveAttempt(String userId, String examId) {
    return attemptRepository.findByUserIdAndExamIdAndSubmitted(userId, examId, false);
  }

  @Transactional
  public Attempt submitAttempt(String attemptId) {
    Optional<Attempt> attemptOpt = attemptRepository.findById(attemptId);

    if (attemptOpt.isPresent()) {
      Attempt attempt = attemptOpt.get();

      if (attempt.getSubmitted()) {
        throw new RuntimeException("Attempt already submitted");
      }

      attempt.setSubmitted(true);
      attempt.setEndTime(new Date());

      attempt.setScore(0.0);
      attempt.setPercentageScore(0.0);

      return attemptRepository.save(attempt);
    } else {
      throw new RuntimeException("Attempt not found");
    }
  }

  public List<Attempt> getBestAttemptsByUserAndExam(String userId, String examId) {
    return attemptRepository.findBestAttemptsByUserAndExam(userId, examId);
  }

  public List<Attempt> getTopAttemptsByExam(String examId) {
    return attemptRepository.findTopAttemptsByExam(examId);
  }
}