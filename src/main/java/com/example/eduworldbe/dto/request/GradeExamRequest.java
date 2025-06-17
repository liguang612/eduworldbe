package com.example.eduworldbe.dto.request;

import lombok.Data;
import java.util.Map;

@Data
public class GradeExamRequest {
  private String attemptId;
  private Map<String, String> answers;
}