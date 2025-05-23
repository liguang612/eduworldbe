package com.example.eduworldbe.dto;

import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ExamAttemptDetailResponse {
  private String id;
  private String title;
  private Double score;
  private Double maxScore;
  private Date startTime;
  private Date endTime;
  private String status;
  private String className;
  private String classId;
  private Map<String, String> answers;
  private List<QuestionDetailResponse> questions;
  private Map<String, Object> correctAnswers;
}