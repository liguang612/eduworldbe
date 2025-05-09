package com.example.eduworldbe.dto;

import com.example.eduworldbe.model.User;
import lombok.Data;
import java.util.List;

@Data
public class LectureResponse {
  private String id;
  private String name;
  private String description;
  private String contents;
  private List<String> endQuestions;
  private List<String> categories;
  private String pdfUrl;
  private String courseId;
  private User teacher;
}