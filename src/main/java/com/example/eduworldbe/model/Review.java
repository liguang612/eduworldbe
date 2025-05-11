package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Entity
@Table(name = "review")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String userId;
  private Integer targetType; // 1: course, 2: lecture, 3: question, 4: exam
  private String targetId;
  private int score; // 1-5
  private String comment;

  @Temporal(TemporalType.TIMESTAMP)
  private Date createdAt;
}