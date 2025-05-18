package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "shared_media")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SharedMedia {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String mediaUrl;
  private Integer mediaType; // 0: image, 1: audio, 2: video, 3: pdf, 4: text

  @Column(columnDefinition = "TEXT")
  private String text; // For shared text content (e.g., reading comprehension passages)

  private String title;

  @Column(nullable = false)
  private Integer usageCount = 0; // Track number of questions using this shared media
}