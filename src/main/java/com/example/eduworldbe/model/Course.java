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
  private String avatar; // URL đến ảnh đại diện của khóa học
  private String description; // Mô tả chi tiết về khóa học

  // Tham chiếu đến id của subject
  private String subjectId;

  private String teacherId; // id của giáo viên tạo lớp học

  private boolean hidden = false; // Mặc định là false, tức là hiển thị

  @ElementCollection
  private List<String> allCategories;

  // Danh sách id trợ giảng
  @ElementCollection
  private List<String> teacherAssistantIds;

  // Danh sách id học sinh
  @ElementCollection
  private List<String> studentIds;

  // Danh sách id của các chapter
  @ElementCollection
  private List<String> chapterIds;

  // Danh sách id review
  @ElementCollection
  private List<String> reviewIds;

  // Danh sách id học sinh đang chờ phê duyệt
  @ElementCollection
  private List<String> pendingStudentIds;
}
