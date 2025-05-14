package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "fill_in_blank")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FillInBlank {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String questionId;
  private String blankKey; // ví dụ: blank1, blank2
  private String correctAnswer;
}