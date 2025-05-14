package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "question")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(columnDefinition = "TEXT")
  private String text;
  private Integer type; // 1: MCQ, 2: Matching, 3: Ordering, 4: ShortAnswer, 5: FillInBlank, 6: DragDrop
  private String mediaUrl;
  private Integer mediaType; // 0: image, 1: audio, 2: video, 3: pdf
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