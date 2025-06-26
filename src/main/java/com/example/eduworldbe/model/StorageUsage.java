package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "storage_usage")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageUsage {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(nullable = false)
  private String userId;

  @Column(nullable = false)
  private String fileName;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String fileUrl;

  @Column(nullable = false)
  private Long fileSize; // Size in bytes

  @Column(nullable = false)
  private String fileType; // "image", "audio", "video", "pdf", "text"

  @Column(nullable = false)
  private Date uploadTime;
}