package com.example.eduworldbe.dto.response;

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
  private String subjectId;
  private String subjectName;
  private Integer grade;
  private User teacher;
  private Integer duration;
  private boolean favourite;
  private double averageRating;
}