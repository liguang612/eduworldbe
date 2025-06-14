package com.example.eduworldbe.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class UpdateQuestionRequest {
  @NotBlank
  private String title;

  @NotBlank
  private String type;

  private Integer level;

  private String sharedMediaId;

  private List<String> categories;

  private List<String> solutionIds;
}