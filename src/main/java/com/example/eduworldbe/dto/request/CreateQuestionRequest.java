package com.example.eduworldbe.dto.request;

import java.util.List;

import lombok.Data;

@Data
public class CreateQuestionRequest {
  private String title;
  private String subjectId;
  private String type;
  private String sharedMediaId;
  private Integer level;
  private List<String> categories;
}
