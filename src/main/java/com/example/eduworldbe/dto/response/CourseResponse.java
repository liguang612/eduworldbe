package com.example.eduworldbe.dto.response;

import com.example.eduworldbe.model.User;
import com.example.eduworldbe.model.Chapter;

import lombok.Data;

import java.util.List;

@Data
public class CourseResponse {
  private String id;
  private String name;
  private String avatar;
  private String description;
  private String subjectId;
  private String subjectName;
  private Integer grade;
  private List<String> allCategories;
  private User teacher;
  private List<User> teacherAssistants;
  private List<User> students;
  private List<User> pendingStudents;
  private List<Chapter> chapters;
  private List<String> reviewIds;
  private double averageRating;
  private boolean hidden;
  private boolean allowStudentPost;
  private boolean requirePostApproval;
  private boolean favourite;
}
