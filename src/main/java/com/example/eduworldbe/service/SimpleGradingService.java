package com.example.eduworldbe.service;

import com.example.eduworldbe.dto.request.SimpleGradeRequest;
import com.example.eduworldbe.dto.response.QuestionDetailResponse;
import com.example.eduworldbe.dto.response.SimpleGradeResponse;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SimpleGradingService {
  @Autowired
  private QuestionService questionService;

  @Autowired
  private HttpServletRequest httpRequest;

  public SimpleGradeResponse gradeAnswers(SimpleGradeRequest request) {
    SimpleGradeResponse response = new SimpleGradeResponse();
    Map<String, Boolean> results = new HashMap<>();
    Map<String, Object> correctAnswers = new HashMap<>();
    int correctCount = 0;

    List<String> questionIds = request.getAnswers().keySet().stream().toList();
    List<QuestionDetailResponse> questions = questionService.getQuestionDetailsByIds(questionIds,
        httpRequest);

    // Chấm điểm và thu thập đáp án đúng
    for (QuestionDetailResponse question : questions) {
      String questionId = question.getId();
      Object userAnswer = request.getAnswers().get(questionId);

      // Chấm điểm
      boolean isCorrect = questionService.checkAnswer(question, userAnswer);
      results.put(questionId, isCorrect);
      if (isCorrect) {
        correctCount++;
      }

      // Lấy đáp án đúng
      Object correctAnswer = questionService.getCorrectAnswerFormatted(question);
      correctAnswers.put(questionId, correctAnswer);
    }

    response.setResults(results);
    response.setCorrectCount(correctCount);
    response.setTotalCount(questions.size());
    response.setCorrectAnswers(correctAnswers);

    return response;
  }
}