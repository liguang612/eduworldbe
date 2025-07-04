package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Attempt;
import com.example.eduworldbe.model.Exam;
import com.example.eduworldbe.model.Question;
import com.example.eduworldbe.dto.request.GradeExamRequest;
import com.example.eduworldbe.dto.response.GradeExamResponse;
import com.example.eduworldbe.dto.response.QuestionDetailResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ExamGradingService {
  @Autowired
  private AttemptService attemptService;

  @Autowired
  private ExamService examService;

  @Autowired
  private QuestionService questionService;

  @Autowired
  private HttpServletRequest httpRequest;

  @Transactional
  public GradeExamResponse gradeExam(GradeExamRequest request) {
    // 1. Lấy thông tin attempt
    Optional<Attempt> attemptOpt = attemptService.getById(request.getAttemptId());
    if (attemptOpt.isEmpty()) {
      throw new RuntimeException("Attempt not found");
    }
    Attempt attempt = attemptOpt.get();

    // 2. Lấy thông tin exam
    Optional<Exam> examOpt = examService.getById(attempt.getExamId());
    if (examOpt.isEmpty()) {
      throw new RuntimeException("Exam not found");
    }
    Exam exam = examOpt.get();

    // 3. Lấy danh sách câu hỏi
    List<Question> questions = examService.getExamQuestions(exam.getId());

    // 3.5. Get detailed question information
    List<String> questionIds = questions.stream()
        .map(Question::getId)
        .collect(Collectors.toList());
    Map<String, QuestionDetailResponse> questionDetailsMap = questionService
        .getQuestionDetailsByIds(questionIds, httpRequest).stream()
        .collect(Collectors.toMap(QuestionDetailResponse::getId, detail -> detail));

    // 4. Tạo response
    GradeExamResponse response = new GradeExamResponse();
    response.setAttemptId(attempt.getId());
    response.setExamId(exam.getId());
    response.setUserId(attempt.getUserId());

    // 5. Chấm điểm từng câu
    Map<String, Double> questionScores = new HashMap<>();
    double totalScore = 0;

    for (Question question : questions) {
      String userAnswer = request.getAnswers().get(question.getId());

      QuestionDetailResponse questionDetail = questionDetailsMap.get(question.getId());
      boolean isCorrect = questionService.checkAnswer(questionDetail, userAnswer);

      // Tính điểm dựa trên level của câu hỏi
      double score = 0;
      if (isCorrect) {
        switch (question.getLevel()) {
          case 1:
            score = attempt.getEasyScore();
            break;
          case 2:
            score = attempt.getMediumScore();
            break;
          case 3:
            score = attempt.getHardScore();
            break;
          case 4:
            score = attempt.getVeryHardScore();
            break;
        }
      }

      questionScores.put(question.getId(), score);
      totalScore += score;
    }

    response.setQuestionScores(questionScores);
    response.setTotalScore(totalScore);

    // 6. Cập nhật attempt
    attempt.setScore(totalScore);
    attempt.setSubmitted(true);
    // attemptService.update(attempt.getId(), attempt);

    return response;
  }
}