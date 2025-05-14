package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "matching_pair")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchingPair {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String questionId;
  private String leftColumnId;
  private String rightColumnId;
}