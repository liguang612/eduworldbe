package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "choice", indexes = {
    @Index(name = "idx_choice_question_id", columnList = "questionId"),
    @Index(name = "idx_choice_order", columnList = "orderIndex"),
    @Index(name = "idx_choice_question_value", columnList = "questionId,value", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Choice {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(columnDefinition = "TEXT")
  private String text;

  private String value;
  private String questionId;
  private Integer orderIndex;

  private Boolean isCorrect = false;
}