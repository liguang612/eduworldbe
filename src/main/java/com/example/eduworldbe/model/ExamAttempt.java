package com.example.eduworldbe.model;

import java.util.Date;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "exam_attempts")
@Data
public class ExamAttempt {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(name = "exam_id")
  private String examId;

  @Column(name = "class_id")
  private String classId;

  private Integer duration;

  @Column(name = "max_score")
  private Double maxScore;

  private String title;

  @Column(name = "easy_score")
  private Double easyScore;

  @Column(name = "medium_score")
  private Double mediumScore;

  @Column(name = "hard_score")
  private Double hardScore;

  @Column(name = "very_hard_score")
  private Double veryHardScore;

  @Column(name = "user_id")
  private String userId;

  @Column(name = "start_time")
  private Date startTime;

  @Column(name = "end_time")
  private Date endTime;

  private String status;

  private Double score;

  @Column(name = "created_at")
  private Date createdAt;

  @Column(name = "updated_at")
  private Date updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = new Date();
    updatedAt = new Date();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = new Date();
  }

  public ExamAttempt copyWith() {
    ExamAttempt copy = new ExamAttempt();
    copy.setId(this.id);
    copy.setExamId(this.examId);
    copy.setClassId(this.classId);
    copy.setDuration(this.duration);
    copy.setMaxScore(this.maxScore);
    copy.setTitle(this.title);
    copy.setEasyScore(this.easyScore);
    copy.setMediumScore(this.mediumScore);
    copy.setHardScore(this.hardScore);
    copy.setVeryHardScore(this.veryHardScore);
    copy.setUserId(this.userId);
    copy.setStartTime(this.startTime);
    copy.setEndTime(this.endTime);
    copy.setStatus(this.status);
    copy.setScore(this.score);
    copy.setCreatedAt(this.createdAt);
    copy.setUpdatedAt(this.updatedAt);
    return copy;
  }
}