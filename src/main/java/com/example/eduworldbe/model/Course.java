package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Table(name = "course")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String name;
  private String avatar;
  private String description;

  // Tham chiếu đến id của subject
  private String subjectId;

  private String teacherId;

  private boolean hidden = false;

  private boolean allowStudentPost = true;
  private boolean requirePostApproval = true;

  @ElementCollection
  private List<String> allCategories;

  @ElementCollection
  private List<String> teacherAssistantIds;

  @ElementCollection
  private List<String> studentIds;

  @ElementCollection
  private List<String> chapterIds;

  @ElementCollection
  private List<String> reviewIds;

  @ElementCollection
  private List<String> pendingStudentIds;
}
