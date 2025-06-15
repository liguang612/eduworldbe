package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "post")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(columnDefinition = "MEDIUMTEXT")
  private String content;

  @ElementCollection
  @Column(columnDefinition = "TEXT")
  private List<String> imageUrls;

  private LocalDateTime createdAt;

  private String userId;

  private String courseId;

  private boolean approved = false;

  @OneToMany(mappedBy = "postId", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Comment> comments;
}