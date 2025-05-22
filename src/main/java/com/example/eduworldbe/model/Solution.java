package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "solution", indexes = {
    @Index(name = "idx_solution_question_id", columnList = "questionId"),
    @Index(name = "idx_solution_created_by", columnList = "createdBy"),
    @Index(name = "idx_solution_status", columnList = "status"),
    @Index(name = "idx_solution_created_at", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Solution {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String questionId;
  private String createdBy;

  @Column(columnDefinition = "MEDIUMTEXT")
  private String content;

  private String status; // PENDING, APPROVED, REJECTED

  private String reviewedBy;
  private Date reviewedAt;

  private Date createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = new Date();
    status = "PENDING";
  }
}