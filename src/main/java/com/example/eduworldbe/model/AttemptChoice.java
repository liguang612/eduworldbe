package com.example.eduworldbe.model;

import java.util.Date;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "attempt_choices")
@Data
public class AttemptChoice {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(name = "question_id")
  private String questionId;

  @Column(name = "choice_id")
  private String choiceId;

  @Column(name = "is_correct")
  private Boolean isCorrect;

  @Column(name = "order_index")
  private Integer orderIndex;

  private String text;
  private String value;

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
}