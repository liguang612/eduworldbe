package com.example.eduworldbe.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class ChoiceBatchRequest {
  private String questionId;
  private List<ChoiceItem> choices;

  @Data
  public static class ChoiceItem {
    private String text;
    private String value;
    private Integer orderIndex;
    private Boolean isCorrect;
  }
}