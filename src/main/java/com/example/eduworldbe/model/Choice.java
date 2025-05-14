package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "choice")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Choice {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String questionId;
  private String text;
  private String imageUrl;
  private Boolean isAnswer;
  private Integer orderIndex; // dùng cho ordering, dragdrop
}