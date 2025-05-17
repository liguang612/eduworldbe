package com.example.eduworldbe.dto;

import lombok.Data;
import java.util.List;

@Data
public class MatchingQuestionRequest {
  private String questionId;
  private String sharedMediaId;
  private List<MatchingColumnItem> left;
  private List<MatchingColumnItem> right;
  private List<MatchingPairItem> pairs;

  @Data
  public static class MatchingColumnItem {
    private String label;
    private Integer orderIndex;
  }

  @Data
  public static class MatchingPairItem {
    private Integer leftIndex;
    private Integer rightIndex;
  }
}