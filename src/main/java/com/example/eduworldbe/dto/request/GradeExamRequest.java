package com.example.eduworldbe.dto.request;

import lombok.Data;
import java.util.Map;

@Data
public class GradeExamRequest {
  private String attemptId; // ID của attempt cần chấm điểm
  private Map<String, String> answers; // Map chứa câu trả lời: key là questionId, value là đáp án
}