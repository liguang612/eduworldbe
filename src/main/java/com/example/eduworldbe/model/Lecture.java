package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Table(name = "lecture")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lecture {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(columnDefinition = "MEDIUMTEXT")
  private String contents;

  @ElementCollection
  private List<String> endQuestions;

  @ElementCollection
  private List<String> categories;

  @ElementCollection
  private List<String> reviewIds;

  private String subjectId;
  private String teacherId;

  private Integer duration;
}
