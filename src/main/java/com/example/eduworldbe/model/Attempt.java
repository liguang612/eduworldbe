package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "attempt", indexes = {
    @Index(name = "idx_attempt_user_id", columnList = "userId"),
    @Index(name = "idx_attempt_exam_id", columnList = "examId"),
    @Index(name = "idx_attempt_start_time", columnList = "startTime"),
    @Index(name = "idx_attempt_end_time", columnList = "endTime"),
    @Index(name = "idx_attempt_submitted", columnList = "submitted")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attempt {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String userId;
  private String examId;

  @ElementCollection
  private List<String> questionIds;

  private Date startTime;
  private Date endTime;
  private Boolean submitted = false;
  private Double score;
  private Double percentageScore;
  private Date submittedAt;

  private Double easyScore;
  private Double mediumScore;
  private Double hardScore;
  private Double veryHardScore;

  @PrePersist
  protected void onCreate() {
    startTime = new Date();
  }
}