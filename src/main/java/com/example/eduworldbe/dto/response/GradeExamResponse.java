package com.example.eduworldbe.dto.response;

import lombok.Data;
import java.util.Map;

@Data
public class GradeExamResponse {
  private String attemptId;
  private String examId;
  private String userId;
  private Double totalScore;
  private Map<String, Double> questionScores;
}