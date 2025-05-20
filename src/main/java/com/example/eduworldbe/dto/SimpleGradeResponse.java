package com.example.eduworldbe.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SimpleGradeResponse {
  private Map<String, Boolean> results; // Map chứa kết quả: key là questionId, value là true/false
  private Integer correctCount; // Số câu đúng
  private Integer totalCount; // Tổng số câu
}