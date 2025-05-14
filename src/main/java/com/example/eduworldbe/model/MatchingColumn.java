package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "matching_column")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchingColumn {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String questionId;
  private String side; // LEFT or RIGHT
  private String content;
  private Integer orderIndex;
}