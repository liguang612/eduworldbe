package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "matching_pair", indexes = {
    @Index(name = "idx_matching_pair_question_id", columnList = "questionId"),
    @Index(name = "idx_matching_pair_source", columnList = "source"),
    @Index(name = "idx_matching_pair_target", columnList = "target")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchingPair {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(name = "source")
  private String from;

  @Column(name = "target")
  private String to;

  private String questionId;
}