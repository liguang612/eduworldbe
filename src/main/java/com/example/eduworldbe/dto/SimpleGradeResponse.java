package com.example.eduworldbe.dto;

import lombok.Data;
import java.util.Map;

@Data
public class SimpleGradeResponse {
  private Map<String, Boolean> results;
  private Integer correctCount;
  private Integer totalCount;
  private Map<String, Object> correctAnswers;
}