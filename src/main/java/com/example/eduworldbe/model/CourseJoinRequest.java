package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "course_join_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseJoinRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String courseId;
  private String userId;
  private Integer role; // 0: student, 1: assistant
  private Integer status; // 0: pending, 1: approved, 2: rejected

  @Temporal(TemporalType.TIMESTAMP)
  private Date createdAt;
}
