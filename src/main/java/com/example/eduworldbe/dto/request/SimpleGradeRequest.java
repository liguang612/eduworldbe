package com.example.eduworldbe.dto.request;

import lombok.Data;
import java.util.Map;

@Data
public class SimpleGradeRequest {
  // questionId - answer object ( value, value[], pair[])
  private Map<String, Object> answers;
}