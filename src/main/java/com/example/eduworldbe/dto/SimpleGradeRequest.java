package com.example.eduworldbe.dto;

import lombok.Data;
import java.util.Map;

@Data
public class SimpleGradeRequest {
  private Map<String, String> answers; // Map chứa câu trả lời: key là questionId, value là đáp án
}