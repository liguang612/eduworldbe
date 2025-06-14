package com.example.eduworldbe.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class MatchingColumnBatchRequest {
  private String questionId;
  private List<MatchingColumnItem> left;
  private List<MatchingColumnItem> right;

  @Data
  public static class MatchingColumnItem {
    private String label;
    private Integer orderIndex;
  }
}