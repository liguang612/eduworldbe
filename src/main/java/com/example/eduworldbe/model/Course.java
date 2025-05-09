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

  // Tham chiếu đến id của subject
  private String subjectId;

  private String teacherId; // id của giáo viên tạo lớp học

  // Danh sách hashtag (lưu dạng JSON hoặc TEXT, hoặc tạo bảng phụ nếu muốn)
  @ElementCollection
  private List<String> allCategories;

  // Danh sách id trợ giảng
  @ElementCollection
  private List<String> tAIds;

  // Danh sách id học sinh
  @ElementCollection
  private List<String> studentIds;

  // Danh sách id bài giảng
  @ElementCollection
  private List<String> lectureIds;

  // Danh sách id review
  @ElementCollection
  private List<String> reviewIds;
}
