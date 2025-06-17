package com.example.eduworldbe.dto.request;

import lombok.Data;
import java.util.Map;

@Data
public class SimpleGradeRequest {
  private Map<String, Object> answers;
}