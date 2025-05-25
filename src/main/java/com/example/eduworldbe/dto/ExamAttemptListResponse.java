package com.example.eduworldbe.dto;

import java.util.Date;

import lombok.Data;

@Data
public class ExamAttemptListResponse {
  private String id;
  private String title;
  private Double score;
  private Double maxScore;
  private Date startTime;
  private Date endTime;
  private String status;
  private String className;
  private String classId;
  private String userId;

  // Thông tin học sinh
  private String studentName;
  private String studentAvatar;
  private String studentSchool;
  private Integer studentGrade;
}