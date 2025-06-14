package com.example.eduworldbe.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class MatchingPairBatchRequest {
  private String questionId;
  private List<MatchingPairItem> pairs;

  @Data
  public static class MatchingPairItem {
    private String from;
    private String to;
  }
}