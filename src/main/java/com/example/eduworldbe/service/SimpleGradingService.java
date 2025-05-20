package com.example.eduworldbe.service;

import com.example.eduworldbe.dto.SimpleGradeRequest;
import com.example.eduworldbe.dto.SimpleGradeResponse;
import com.example.eduworldbe.model.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SimpleGradingService {
  @Autowired
  private QuestionService questionService;

  public SimpleGradeResponse gradeAnswers(SimpleGradeRequest request) {
    SimpleGradeResponse response = new SimpleGradeResponse();
    Map<String, Boolean> results = new HashMap<>();
    int correctCount = 0;

    // Lấy danh sách câu hỏi
    List<Question> questions = questionService.getByIds(request.getAnswers().keySet());

    // Chấm điểm từng câu
    for (Question question : questions) {
      String userAnswer = request.getAnswers().get(question.getId());
      boolean isCorrect = questionService.checkAnswer(question, userAnswer);
      results.put(question.getId(), isCorrect);
      if (isCorrect) {
        correctCount++;
      }
    }

    response.setResults(results);
    response.setCorrectCount(correctCount);
    response.setTotalCount(questions.size());

    return response;
  }
}