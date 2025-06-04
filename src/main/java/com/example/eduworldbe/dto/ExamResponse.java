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
  private Integer easyCount;
  private Integer mediumCount;
  private Integer hardCount;
  private Integer veryHardCount;
  private Integer totalQuestions;
  private Integer questionBankSize;
  private Double averageRating;
  private Integer reviewCount;
  private Boolean allowReview;
  private Integer maxAttempts;
  private Boolean allowViewAnswer;
  private Double easyScore;
  private Double mediumScore;
  private Double hardScore;
  private Double veryHardScore;
  private String className;
  private String subjectName;
  private Integer grade;
  private boolean favourite;

  public ExamResponse() {
  }

  public void setVeryHardScore(Double veryHardScore) {
    this.veryHardScore = veryHardScore;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getSubjectName() {
    return subjectName;
  }

  public void setSubjectName(String subjectName) {
    this.subjectName = subjectName;
  }

  public Integer getGrade() {
    return grade;
  }

  public void setGrade(Integer grade) {
    this.grade = grade;
  }
}