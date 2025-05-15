package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "question", indexes = {
    @Index(name = "idx_question_type", columnList = "type"),
    @Index(name = "idx_question_created_by", columnList = "createdBy"),
    @Index(name = "idx_question_created_at", columnList = "createdAt"),
    @Index(name = "idx_question_subject_id", columnList = "subjectId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(columnDefinition = "TEXT")
  private String title;

  @Column(nullable = false)
  private String subjectId;

  private String type; // radio, checkbox, itemConnector, ordering, shortAnswer

  @ManyToOne
  @JoinColumn(name = "shared_media_id")
  private SharedMedia sharedMedia;

  private Integer level;
  private String createdBy;
  @ElementCollection
  private List<String> categories;
  @ElementCollection
  private List<String> solutionIds;
  @ElementCollection
  private List<String> reviewIds;
  private Date createdAt;
  private Date updatedAt;
}