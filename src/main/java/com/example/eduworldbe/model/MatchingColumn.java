package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "matching_column", indexes = {
    @Index(name = "idx_matching_column_question_id", columnList = "questionId"),
    @Index(name = "idx_matching_column_side", columnList = "side"),
    @Index(name = "idx_matching_column_order", columnList = "orderIndex")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchingColumn {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(columnDefinition = "TEXT")
  private String label;

  private String questionId;
  private String side;
  private Integer orderIndex;
}