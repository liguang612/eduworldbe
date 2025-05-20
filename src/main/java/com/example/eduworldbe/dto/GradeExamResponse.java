package com.example.eduworldbe.dto;

import lombok.Data;
import java.util.Map;

@Data
public class GradeExamResponse {
  private String attemptId;
  private String examId;
  private String userId;
  private Integer totalScore; // Tổng điểm
  private Map<String, Integer> questionScores; // Map chứa điểm từng câu: key là questionId, value là điểm
}