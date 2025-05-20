package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

  @ElementCollection
  @CollectionTable(name = "attempt_choices", joinColumns = @JoinColumn(name = "attempt_id"))
  @MapKeyColumn(name = "question_id")
  @Column(name = "choice_ids")
  private Map<String, String> choicesSelected;

  private Date startTime;
  private Date endTime;
  private Boolean submitted = false;
  private Integer score;
  private Double percentageScore;
  private Date submittedAt;

  // Điểm số cho từng level
  private Integer easyScore; // Điểm cho câu level 1
  private Integer mediumScore; // Điểm cho câu level 2
  private Integer hardScore; // Điểm cho câu level 3
  private Integer veryHardScore; // Điểm cho câu level 4

  @PrePersist
  protected void onCreate() {
    startTime = new Date();
  }
}