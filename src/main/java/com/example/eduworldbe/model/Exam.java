package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "exam", indexes = {
    @Index(name = "idx_exam_class_id", columnList = "classId"),
    @Index(name = "idx_exam_created_by", columnList = "createdBy"),
    @Index(name = "idx_exam_open_time", columnList = "openTime"),
    @Index(name = "idx_exam_close_time", columnList = "closeTime")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Exam {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String classId;
  private String title;

  @ElementCollection
  private List<String> questionIds;

  private Integer easyCount = 0;
  private Integer mediumCount = 0;
  private Integer hardCount = 0;
  private Integer veryHardCount = 0;

  // Điểm số cho từng level
  private Integer easyScore = 1; // Điểm cho câu level 1
  private Integer mediumScore = 2; // Điểm cho câu level 2
  private Integer hardScore = 3; // Điểm cho câu level 3
  private Integer veryHardScore = 4; // Điểm cho câu level 4

  private Date openTime;
  private Date closeTime;

  @ElementCollection
  private List<String> reviewIds;

  private Integer maxScore = 100;

  private Integer durationMinutes = 60;

  private Boolean shuffleQuestion = false;
  private Boolean shuffleChoice = false;

  private String createdBy;
  private Date createdAt;
  private Date updatedAt;

  @ElementCollection
  private List<String> categories;

  private Boolean allowReview = true;

  private Integer maxAttempts = 1;

  @PrePersist
  protected void onCreate() {
    createdAt = new Date();
    updatedAt = new Date();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = new Date();
  }
}