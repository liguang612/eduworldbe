package com.example.eduworldbe.dto;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class ExamResponse {
  private String id;
  private String classId;
  private String title;
  private Date openTime;
  private Date closeTime;
  private Integer maxScore;
  private Integer durationMinutes;
  private Boolean shuffleQuestion;
  private Boolean shuffleChoice;
  private String createdBy;
  private Date createdAt;
  private Date updatedAt;
  private List<String> categories;
  private Integer level1Count;
  private Integer level2Count;
  private Integer level3Count;
  private Integer level4Count;
  private Integer totalQuestions;
  private Integer questionBankSize;
  private Double averageRating;
  private Integer reviewCount;
  private Boolean allowReview;
  private Integer maxAttempts;
}