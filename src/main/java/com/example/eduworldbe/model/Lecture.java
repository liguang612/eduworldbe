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
  private String description;

  // Nội dung bài giảng: lưu JSON hoặc HTML
  @Column(columnDefinition = "TEXT")
  private String contents;

  // Danh sách ID câu hỏi cuối bài
  @ElementCollection
  private List<String> endQuestions;

  // Danh sách hashtag
  @ElementCollection
  private List<String> categories;

  // ID của khóa học chứa bài giảng
  private String courseId; // optional, không bắt buộc

  private String teacherId; // id của giáo viên tạo bài giảng
}
