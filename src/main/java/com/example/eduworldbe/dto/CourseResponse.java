package com.example.eduworldbe.dto;

import com.example.eduworldbe.model.User;
import lombok.Data;

import java.util.List;

@Data
public class CourseResponse {
  private String id;
  private String name;
  private String subjectId;
  private List<String> allCategories;
  private User teacher;
  private List<User> teacherAssistants;
  private List<User> students;
  private List<String> lectureIds;
  private List<String> reviewIds;
  private double averageRating;
}
